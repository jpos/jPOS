/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.util;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;
import org.jpos.space.Space;
import org.jpos.space.TransientSpace;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;
import org.jpos.iso.ISOException;

/**
 * General purpose, Field Separator delimited message
 *
 * @author Alejandro Revila
 * @since 1.4.7
 */
public class FSDMsg implements Loggeable {
    public static final char FS = '\034';

    Map map;
    String baseSchema;
    String basePath;
    byte[] header;

    /**
     * @param basePath   schema path
     */
    public FSDMsg (String basePath) {
        this (basePath, "base");
    }
    /**
     * @param basePath   schema path
     * @param baseSchema schema name
     */
    public FSDMsg (String basePath, String baseSchema) {
        super();
        map = new LinkedHashMap();
        this.basePath   = basePath;
        this.baseSchema = baseSchema;
    }

    /**
     * parse message
     *
     * @param is input stream
     *
     * @throws IOException
     * @throws JDOMException
     * @throws MalformedURLException
     */
    public void unpack (InputStream is) 
        throws IOException, JDOMException, MalformedURLException
    {
        try {
            unpack (is, getSchema (baseSchema));
        } catch (EOFException e) {
            map.put ("EOF", "true");
        }
    }
    /**
     * parse message
     *
     * @param b message image
     *
     * @throws IOException
     * @throws JDOMException
     * @throws MalformedURLException
     */
    public void unpack (byte[] b) 
        throws IOException, JDOMException, MalformedURLException
    {
        unpack (new ByteArrayInputStream (b));
    }

    /**
     * @return message string
     */
    public String pack () 
        throws JDOMException, MalformedURLException, ISOException, IOException
    {
        StringBuffer sb = new StringBuffer ();
        pack (getSchema (baseSchema), sb);
        return sb.toString ();
    }

    protected String get (String id, String type, int length, String defValue) 
        throws ISOException
    {
        String value = (String) map.get (id);
        if (value == null)
            value = defValue == null ? "" : defValue;

        type   = type.toUpperCase ();

        switch (type.charAt (0)) {
            case 'N':
                value = ISOUtil.zeropad (value, length);
                break;
            case 'A':
                value = ISOUtil.strpad (value, length);
                break;
            case 'K':
                if (defValue != null)
                    value = defValue;
                break;
            case 'B':
                try {
                    value = new String (
                        ISOUtil.hex2byte (value.substring (0, length << 1)),
                        "ISO8859_1"
                    );
                } catch (UnsupportedEncodingException e) {
                    // ISO8859_1 is supported
                }
                break;
        }
        return (type.endsWith ("FS")) ? value.trim() : value;
    }

    protected void pack (Element schema, StringBuffer sb) 
        throws JDOMException, MalformedURLException, ISOException, IOException
    {
        String keyOff = "";
        Iterator iter = schema.getChildren("field").iterator();
        while (iter.hasNext()) {
            Element elem = (Element) iter.next ();
            String id    = elem.getAttributeValue ("id");
            int length   = Integer.parseInt (elem.getAttributeValue ("length"));
            String type  = elem.getAttributeValue ("type");
            boolean key  = "true".equals (elem.getAttributeValue ("key"));
            String defValue = elem.getText();
            String value = get (id, type, length, defValue);
            sb.append (value);
            if (type.endsWith ("FS"))
                sb.append (FS);
            if (key) 
                keyOff = keyOff + value;
        }
        if (keyOff.length() > 0) 
            pack (getSchema (getId (schema) + keyOff), sb);
    }

    protected void unpack (InputStream is, Element schema) 
        throws IOException, JDOMException, MalformedURLException 
    
