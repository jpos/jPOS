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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;


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
 * The optional attributes maxValidField, bitmapField, thirdBitmapField, and emitBitmap
 * are allowed on the isopackager node.
 *
 * </pre>
 * @author Eoin Flood
 * @version $Revision$ $Date$
 * @see ISOPackager
 * @see ISOBasePackager
 */

@SuppressWarnings("unchecked")
public class GenericPackager
    extends ISOBasePackager implements Configurable
{
   /* Values copied from ISOBasePackager
      These can be changes using attributes on the isopackager node */
    private int maxValidField=128;
    private boolean emitBitmap=true;
    private int bitmapField=1;
    private String firstField = null;
    private String filename;

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
        this.filename = filename;
        readFile(filename);
    }

    /**
     * Create a GenericPackager with the field descriptions
     * from an XML InputStream
     * @param input The XML field description InputStream
     */
    public GenericPackager(InputStream input) throws ISOException
    {
        readFile(input);
    }

    /**
     * Packager Configuration.
     *
     * <ul>
     *  <li>packager-config
     *  <li>packager-logger
     *  <li>packager-log-fieldname
     *  <li>packager-realm
     * </ul>
     *
     * @param cfg Configuration
     */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        filename = cfg.get("packager-config", null);
        if (filename == null)
            throw new ConfigurationException("packager-config property cannot be null");

        try
        {
            String loggerName = cfg.get("packager-logger");
            if (loggerName != null)
                setLogger(Logger.getLogger (loggerName),
                           cfg.get ("packager-realm"));

            // inherited protected logFieldName
            logFieldName= cfg.getBoolean("packager-log-fieldname", logFieldName);

            readFile(filename);
        } catch (ISOException e)
        {
            throw new ConfigurationException(e.getMessage(), e.fillInStackTrace());
        }
    }

    @Override
    protected int getMaxValidField()
    {
        return maxValidField;
    }

    @Override
    protected boolean emitBitMap()
    {
        return emitBitmap;
    }

    @Override
    protected ISOFieldPackager getBitMapfieldPackager()
    {
        return fld[bitmapField];
    }

    /**
     * Parse the field descriptions from an XML file.
     *
     * <pre>
     * Uses the sax parser specified by the system property 'sax.parser'
     * The default parser is org.apache.crimson.parser.XMLReaderImpl
     * </pre>
     * @param filename The XML field description file
     */
    public void readFile(String filename) throws ISOException
    {
        try {
            if (filename.startsWith("jar:") && filename.length()>4) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                readFile(
                    cl.getResourceAsStream(filename.substring(4))
                );
            } else {
                createXMLReader().parse(filename);
            }
        }
        catch (Exception e) {
            throw new ISOException("Error reading " + filename, e);
        }
    }

    /**
     * Parse the field descriptions from an XML InputStream.
     *
     * <pre>
     * Uses the sax parser specified by the system property 'sax.parser'
     * The default parser is org.apache.crimson.parser.XMLReaderImpl
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
    @Override
    public void setLogger (Logger logger, String realm) {
        super.setLogger (logger, realm);
        if (fld != null) {
            for (int i=0; i<fld.length; i++) {
                if (fld[i] instanceof ISOMsgFieldPackager) {
                    Object o = ((ISOMsgFieldPackager)fld[i]).getISOMsgPackager();
                    if (o instanceof LogSource) {
                        ((LogSource)o).setLogger (logger, realm + "-fld-" + i);
                    }
                }
            }
        }
    }
    private XMLReader createXMLReader () throws SAXException {
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
        reader.setFeature ("http://xml.org/sax/features/validation", true);
        GenericContentHandler handler = new GenericContentHandler();
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);
        reader.setEntityResolver(new GenericEntityResolver());
        return reader;
    }
    @Override
    public String getDescription () {
        StringBuilder sb = new StringBuilder();
        sb.append (super.getDescription());
        if (filename != null) {
            sb.append ('[');
            sb.append (filename);
            sb.append (']');
        }
        return sb.toString();
    }

    protected void setGenericPackagerParams (Attributes atts)
    {
        String maxField  = atts.getValue("maxValidField");
        String emitBmap  = atts.getValue("emitBitmap");
        String bmapfield = atts.getValue("bitmapField");
        String thirdbmf  = atts.getValue("thirdBitmapField");
        firstField = atts.getValue("firstField");
        String headerLenStr = atts.getValue("headerLength");

        if (maxField != null)
            maxValidField = Integer.parseInt(maxField);

        if (emitBmap != null)
            emitBitmap = Boolean.valueOf(emitBmap);

        if (bmapfield != null)
            bitmapField = Integer.parseInt(bmapfield);

        // BBB TODO IDEA: should we check somewhere that fld[thirdBitmapField] instanceof ISOBitMapPackager?
        if (thirdbmf != null)
            try { setThirdBitmapField(Integer.parseInt(thirdbmf)); }
            catch (ISOException e)
            {   // BBB throwing unchecked exception in order not to change the method's contract
                // BBB (the parseInt's and valueOf's above are doing it anyway...)
                throw new IllegalArgumentException(e.getMessage());
            }

        if (firstField != null)
            Integer.parseInt (firstField);  // attempt to parse just to
                                            // force an exception if the
                                            // data is not correct.
        if (headerLenStr != null)
            setHeaderLength(Integer.parseInt(headerLenStr));
    }

    public static class GenericEntityResolver implements EntityResolver
    {
        /**
         * Allow the application to resolve external entities.
         * <p/>
         * The strategy we follow is:<p>
         * We first check whether the DTD points to a well defined URI,
         * and resolve to our internal DTDs.<p>
         *
         * If the systemId points to a file, then we attempt to read the
         * DTD from the filesystem, in case they've been modified by the user.
         * Otherwise, we fallback to the built-in DTDs inside jPOS.<p>
         *
         * @param publicId The public identifier of the external entity
         *                 being referenced, or null if none was supplied.
         * @param systemId The system identifier of the external entity
         *                 being referenced.
         * @return An InputSource object describing the new input source,
         *         or null to request that the parser open a regular
         *         URI connection to the system identifier.
         * @throws org.xml.sax.SAXException Any SAX exception, possibly
         *                                  wrapping another exception.
         * @throws java.io.IOException      A Java-specific IO exception,
         *                                  possibly the result of creating a new InputStream
         *                                  or Reader for the InputSource.
         * @see org.xml.sax.InputSource
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
        {
            if(systemId==null) return null;

            ClassLoader cl =Thread.currentThread().getContextClassLoader();
            cl = cl ==null?ClassLoader.getSystemClassLoader() : cl;

            if(systemId.equals("http://jpos.org/dtd/generic-packager-1.0.dtd"))
            {
                final URL resource = cl.getResource("org/jpos/iso/packager/genericpackager.dtd");
                return new InputSource(resource.toExternalForm());
            }
            else if(systemId.equals("http://jpos.org/dtd/generic-validating-packager-1.0.dtd"))
            {
                final URL resource = cl.getResource("org/jpos/iso/packager/generic-validating-packager.dtd");
                return new InputSource(resource.toExternalForm());
            }

            URL url=new URL(systemId);
            if(url.getProtocol().equals("file"))
            {
                String file=url.getFile();
                if(file.endsWith(".dtd"))
                {
                    File f=new File(file);
                    InputStream res=null;
                    if(f.exists())
                    {
                        res=new FileInputStream(f);
                    }
                    if(res==null)
                    {
                        String dtdResource="org/jpos/iso/packager/"+f.getName();
                        res= cl.getResourceAsStream(dtdResource);
                    }
                    if(res!=null) return new InputSource(res);
                }
            }
            return null;
        }
    }

    public class GenericContentHandler extends DefaultHandler
    {
        private Stack<Object> fieldStack;

        @Override
        public void startDocument()
        {
            fieldStack = new Stack<Object>();
        }

        @Override
        public void endDocument() throws SAXException
        {
            if (!fieldStack.isEmpty())
            {
                throw new SAXException ("Format error in XML Field Description File");
            }
        }

        @Override
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
                // Modified for using TaggedFieldPackager
                String token = atts.getValue("token");
                String trim = atts.getValue("trim");
                String params = atts.getValue("params");

                if (localName.equals("isopackager"))
                {
                    // Stick a new Map on stack to collect the fields
                    fieldStack.push(new TreeMap());

                    setGenericPackagerParams (atts);
                }

                if (localName.equals("isofieldpackager"))
                {
                    /*
                    For an isofield packager node push the following fields
                    onto the stack.
                    1) an Integer indicating the field ID
                    2) an instance of the specified ISOFieldPackager class
                    3) an instance of the specified ISOBasePackager (msgPackager) class
                    4) a Map to collect the subfields
                    */
                    String packager = atts.getValue("packager");

                    fieldStack.push(new Integer(id));

                    ISOFieldPackager f;
                    f = (ISOFieldPackager) Class.forName(type).newInstance();
                    f.setDescription(name);
                    f.setLength(Integer.parseInt(size));
                    f.setPad(Boolean.parseBoolean(pad));

                    // Modified for using TaggedFieldPackager
                    if( f instanceof TaggedFieldPackager){
                      ((TaggedFieldPackager)f).setToken( token );
                    }
                    fieldStack.push(f);

                    ISOBasePackager p = (ISOBasePackager) instantiate(packager, params);
                    if (p instanceof GenericPackager)
                    {
                        GenericPackager gp = (GenericPackager) p;
                        gp.setGenericPackagerParams (atts);
                    }
                    fieldStack.push(p);

                    fieldStack.push(new TreeMap());
                }
                else if (localName.equals("isofield"))
                {
                    Class c = Class.forName(type);
                    ISOFieldPackager f;
                    f = (ISOFieldPackager) instantiate(type, params);
                    f.setDescription(name);
                    f.setLength(Integer.parseInt(size));
                    f.setPad(Boolean.parseBoolean(pad));
                    f.setTrim(Boolean.parseBoolean(trim));
                    // Modified for using TaggedFieldPackager
                    if( f instanceof TaggedFieldPackager){
                      ((TaggedFieldPackager)f).setToken( token );
                    }
                    // Insert this new isofield into the Map
                    // on the top of the stack using the fieldID as the key
                    Map m = (Map) fieldStack.peek();
                    m.put(new Integer(id), f);
                }
            }
            catch (Exception ex)
            {
                throw new SAXException(ex);
            }
        }

        /**
         * Convert the ISOFieldPackagers in the Map
         * to an array of ISOFieldPackagers
         */
        private ISOFieldPackager[] makeFieldArray(Map<Integer,ISOFieldPackager> m)
        {
            int maxField = 0;

            // First find the largest field number in the Map
            for (Entry<Integer,ISOFieldPackager> ent :m.entrySet())
                if (ent.getKey() > maxField)
                    maxField = ent.getKey();

            // Create the array
            ISOFieldPackager fld[] = new ISOFieldPackager[maxField+1];

            // Populate it
            for (Entry<Integer,ISOFieldPackager> ent :m.entrySet())
               fld[ent.getKey()] = ent.getValue();
            return fld;
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
        {
            Map<Integer,ISOFieldPackager> m;
            if (localName.equals("isopackager"))
            {
                m  = (Map)fieldStack.pop();

                setFieldPackager(makeFieldArray(m));
            }

            if (localName.equals("isofieldpackager"))
            {
                // Pop the 4 entries off the stack in the correct order
                m = (Map)fieldStack.pop();

                ISOBasePackager msgPackager = (ISOBasePackager) fieldStack.pop();
                msgPackager.setFieldPackager (makeFieldArray(m));

                ISOFieldPackager fieldPackager = (ISOFieldPackager) fieldStack.pop();

                Integer fno = (Integer) fieldStack.pop();

                msgPackager.setLogger (getLogger(), getRealm() + "-fld-" + fno);

                // Create the ISOMsgField packager with the retrieved msg and field Packagers
                ISOMsgFieldPackager mfp =
                    new ISOMsgFieldPackager(fieldPackager, msgPackager);

                // Add the newly created ISOMsgField packager to the
                // lower level field stack

                m=(Map)fieldStack.peek();
                m.put(fno, mfp);
            }
        }

        // ErrorHandler Methods
        @Override
        public void error (SAXParseException ex) throws SAXException
        {
            throw ex;
        }

        @Override
        public void fatalError (SAXParseException ex) throws SAXException
        {
            throw ex;
        }
    }
    @Override
    protected int getFirstField() {
        if (firstField != null)
            return Integer.parseInt (firstField);
        else return super.getFirstField();
    }

    /**
     * Helper class used to instantiate packagers
     *
     * @param clazz class name
     * @param params If not null <code>constructor(String)</code> has to exist in packager implementation.
     *
     * @return newly created object
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Object instantiate (String clazz, String params)
      throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Object obj;
        if (params != null)
            obj = Class.forName(clazz).getConstructor(String.class).newInstance(params);
        else
            obj = Class.forName(clazz).newInstance();

        return obj;
    }
}
