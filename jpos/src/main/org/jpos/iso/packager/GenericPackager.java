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

package org.jpos.iso.packager;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.IF_TBASE;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsgFieldPackager;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
/**
 * <pre>
 * GenericPackager uses an XML config file to describe the layout of an ISOMessage
 * The general format is as follows
 * &lt;isopackager&gt;
 *     &lt;isofield 
 *         id="[field id]"
 *         name="[field name]"
 *         length="[max field length]"
 *         class="[org.jpos.iso.IF_*]"
 *         pad="true|false"&gt;  
 *     &lt;/isofield&gt;
 *     ...
 * &lt;/isopackager&gt;
 *
 * Fields that contain subfields can be handled as follows
 * &lt;isofieldpackager
 *     id="[field id]"
 *     name="[field name]"
 *     length="[field length]"
 *     class="[org.jpos.iso.IF_*]"
 *     packager="[org.jpos.iso.packager.*]"&gt;
 *      
 *     &lt;isofield 
 *         id="[subfield id]"
 *         name="[subfield name]"
 *         length="[max subfield length]"
 *         class="[org.jpos.iso.IF_*]"
 *         pad="true|false"&gt;
 *     &lt;/isofield&gt;
 *         ...
 * &lt;/isofieldpackager&gt;
 *
 * The optional attributes maxValidField, bitmapField and emitBitmap
 * are allowed on the isopackager node.
 *
 * </pre>
 * @author Eoin Flood
 * @version $Revision$ $Date$
 * @see ISOPackager
 * @see ISOBasePackager
 */
