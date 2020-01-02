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

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOBaseValidator;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOFieldValidator;
import org.jpos.iso.ISOMsgFieldPackager;
import org.jpos.iso.ISOMsgFieldValidator;
import org.jpos.iso.ISOValidator;
import org.jpos.iso.validator.ISOVException;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;


/**
 * Generic Packager that configure validators too.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class GenericValidatingPackager extends GenericPackager implements ISOValidator {

    public GenericValidatingPackager(  ) throws ISOException{
        super();
    }
    public GenericValidatingPackager( String fileName ) throws ISOException {
        super( fileName );
    }
    public GenericValidatingPackager (InputStream stream) throws ISOException {
        super (stream);
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

    /**
     * It define GenericValidatorContentHandler like handler.
     */
    public void readFile(String filename) throws org.jpos.iso.ISOException {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader(
                    System.getProperty( "sax.parser",
                    "org.apache.crimson.parser.XMLReaderImpl"));
            reader.setFeature ("http://xml.org/sax/features/validation", true);
            GenericValidatorContentHandler handler = new GenericValidatorContentHandler();
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            reader.setEntityResolver(new GenericEntityResolver());
            reader.parse(filename);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ISOException(e);
        }
    }

    protected void setGenericPackagerParams ( Attributes atts ) {
        String maxField  = atts.getValue( "maxValidField" );
        String emitBmap  = atts.getValue( "emitBitmap" );
        String bmapfield = atts.getValue( "bitmapField" );
        if ( maxField != null )
            maxValidField = Integer.parseInt( maxField );
        if ( emitBmap != null )
            emitBitmap = Boolean.valueOf(emitBmap);
        if ( bmapfield != null )
            bitmapField = Integer.parseInt( bmapfield );
    }

    public void setMsgValidator( ISOBaseValidator[] msgVlds ){
        this.mvlds = msgVlds;
    }

    public void setFieldValidator( ISOFieldValidator[] fvlds ){
        this.fvlds = fvlds;
    }

    public ISOComponent validate(ISOComponent m) throws ISOException {
        LogEvent evt = new LogEvent( this, "validate" );
        try {
            ISOComponent c;
            Map<Object,ISOComponent> fields = m.getChildren();
            /** Field  validations **/
            for (ISOValidator val :fvlds) {
                if ( (c=fields.get (((ISOFieldValidator) val).getFieldId())) != null ){
                    try {
                        m.set( val.validate( c ) );
                    } catch ( ISOVException e ) {
                        if ( !e.treated() ) {
                            m.set( e.getErrComponent() );
                            e.setTreated( true );
                        }
                        evt.addMessage( "Component Validation Error." );
                        throw e;
                    }
                }
            }
            /** msg validations **/
            try {
                for (ISOBaseValidator mval :mvlds)
                    m = mval.validate( m );
            }
            catch (ISOVException ex) {
                evt.addMessage( "Component Validation Error." );
                throw ex;
            }
            return m;
        }
        finally {
            Logger.log( evt );
        }
    }

