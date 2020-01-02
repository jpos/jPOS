/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.packager;

import org.jpos.iso.*;
import org.jpos.iso.header.BaseHeader;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.Stack;

/**
 * packs/unpacks ISOMsgs into XML representation
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 */
@SuppressWarnings("unchecked")
public class XMLPackager extends DefaultHandler
                         implements ISOPackager, LogSource
{
    protected Logger logger = null;
    protected String realm = null;
    private ByteArrayOutputStream out;
    private PrintStream p;
    private XMLReader reader;
    private Stack stk;

    public static final String ISOMSG_TAG    = "isomsg";
    public static final String ISOFIELD_TAG  = "field";
    public static final String ID_ATTR       = "id";
    public static final String VALUE_ATTR    = "value";
    public static final String TYPE_ATTR     = "type";
    public static final String TYPE_BINARY   = "binary";
    public static final String TYPE_BITMAP   = "bitmap";
    public static final String TYPE_AMOUNT   = "amount";
    public static final String CURRENCY_ATTR = "currency";
    public static final String HEADER_TAG    = "header";
    public static final String ENCODING_ATTR = "encoding";
    public static final String ASCII_ENCODING= "ascii";

    // fields that will be forced to be interpreted as binary data
    private int[] binaryFields= null;

    public XMLPackager() throws ISOException {
        super();
        out = new ByteArrayOutputStream();
        try {
            p   = new PrintStream(out, false, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
            // utf-8 is a supported encoding
        }
        stk = new Stack();
        try {
            reader = createXMLReader();

            // some parser restrictions have been set for security and maybe PCI compliance
            setXMLParserFeature("http://xml.org/sax/features/validation", false);
            setXMLParserFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            setXMLParserFeature("http://xml.org/sax/features/external-general-entities", false);
            setXMLParserFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Exception e) {
            throw new ISOException (e.toString());
        }
    }

    public void forceBinary(int ... bfields) {
        binaryFields= bfields;
    }

    public byte[] pack (ISOComponent c) throws ISOException {
        LogEvent evt = new LogEvent (this, "pack");
        try {
            if (!(c instanceof ISOMsg))
                throw new ISOException ("cannot pack "+c.getClass());
            ISOMsg m = (ISOMsg) c;
            byte[] b;
            synchronized (this) {
                m.setDirection(0);  // avoid "direction=xxxxxx" in XML msg
                m.dump (p, "");
                b = out.toByteArray();
                out.reset();
            }
            if (logger != null)
                evt.addMessage (m);
            return b;
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } finally {
            Logger.log(evt);
        }
    }

    public synchronized int unpack (ISOComponent c, byte[] b)
        throws ISOException
    {
        LogEvent evt = new LogEvent (this, "unpack");
        try {
            if (!(c instanceof ISOMsg))
                throw new ISOException
                    ("Can't call packager on non Composite");

            while (!stk.empty())    // purge from possible previous error
                stk.pop();

            InputSource src = new InputSource (new ByteArrayInputStream(b));
            reader.parse (src);
            if (stk.empty())
                throw new ISOException ("error parsing");

            ISOMsg m = (ISOMsg) c;
            ISOMsg m1 = (ISOMsg) stk.pop();
            m.merge (m1);
            m.setHeader (m1.getHeader());

            fixupBinary(m, binaryFields);

            if (logger != null)
                evt.addMessage (m);
            return b.length;
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } catch (IOException e) {
            evt.addMessage (e);
            throw new ISOException (e.toString());
        } catch (SAXException e) {
            evt.addMessage (e);
            throw new ISOException (e.toString());
        } finally {
            Logger.log (evt);
        }
    }

    public synchronized void unpack (ISOComponent c, InputStream in)
        throws ISOException, IOException
    {
        LogEvent evt = new LogEvent (this, "unpack");
        try {
            if (!(c instanceof ISOMsg))
                throw new ISOException
                    ("Can't call packager on non Composite");

            while (!stk.empty())    // purge from possible previous error
                stk.pop();

            reader.parse (new InputSource (in));
            if (stk.empty())
                throw new ISOException ("error parsing");

            ISOMsg m = (ISOMsg) c;
            ISOMsg m1 = (ISOMsg) stk.pop();
            m.merge (m1);
            m.setHeader (m1.getHeader());

            fixupBinary(m, binaryFields);

            if (logger != null)
                evt.addMessage (m);
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } catch (SAXException e) {
            evt.addMessage (e);
            throw new ISOException (e.toString());
        } finally {
            Logger.log (evt);
        }
    }

    public void startElement
        (String ns, String name, String qName, Attributes atts)
        throws SAXException
    {
        int fieldNumber = -1;
        try {
            String id       = atts.getValue(ID_ATTR);
            if (id != null) {
                try {
                    fieldNumber = Integer.parseInt (id);
                } catch (NumberFormatException ex) {
                    throw new SAXException ("Invalid id " + id);
                }
            }
            if (name.equals (ISOMSG_TAG)) {
                if (fieldNumber >= 0) {
                    if (stk.empty())
                        throw new SAXException ("inner without outer");

                    ISOMsg inner = new ISOMsg(fieldNumber);
                    ((ISOMsg)stk.peek()).set (inner);
                    stk.push (inner);
                } else {
                    stk.push (new ISOMsg(0));
                }
            } else if (name.equals (ISOFIELD_TAG)) {
                ISOMsg m     = (ISOMsg) stk.peek();
                String value = atts.getValue(VALUE_ATTR);
                String type  = atts.getValue(TYPE_ATTR);
                if (id == null)
                    throw new SAXException ("invalid field");
                value = value == null ? "" : value;

                ISOComponent ic;
                if (TYPE_BINARY.equals (type)) {
                    ic = new ISOBinaryField (
                        fieldNumber,
                            ISOUtil.hex2byte (
                                value.getBytes(), 0, value.length()/2
                            )
                        );
                }
                else if (TYPE_AMOUNT.equals (type)) {
                    ic =  new ISOAmount(
                        fieldNumber,
                        Integer.parseInt (atts.getValue(CURRENCY_ATTR)),
                        new BigDecimal (value)
                    );
                }
                else {
                    ic = new ISOField (fieldNumber, ISOUtil.stripUnicode(value));
                }
                m.set (ic);
                stk.push (ic);
            } else if (HEADER_TAG.equals (name)) {
                BaseHeader bh = new BaseHeader();
                bh.setAsciiEncoding (ASCII_ENCODING.equalsIgnoreCase(atts.getValue(ENCODING_ATTR)));
                stk.push (bh);
            }
        } catch (ISOException e) {
            throw new SAXException
                ("ISOException unpacking "+fieldNumber);
        }
    }
    public void characters (char ch[], int start, int length) {
        Object obj = stk.peek();
        if (obj instanceof ISOField) {
            ISOField f = (ISOField) obj;
            String value = f.getValue() + new String(ch, start, length);
            try {
                f.setValue(value);
            } catch (ISOException e) {
                try {
                    f.setValue (e.getMessage());
                } catch (ISOException ignored) {
                    // giving up
                }
            }
        }
        else if (obj instanceof BaseHeader) {
            BaseHeader bh = (BaseHeader) obj;
            String s = new String(ch,start,length);
            if (bh.isAsciiEncoding()) {
                bh.unpack (s.getBytes());
            } else {
                bh.unpack (ISOUtil.hex2byte (s));
            }
        }
    }
    public void endElement (String ns, String name, String qname)
        throws SAXException
    {
        if (name.equals (ISOMSG_TAG)) {
            ISOMsg m = (ISOMsg) stk.pop();
            if (stk.empty())
                stk.push (m); // push outer message
        } else if (ISOFIELD_TAG.equals (name)) {
            stk.pop();
        } else if (HEADER_TAG.equals (name)) {
            BaseHeader h = (BaseHeader) stk.pop();
            ISOMsg m = (ISOMsg) stk.peek ();
            m.setHeader (h);
        }
    }

    // we may want to force fome fields to be interpreted as binary data
    protected void fixupBinary(ISOMsg m, int[] bfields) throws ISOException {
        if (bfields != null) {
            for (int f : bfields) {
                if (m.hasField(f)) {
                    ISOComponent c = m.getComponent(f);
                    if (c instanceof ISOField)
                        m.set(f, ((ISOField) c).getBytes());
                }
            }
        }
    }

    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return "Data element " + fldNumber;
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
    public ISOMsg createISOMsg () {
        return new ISOMsg();
    }
    public String getDescription () {
        return getClass().getName();
    }

    protected XMLReader createXMLReader () throws SAXException {
        XMLReader reader;
        try {
            reader = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            reader = XMLReaderFactory.createXMLReader (
                System.getProperty(
                    "org.xml.sax.driver",
                    "org.apache.crimson.parser.XMLReaderImpl"
                )
            );
        }

        reader.setContentHandler(this);
        reader.setErrorHandler(this);
        return reader;
    }

    public void setXMLParserFeature(String fname, boolean val) throws SAXException  {
        reader.setFeature(fname, val);
    }
}