public class GenericPackager 
    extends ISOBasePackager implements ReConfigurable
{
   /* Values copied from ISOBasePackager
      These can be changes using attributes on the isopackager node */ 
    private int maxValidField=128;
    private boolean emitBitmap=true;
    private int bitmapField=1;

    public GenericPackager() throws ISOException
    {
        super();
    }

    /** 
     * Create a GenericPackager with the field descriptions 
     * from an XML File
     * @param filename The XML field description file
     */
    public GenericPackager(String filename) throws ISOException
    {
        this();
        readFile(filename);
    }

    /** 
     * Create a GenericPackager with the field descriptions 
     * from an XML InputStream
     * @param input The XML field description InputStream
     */
    public GenericPackager(InputStream input) throws ISOException
    {
        this();
        readFile(input);
    }

    /**
     * Packager Configuration.
     *
     * <ul>
     *  <li>packager-config
     *  <li>packager-logger
     *  <li>packager-realm
     * </ul>
     *
     * @param cfg Configuration
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        try
        {
            String loggerName = cfg.get("packager-logger");
            if (loggerName != null)
                setLogger(Logger.getLogger (loggerName), 
                           cfg.get ("packager-realm"));

            readFile(cfg.get("packager-config"));
        } 
        catch (ISOException e) 
        {
            throw new ConfigurationException(e);
        }
    }

    protected int getMaxValidField()
    {
        return maxValidField;
    }

    protected boolean emitBitMap()
    {
        return emitBitmap;
    }

    protected ISOFieldPackager getBitMapfieldPackager()
    {
        return fld[bitmapField];
    }

    /**
     * Parse the field descriptions from an XML file.
     *
     * <pre>
     * Uses the sax parser specified by the system property 'sax.parser'
     * The default parser is org.apache.xerces.parsers.SAXParser 
     * </pre>
     * @param filename The XML field description file
     */
    public void readFile(String filename) throws ISOException
    {
        try {
            createXMLReader().parse(filename);  
        } 
        catch (Exception e) {
            throw new ISOException(e);
        }
    }

    /**
     * Parse the field descriptions from an XML InputStream.
     *
     * <pre>
     * Uses the sax parser specified by the system property 'sax.parser'
     * The default parser is org.apache.xerces.parsers.SAXParser 
     * </pre>
     * @param input The XML field description InputStream
     */
    public void readFile(InputStream input) throws ISOException
    {
        try {
            createXMLReader().parse(new InputSource(input));
        } 
        catch (Exception e) {
            throw new ISOException(e);
        }
    }

    private XMLReader createXMLReader () throws SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader(
                System.getProperty( "sax.parser", 
                "org.apache.xerces.parsers.SAXParser"));

        reader.setFeature ("http://xml.org/sax/features/validation", true);
        GenericContentHandler handler = new GenericContentHandler();
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);
        return reader;
    }


    private void setGenericPackagerParams (Attributes atts)
    {
        String maxField  = atts.getValue("maxValidField");
        String emitBmap  = atts.getValue("emitBitmap");
        String bmapfield = atts.getValue("bitmapField");

        if (maxField != null)
            maxValidField = Integer.parseInt(maxField); 

        if (emitBmap != null)
            emitBitmap = Boolean.valueOf(emitBmap).booleanValue();

        if (bmapfield != null)
            bitmapField = Integer.parseInt(bmapfield);
    }


    public class GenericContentHandler extends DefaultHandler
    {
        private Stack fieldStack;

        public void startDocument()
        {
            fieldStack = new Stack();
        }

        public void endDocument() throws SAXException
        {
            if (!fieldStack.isEmpty())
            {
                throw new SAXException ("Format error in XML Field Description File");
            }
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
                throws SAXException
        {
            try
            {
                String id   = atts.getValue("id");
                String type = atts.getValue("class");
                String name = atts.getValue("name");
                String size = atts.getValue("length");
                String pad  = atts.getValue("pad");
                // Modified for using IF_TBASE
                String token = atts.getValue("token");

                if (localName.equals("isopackager"))
                {
                    // Stick a new Hashtable on stack to collect the fields
                    fieldStack.push(new Hashtable());

                    setGenericPackagerParams (atts);
                }

                if (localName.equals("isofieldpackager"))
                {
                    /*
                    For a isofield packager node push the following fields
                    onto the stack.
                    1) an Integer indicating the field ID
                    2) an instance of the specified ISOFieldPackager class
                    3) an instance of the specified ISOBasePackager (msgPackager) class
                    4) a Hashtable to collect the subfields
                    */
                    String packager = atts.getValue("packager");

                    fieldStack.push(new Integer(id));

                    ISOFieldPackager f;
                    f = (ISOFieldPackager) Class.forName(type).newInstance();   
                    f.setDescription(name);
                    f.setLength(Integer.parseInt(size));
                    f.setPad(new Boolean(pad).booleanValue());
                    // Modified for using IF_TBASE
                    if( f instanceof IF_TBASE){
                      ((IF_TBASE)f).setToken( token );
                    }
                    fieldStack.push(f);

                    ISOBasePackager p;
                    p = (ISOBasePackager) Class.forName(packager).newInstance();
                    if (p instanceof GenericPackager)
                    {
                        GenericPackager gp = (GenericPackager) p;
                        gp.setGenericPackagerParams (atts);
                    }
                    fieldStack.push(p);

                    fieldStack.push(new Hashtable());
                }

                if (localName.equals("isofield"))
                {
                    Class c = Class.forName(type);
                    ISOFieldPackager f;
                    f = (ISOFieldPackager) c.newInstance();     
                    f.setDescription(name);
                    f.setLength(Integer.parseInt(size));
                    f.setPad(new Boolean(pad).booleanValue());
                    // Modified for using IF_TBASE
                    if( f instanceof IF_TBASE){
                      ((IF_TBASE)f).setToken( token );
                    }
                    // Insert this new isofield into the Hashtable
                    // on the top of the stack using the fieldID as the key
                    Hashtable ht = (Hashtable) fieldStack.peek();
                    ht.put(new Integer(id), f);
                }
            }
            catch (Exception ex)
            {
                throw new SAXException(ex);
            }
        }

        /**
         * Convert the ISOFieldPackagers in the Hashtable
         * to an array of ISOFieldPackagers
         */
        private ISOFieldPackager[] makeFieldArray(Hashtable tab)
        {
            int maxField = 0;

            // First find the largest field number in the Hashtable
            for (Enumeration e=tab.keys(); e.hasMoreElements(); )
            {
                int n = ((Integer)e.nextElement()).intValue();
                if (n > maxField) maxField = n;
            }

            // Create the array
            ISOFieldPackager fld[] = new ISOFieldPackager[maxField+1];

            // Populate it
            for (Enumeration e=tab.keys(); e.hasMoreElements(); )
            {
                Integer key = (Integer) e.nextElement();
                fld[key.intValue()] = (ISOFieldPackager)tab.get(key);
            }
            return fld;
        }

        public void endElement(String namespaceURI, String localName, String qName)
        {
            if (localName.equals("isopackager"))
            {
                Hashtable tab  = (Hashtable)fieldStack.pop();

                setFieldPackager(makeFieldArray(tab));
            }

            if (localName.equals("isofieldpackager"))
            {
                // Pop the 4 entries off the stack in the correct order
                Hashtable tab = (Hashtable)fieldStack.pop();

                ISOBasePackager msgPackager = (ISOBasePackager) fieldStack.pop();
                msgPackager.setFieldPackager (makeFieldArray(tab));
                msgPackager.setLogger (getLogger(), "Generic Packager");

                ISOFieldPackager fieldPackager = (ISOFieldPackager) fieldStack.pop();

                Integer fno = (Integer) fieldStack.pop();

                // Create the ISOMsgField packager with the retrieved msg and field Packagers
                ISOMsgFieldPackager mfp = 
                    new ISOMsgFieldPackager(fieldPackager, msgPackager);

                // Add the newly created ISOMsgField packager to the
                // lower level field stack

                tab=(Hashtable)fieldStack.peek();
                tab.put(fno, mfp);
            }
        }

        // ErrorHandler Methods
        public void error (SAXParseException ex) throws SAXException
        {
            throw ex;
        }

        public void fatalError (SAXParseException ex) throws SAXException
        {
            throw ex;
        }
    }
}