/*  Values copied from ISOBasePackager
These can be changes using attributes on the isopackager node */
    protected  int maxValidField=128;
    protected boolean emitBitmap=true;
    protected int bitmapField=1;
    /** FieldValidator array. **/
    protected ISOValidator[] fvlds = {};
    /** MsgValidator array **/
    protected ISOBaseValidator[] mvlds = {};
    /** incr used to put validators in the same hashtable of
     *  fieldpackagers. packagers will stay on index 1, 2, 3...
     *  and validators in inc+1, inc+2, inc+3,... **/
    static final int inc = 500;


    @SuppressWarnings("unchecked")
    public class GenericValidatorContentHandler extends DefaultHandler {
        @Override
        public void startDocument(){
            fieldStack = new Stack<Object>();
            validatorStack = new Stack<Object>();
        }

        @Override
        public void endDocument() throws SAXException {
            if ( !fieldStack.isEmpty() )
                throw new SAXException ( "Format error in XML Field Description File" );
        }

        @Override
        public void startElement( String namespaceURI, String localName, String qName, Attributes atts )
                throws SAXException {
            try {
                if ( localName.equals( "isopackager" ) ) {
                    // Stick a new Map on stack to collect the fields
                    fieldStack.push( new TreeMap() );

                    /** used to insert msg-level validators **/
                    Map m = new TreeMap();
                    m.put(VALIDATOR_INDEX, new ArrayList() );

                    validatorStack.push( m );
                    setGenericPackagerParams ( atts );
                }
                if (localName.equals("isofield")){
                    /** getID global for isofieldvalidator **/
                    fldID = atts.getValue("id");
                    String type = atts.getValue("class");
                    String name = atts.getValue("name");
                    String size = atts.getValue("length");
                    String pad  = atts.getValue("pad");
                    Class c = Class.forName(type);
                    ISOFieldPackager f;
                    f = (ISOFieldPackager) c.newInstance();
                    f.setDescription(name);
                    f.setLength(Integer.parseInt(size));
                    f.setPad(Boolean.parseBoolean(pad));
                    // Insert this new isofield into the Map
                    // on the top of the stack using the fieldID as the key
                    Map m = (Map) fieldStack.peek();
                    m.put(new Integer(fldID), f);
                }
                if ( localName.equals( "isofieldvalidator" ) ){
                    String type = atts.getValue( "class" );
                    String breakOnError = atts.getValue( "break-on-error" );
                    String minLen = atts.getValue( "minlen" );
                    String maxLen = atts.getValue( "maxlen" );
                    Class c = Class.forName( type );
                    ISOFieldValidator v = (ISOFieldValidator)c.newInstance();
                    if ( breakOnError != null ) v.setBreakOnError(Boolean.valueOf(breakOnError));
                    if ( minLen != null ) v.setMinLength( Integer.parseInt( minLen ) );
                    if ( maxLen != null ) v.setMaxLength( Integer.parseInt( maxLen ) );
                    v.setFieldId( Integer.parseInt(fldID) );
                    /** insert validator on stack waiting for properties **/
                    validatorStack.push( v );
                    validatorStack.push( new Properties() );
                }
                if ( localName.equals( "property" ) ){
                    ((Properties)validatorStack.peek()).setProperty(
                            atts.getValue( "name" ),
                            atts.getValue( "value" ) );
                }
                if ( localName.equals( "isovalidator" ) ){
                    String type = atts.getValue( "class" );
                    String breakOnError = atts.getValue( "break-on-error" );
                    Class c = Class.forName( type );
                    ISOBaseValidator v = (ISOBaseValidator)c.newInstance();
                    if ( breakOnError != null ) v.setBreakOnError(Boolean.valueOf(breakOnError));
                    /** insert validator on stack waiting for properties **/
                    validatorStack.push( v );
                    validatorStack.push( new Properties() );
                }
                if ( localName.equals("isofieldpackager") ) {
                    String id   = atts.getValue("id");
                    String type = atts.getValue("class");
                    String name = atts.getValue("name");
                    String size = atts.getValue("length");
                    String pad  = atts.getValue("pad");
/*
For a isofield packager node push the following fields
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
                    fieldStack.push(f);
                    ISOBasePackager p;
                    p = (ISOBasePackager) Class.forName(packager).newInstance();
                    if (p instanceof GenericValidatingPackager){
                        GenericValidatingPackager gp = (GenericValidatingPackager) p;
                        gp.setGenericPackagerParams (atts);
                    }
                    fieldStack.push(p);
                    String validator = atts.getValue( "validator" );
                    ISOBaseValidatingPackager v;
                    v = (ISOBaseValidatingPackager) Class.forName(validator).newInstance();
                    validatorStack.push( v );
                    Map m = new TreeMap();
                    m.put(VALIDATOR_INDEX, new ArrayList() );
                    validatorStack.push( m );
                    fieldStack.push( new TreeMap() );
                }
            } catch (Exception ex){
                throw new SAXException(ex);
            }
        }

        /**
         * Convert the ISOFieldPackagers in the Map
         * to an array of ISOFieldPackagers
         */
        private ISOFieldPackager[] makeFieldPackArray(Map<Integer,ISOFieldPackager> m){
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
        public void endElement(String namespaceURI, String localName, String qName) {
            if (localName.equals("isopackager")){
                Map m  = (Map)fieldStack.pop();
                setFieldPackager( makeFieldPackArray(m) );
                m = (Map)validatorStack.pop();
                setFieldValidator ( makeFieldValidatorArray( m ));
                setMsgValidator( makeMsgValidatorArray( m ) );
            }
            if ( localName.equals( "isofieldvalidator" ) ){
                /** pop properties **/
                Properties p = (Properties)validatorStack.pop();
                SimpleConfiguration cfg = null;
                if ( !p.entrySet().isEmpty() )
                    cfg = new SimpleConfiguration( p );
                /** pop validator and add it to the hash **/
                ISOFieldValidator f = (ISOFieldValidator)validatorStack.pop();
                if ( cfg != null ){
                    try {
                        f.setConfiguration( cfg );
                    }
                    catch (ConfigurationException ex) {
                        ex.printStackTrace(  );
                    }
                }
                ((Map)validatorStack.peek()).put( new Integer(fldID), f );
            }
            if ( localName.equals( "isovalidator" ) ){
                /** pop properties **/
                Properties p = (Properties)validatorStack.pop();
                SimpleConfiguration cfg = null;
                if ( !p.entrySet().isEmpty() )
                    cfg = new SimpleConfiguration( p );
                /** pop validator and add it to the hash **/
                ISOBaseValidator v = (ISOBaseValidator)validatorStack.pop();
                if ( cfg != null ){
                    try {
                        v.setConfiguration( cfg );
                    }
                    catch (ConfigurationException ex) {
                        ex.printStackTrace(  );
                    }
                }
                /** add validator to the has **/
                ((List)((Map)validatorStack.peek()).get(VALIDATOR_INDEX)).add( v );
            }
            if (localName.equals("isofieldpackager")){
                // Pop the 4 entries off the stack in the correct order
                Map m = (Map)fieldStack.pop();
                ISOBasePackager msgPackager = (ISOBasePackager) fieldStack.pop();
                msgPackager.setFieldPackager (makeFieldArray(m));
                msgPackager.setLogger (getLogger(), "Generic Packager");
                ISOFieldPackager fieldPackager = (ISOFieldPackager) fieldStack.pop();
                Integer fno = (Integer) fieldStack.pop();
                // Create the ISOMsgField packager with the retrieved msg and field Packagers
                ISOMsgFieldPackager mfp =
                        new ISOMsgFieldPackager(fieldPackager, msgPackager);

                // Add the newly created ISOMsgField packager to the
                // lower level field stack
                m=(Map)fieldStack.peek();
                m.put(fno, mfp);
                Map val = (Map)validatorStack.pop();
                ISOBaseValidatingPackager v = (ISOBaseValidatingPackager) validatorStack.pop();
                v.setFieldValidator( makeFieldValidatorArray ( val ) );
                v.setMsgValidator( makeMsgValidatorArray ( val ) );
                ISOMsgFieldValidator mfv = new ISOMsgFieldValidator ( fieldPackager.getDescription(), v );
                mfv.setFieldId(fno);
                v.setLogger (getLogger(), "Generic validating Packager");
                m=(Map)validatorStack.peek();
                m.put(fno, mfv);
            }
        }

        ISOFieldValidator[] makeFieldValidatorArray ( Map<Integer,ISOFieldValidator> m ){
            List<ISOFieldValidator> l = new ArrayList();
            // Populate it
            for (Entry<Integer,ISOFieldValidator> ent :m.entrySet() )
                if ( ent.getKey() != VALIDATOR_INDEX )
                    l.add(ent.getValue());
            // Create the array
            return l.toArray(new ISOFieldValidator[l.size()]);
        }

        ISOBaseValidator[] makeMsgValidatorArray ( Map m ){
            // First find the count
            List<ISOBaseValidator> l = (List)m.get(VALIDATOR_INDEX);
            return l.toArray(new ISOBaseValidator[l.size()]);
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

        static final int VALIDATOR_INDEX = -3 ;
        private Stack<Object> fieldStack, validatorStack;
        private String fldID;

    }
}
