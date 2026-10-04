/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * packs/unpacks ISOMsgs from jPOS logs
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 */
@SuppressWarnings("unchecked")
public class LogPackager extends DefaultHandler
                         implements ISOPackager, LogSource
{
    private static final int MAX_ISOMSG_NESTING_DEPTH = 32;
    /** Logger receiving pack/unpack diagnostic events. */
    protected Logger logger = null;
    /** Logger realm associated with this packager. */
    protected String realm = null;
    private ByteArrayOutputStream out;
    private PrintStream p;
    private XMLReader reader = null;
    private Stack<ISOMsg> stk;
    private int isomsgDepth;
    private boolean rootComplete;

    private Lock lock = new ReentrantLock();

    /** XML element name for the outer log wrapper. */
    public static final String LOG_TAG       = "log";
    /** XML element name for an ISO message. */
    public static final String ISOMSG_TAG    = "isomsg";
    /** XML element name for an ISO field. */
    public static final String ISOFIELD_TAG  = "field";
    /** XML attribute carrying the field number. */
    public static final String ID_ATTR       = "id";
    /** XML attribute carrying the field value. */
    public static final String VALUE_ATTR    = "value";
    /** XML attribute identifying the field type ({@link #TYPE_BINARY}, {@link #TYPE_BITMAP}). */
    public static final String TYPE_ATTR     = "type";
    /** Field-type marker: hex-encoded binary value. */
    public static final String TYPE_BINARY   = "binary";
    /** Field-type marker: bitmap value. */
    public static final String TYPE_BITMAP   = "bitmap";

    /**
     * Constructs the packager and prepares its underlying SAX parser.
     *
     * @throws ISOException if the configured SAX parser cannot be instantiated
     */
    public LogPackager() throws ISOException {
        super();
        out = new ByteArrayOutputStream();
        p   = new PrintStream(out);
        stk = new Stack<>();
        try {
            reader = createXMLReader();
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            try {
                reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (SAXNotRecognizedException | SAXNotSupportedException ignored) {
                // DOCTYPE declarations are rejected, so this additional control is optional.
            }
            reader.setEntityResolver((publicId, systemId) -> {
                throw new SAXException("External entities are not allowed");
            });
        } catch (Exception e) {
            throw new ISOException(e);
        }
    }
    public byte[] pack (ISOComponent c) throws ISOException {
        LogEvent evt = new LogEvent (this, "pack");
        lock.lock();
        try {
            if (!(c instanceof ISOMsg))
                throw new ISOException ("cannot pack "+c.getClass());
            ISOMsg m = (ISOMsg) c;
            byte[] b;
            p.println ("<log>");
            c.dump (p, " ");
            p.println ("</log>");
            b = out.toByteArray();
            out.reset();
            if (logger != null)
                evt.addMessage (m);
            return b;
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } finally {
            Logger.log(evt);
            lock.unlock();
        }
    }

    public int unpack (ISOComponent c, byte[] b)
        throws ISOException
    {
        unpack(c, new InputSource(new ByteArrayInputStream(b)));
        return b.length;
    }

    public void unpack (ISOComponent c, InputStream in)
        throws ISOException, IOException
    {
        unpack(c, new InputSource(in));
    }

    private void unpack (ISOComponent c, InputSource in) throws ISOException {
        LogEvent evt = new LogEvent (this, "unpack");
        lock.lock();
        try {
            if (!(c instanceof ISOMsg m))
                throw new ISOException("Can't call packager on non Composite");

            stk.clear();
            isomsgDepth = 0;
            rootComplete = false;

            reader.parse(in);
            if (!rootComplete || stk.size() != 1)
                throw new ISOException("error parsing log message");

            m.merge(stk.pop());
            if (logger != null)
                evt.addMessage(m);
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (IOException | SAXException | RuntimeException e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            stk.clear();
            isomsgDepth = 0;
            rootComplete = false;
            Logger.log(evt);
            lock.unlock();
        }
    }

    public void startElement 
        (String ns, String name, String qName, Attributes atts)
        throws SAXException
    {
        int fieldNumber = -1;
        try {
            String elementName = name == null || name.isEmpty() ? qName : name;
            boolean isMessage = ISOMSG_TAG.equals(elementName);
            boolean isField = ISOFIELD_TAG.equals(elementName);
            String id       = atts.getValue(ID_ATTR);
            if ((isMessage || isField) && id != null) {
                try {
                    fieldNumber = Integer.parseInt (id);
                } catch (NumberFormatException ex) {
                    throw new SAXException("invalid field id", ex);
                }
            }
            if (isMessage) {
                if (isomsgDepth >= MAX_ISOMSG_NESTING_DEPTH)
                    throw new SAXException("Maximum isomsg nesting depth exceeded");
                if (isomsgDepth == 0) {
                    if (fieldNumber >= 0)
                        throw new SAXException("inner without outer");
                    if (rootComplete || !stk.empty())
                        throw new SAXException("multiple outer messages");
                    stk.push(new ISOMsg(0));
                } else {
                    if (fieldNumber < 0)
                        throw new SAXException("inner message without id");
                    if (stk.empty())
                        throw new SAXException("inner without outer");

                    ISOMsg inner = new ISOMsg(fieldNumber);
                    stk.peek().set(inner);
                    stk.push(inner);
                }
                isomsgDepth++;
            } else if (isField) {
                if (isomsgDepth == 0 || stk.empty())
                    throw new SAXException("field without isomsg");
                ISOMsg m     = stk.peek();
                String value = atts.getValue(VALUE_ATTR);
                String type  = atts.getValue(TYPE_ATTR);
                if (id == null || value == null)
                    throw new SAXException ("invalid field");   
                if (TYPE_BINARY.equals (type)) {
                    m.set (new ISOBinaryField (
                        fieldNumber, 
                            ISOUtil.hex2byte (
                                value.getBytes(), 0, value.length()/2
                            )
                        )
                    );
                }
                else {
                    m.set (new ISOField (fieldNumber, value));
                }
            }
        } catch (ISOException e) {
            throw new SAXException("ISOException unpacking " + fieldNumber, e);
        }
    }

    public void endElement (String ns, String name, String qname) 
        throws SAXException
    {
        String elementName = name == null || name.isEmpty() ? qname : name;
        if (ISOMSG_TAG.equals(elementName)) {
            if (isomsgDepth == 0 || stk.empty())
                throw new SAXException("isomsg close without open");
            ISOMsg m = stk.pop();
            isomsgDepth--;
            if (isomsgDepth == 0) {
                if (!stk.empty())
                    throw new SAXException("invalid isomsg structure");
                stk.push(m); // retain outer message until parsing completes
                rootComplete = true;
            }
        }
    }

    private XMLReader createXMLReader() throws SAXException {
        String parserClass = System.getProperty("sax.parser");
        if (parserClass != null && !parserClass.isBlank()) {
            XMLReader configuredReader = XMLReaderFactory.createXMLReader(parserClass);
            configuredReader.setContentHandler(this);
            configuredReader.setErrorHandler(this);
            return configuredReader;
        }

        XMLReader xmlReader;
        try {
            xmlReader = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            xmlReader = XMLReaderFactory.createXMLReader(
                System.getProperty(
                    "org.xml.sax.driver",
                    "org.apache.crimson.parser.XMLReaderImpl"
                )
            );
        }
        xmlReader.setContentHandler(this);
        xmlReader.setErrorHandler(this);
        return xmlReader;
    }

    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return "<notavailable/>";
    }
    public String getDescription () {
        return getClass().getName();
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
    public ISOMsg createISOMsg() {
        return new ISOMsg();
    }
}