    {
        Iterator iter = schema.getChildren("field").iterator();
        String keyOff = "";
        while (iter.hasNext()) {
            Element elem = (Element) iter.next();

            String id    = elem.getAttributeValue ("id");
            int length   = Integer.parseInt (elem.getAttributeValue ("length"));
            String type  = elem.getAttributeValue ("type").toUpperCase();
            boolean key  = "true".equals (elem.getAttributeValue ("key"));
            String value = readField (
                is, id, length, type.endsWith ("FS"), "B".equals (type)
            );

            if (key)
                keyOff = keyOff + value;

            if ("K".equalsIgnoreCase (type) && !value.equals (elem.getText()))
                throw new IllegalArgumentException (
                    "Field "+id 
                       + " value='"     +value
                       + "' expected='" + elem.getText () + "'"
                );
        }
        if (keyOff.length() > 0) {
            unpack (is, 
                getSchema (getId (schema) + keyOff)
            );
        }
    }
    private String getId (Element e) {
        String s = e.getAttributeValue ("id");
        return s == null ? "" : s;
    }
    protected String read (InputStream is, int len, boolean fs) 
        throws IOException 
    {
        StringBuffer sb = new StringBuffer();
        byte[] b = new byte[1];
        for (int i=0; i<len; i++) {
            if (is.read (b) < 0)
                throw new EOFException ();
            if (b[0] == FS) {
                fs = false;
                break;
            }
            sb.append ((char) (b[0] & 0xff));
        }
        if (fs) {
            if (is.read (b) < 0)
                throw new EOFException ();
        }
        return sb.toString ();
    }
    protected String readField 
        (InputStream is, String fieldName, int len, boolean fs, boolean binary) 
        throws IOException
    {
        String fieldValue = read (is, len, fs);
        if (binary)
            fieldValue = ISOUtil.hexString (fieldValue.getBytes ("ISO8859_1"));
        map.put (fieldName, fieldValue);
        // System.out.println (fieldName + ":" + fieldValue);
        return fieldValue;
    }
    public void set (String name, String value) {
        if (value != null)
            map.put (name, value);
        else
            map.remove (name);
    }
    public void setHeader (byte[] h) {
        this.header = h;
    }
    public byte[] getHeader () {
        return header;
    }
    public String getHexHeader () {
        return header != null ? ISOUtil.hexString (header).substring (2) : "";
    }
    public String get (String name) {
        return (String) map.get (name);
    }
    public void copy (String name, FSDMsg msg) {
        map.put (name, msg.get (name));
    }
    public byte[] getHexBytes (String name) {
        String s = get (name);
        return s == null ? null : ISOUtil.hex2byte (s);
    }
    public int getInt (String name) {
        int i = 0;
        try {
            i = Integer.parseInt (get (name));
        } catch (Exception e) { }
        return i;
    }
    public Element toXML () {
        Element e = new Element ("message");
        if (header != null) {
            e.addContent (
                new Element ("header")
                    .setText (getHexHeader ())
            );
        }
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String fieldName = (String) iter.next();
            Element inner = new Element (fieldName);
            inner.addContent (ISOUtil.normalize ((String) map.get (fieldName)));
            e.addContent (inner);
        }
        return e;
    }
    protected Element getSchema () 
        throws JDOMException, MalformedURLException, IOException
    {
        return getSchema (baseSchema);
    }
    protected Element getSchema (String message) 
        throws JDOMException, MalformedURLException, IOException
    {
        StringBuffer sb = new StringBuffer (basePath);
        sb.append (message);
        sb.append (".xml");
        String uri = sb.toString ();

        Space sp = TransientSpace.getSpace();
        Element schema = (Element) sp.rdp (uri);
        if (schema == null) {
            synchronized (FSDMsg.class) {
                schema = (Element) sp.rdp (uri);
                if (schema == null) {
                    SAXBuilder builder = new SAXBuilder ();
                    schema = builder.build (new URL (uri)).getRootElement ();
                }
                sp.out (uri, schema);
            }
        } 
        return schema;
    }
    public void dump (PrintStream p, String indent) {
        try {
            XMLOutputter out = new XMLOutputter (Format.getPrettyFormat());
            out.output (toXML(), p);
        } catch (IOException e) {
            e.printStackTrace (p);
        }
    }
    /**
     * @return message's Map
     */
    public Map getMap () {
        return map;
    }
}

