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

package org.jpos.util;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;

/**
 * General purpose, Field Separator delimited message.
 *
 * <h2>How to use</h2>
 * <p>
 * The message format (or schema) is defined in xml files containing a schema element, with an optional id attribute, and multiple
 * field elements. A field element is made up of the following attributes:
 * <dl>
 * <dt>id</dt>
 * <dd>The name of the field. This is used in calls to {@link FSDMsg#set(String, String)}. It should be unique amongst the fields in an FSDMsg.</dd>
 * <dt>length</dt>
 * <dd>The maximum length of the data allowed in this field. Fixed length fields will be padded to this length. A zero length is allowed, and can
 * be useful to define extra separator characters in the message.</dd>
 * <dt>type</dt>
 * <dd>The type of the included data, including an optional separator for marking the end of the field and the beginning of the next one. The data type
 * is defined by the first char of the type, and the separator is defined by the following chars. If a field separator is specified, then no
 * padding is done on values for this field.
 * </dd>
 * <dt>key</dt>
 * <dd>If this optional attribute has a value of "true", then fields from another schema, specified by the value, are appended to this schema.</dd>
 * <dt>separator</dt>
 * <dd>An optional attribute containing the separator for the field. This is the preferred method of specifying the separator. See the list of optional</dd>
 * </dl>
 * <p>
 * Possible types are:
 * <dl>
 * <dt>A</dt><dd>Alphanumeric. Padding if any is done with spaces to the right.</dd>
 * <dt>B</dt><dd>Binary. Padding, if any, is done with zeros to the left.</dd>
 * <dt>K</dt><dd>Constant. The value is specified by the field content. No padding is done.</dd>
 * <dt>N</dt><dd>Numeric. Padding, if any, is done with zeros to the left.</dd>
 * </dl>
 * <p>
 * Supported field separators are:
 * <dl>
 * <dt>FS</dt><dd>Field separator using '034' as the separator.</dd>
 * <dt>US</dt><dd>Field separator using '037' as the separator.</dd>
 * <dt>GS</dt><dd>Group separator using '035' as the separator.</dd>
 * <dt>RS</dt><dd>Row separator using '036' as the separator.</dd>
 * <dt>PIPE</dt><dd>Field separator using '|' as the separator.</dd>
 * <dt>EOF</dt><dd>End of File - no separator character is emitted, but also no padding is done. Also if the end of file is reached
 * parsing a message, then no exception is thrown.</dd>
 * <dt>DS</dt><dd>A dummy separator. This is similar to EOF, but the message stream must not end before it is allowed.</dd>
 * <dt>EOM</dt><dd>End of message separator. This reads all bytes available in the stream.
 * </dl>
 * <p>
 * Key fields allow you to specify a tree of possible message formats. The key fields are the fork points of the tree.
 * Multiple key fields are supported. It is also possible to have more key fields specified in appended schemas.
 * </p>
 *
 * @author Alejandro Revila
 * @author Mark Salter
 * @author Dave Bergert
 * @since 1.4.7
 */
@SuppressWarnings("unchecked")
public class FSDMsg implements Loggeable, Cloneable {
    /** Field Separator character (ASCII 0x1C). */
    public static char FS = '\034';
    /** Unit Separator character (ASCII 0x1F). */
    public static char US = '\037';
    /** Group Separator character (ASCII 0x1D). */
    public static char GS = '\035';
    /** Record Separator character (ASCII 0x1E). */
    public static char RS = '\036';
    /** End of File character (null byte). */
    public static char EOF = '\000';
    /** Pipe character (ASCII 0x7C). */
    public static char PIPE = '\u007C';
    /** End of Message marker (null byte). */
    public static char EOM = '\000';

    private static final Set<String> DUMMY_SEPARATORS = new HashSet<>(Arrays.asList("DS", "EOM"));
    private static final String EOM_SEPARATOR = "EOM";
    private static final int READ_BUFFER = 8192;

    Map<String,String> fields;
    Map<String, Character> separators;

    String baseSchema;
    String basePath;
    byte[] header;
    Charset charset;
    private int readCount;

    /**
     * Creates a FSDMsg with a specific base path for the message format schema.
     * @param basePath   schema path, for example: "file:src/data/NDC-" looks for a file src/data/NDC-base.xml
     */
    public FSDMsg (String basePath) {
        this (basePath, "base");
    }

    /**
     * Creates a FSDMsg with a specific base path for the message format schema, and a base schema name. For instance,
     * FSDMsg("file:src/data/NDC-", "root") will look for a file: src/data/NDC-root.xml
     * @param basePath   schema path
     * @param baseSchema schema name
     */
    public FSDMsg (String basePath, String baseSchema) {
        super();
        fields = new LinkedHashMap<>();
        separators = new LinkedHashMap<>();
        this.basePath   = basePath;
        this.baseSchema = baseSchema;
        charset = ISOUtil.CHARSET;
        readCount = 0;

        setSeparator("FS", FS);
        setSeparator("US", US);
        setSeparator("GS", GS);
        setSeparator("RS", RS);
        setSeparator("EOF", EOF);
        setSeparator("PIPE", PIPE);
    }
    /**
     * Returns the base schema path used to load field definitions.
     * @return the base schema path
     */
    public String getBasePath() {
        return basePath;
    }
    /**
     * Returns the base schema name used to load field definitions.
     * @return the base schema name
     */
    public String getBaseSchema() {
        return baseSchema;
    }

    /**
     * Sets the character set used for packing and unpacking string fields.
     * @param charset the character set to use
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Adds or overrides a separator type/char pair.
     * @param separatorName string identifier for the separator type (e.g. "FS", "US")
     * @param separator the character representing this separator
     */
    public void setSeparator(String separatorName, char separator) {
        separators.put(separatorName, separator);
    }

    /**
     * Removes a previously defined separator type.
     * @param separatorName string identifier for the separator type to remove
     * @throws IllegalArgumentException if the separator was not previously defined
     */
    public void unsetSeparator(String separatorName) {
        if (!separators.containsKey(separatorName))
            throw new IllegalArgumentException("unsetSeparator was attempted for "+
                      separatorName+" which was not previously defined.");

        separators.remove(separatorName);
    }

    /**
     * parse message. If the stream ends before the message is completely read, then the method adds an EOF field.
     *
     * @param is input stream
     *
     * @throws IOException
     * @throws JDOMException
     */
    public void unpack (InputStream is)
        throws IOException, JDOMException {
        try {
            if (is.markSupported())
                is.mark(READ_BUFFER);
            unpack (new InputStreamReader(is, charset), getSchema (baseSchema));
            if (is.markSupported()) {
                is.reset();
                is.skip (readCount);
                readCount = 0;
            }
        } catch (EOFException e) {
            if (!fields.isEmpty())
                fields.put ("EOF", "true");         // some fields were read, but unexpected EOF found
            else                                    // nothing new since last msg, fields were read; no more msgs from this stream
                throw e;                            // just rethrow the exception
        }
    }
    /**
     * parse message. If the stream ends before the message is completely read, then the method adds an EOF field.
     *
     * @param b message image
     *
     * @throws IOException on I/O error while reading the byte array
     * @throws JDOMException on schema parsing error
     */
    public void unpack (byte[] b)
        throws IOException, JDOMException {
        unpack (new ByteArrayInputStream (b));
    }

    /**
     * Packs this FSDMsg into its string representation.
     * @return the packed message string
     * @throws org.jdom2.JDOMException on schema parsing error
     * @throws java.io.IOException on I/O error
     * @throws ISOException on packing error
     */
    public String pack ()
        throws JDOMException, IOException, ISOException
    {
        StringBuilder sb = new StringBuilder ();
        pack (getSchema (baseSchema), sb);
        return sb.toString ();
    }
    /**
     * Packs this message into a byte array.
     * @return the packed bytes
     * @throws ISOException on pack error
     * @throws IOException on I/O error
     * @throws JDOMException on schema parse error
     */
    public byte[] packToBytes ()
        throws JDOMException, IOException, ISOException
    {
        return pack().getBytes(charset);
    }

    /**
     * Returns the formatted value for the named field.
     * @param id        field identifier
     * @param type      field type
     * @param length    field length
     * @param defValue  default value if field is absent
     * @param separator field separator
     * @return formatted field value
     * @throws ISOException on error
     */
	    protected String get (String id, String type, int length, String defValue, String separator)
        throws ISOException
    {
	    return get(id,type,length,defValue,separator,true);
    }
    /**
     * Returns the formatted value for the named field with optional unpadding.
     * @param id        field identifier
     * @param type      field type
     * @param length    field length
     * @param defValue  default value
     * @param separator field separator
     * @param unPad     if true, strip padding
     * @return formatted field value
     * @throws ISOException on error
     */
    protected String get (String id, String type, int length, String defValue, String separator, boolean unPad)
        throws ISOException
    {
        String value = fields.get (id);
        if (value == null)
            value = defValue == null ? "" : defValue;

        type = type.toUpperCase ();
        int lengthLength = 0;
        while (type.charAt(0) == 'L') {
            lengthLength++;
            type = type.substring(1);
        }

        switch (type.charAt (0)) {
            case 'N':
                if (!isSeparated(separator)) {
                    value = ISOUtil.zeropad (value, length);
                } // else Leave value unpadded.
                break;
            case 'A':
                if (!isSeparated(separator) && lengthLength == 0) {
                    value = ISOUtil.strpad (value, length);
                } // else Leave value unpadded.
                if (value.length() > length)
                    value = value.substring(0,length);
                break;
            case 'K':
                if (defValue != null)
                    value = defValue;
                break;
            case 'B':
                if (length << 1 < value.length())
                    throw new IllegalArgumentException("field content=" + value
                            + " is too long to fit in field " + id
                            + " whose length is " + length);

                if (isSeparated(separator)) {
                    // Convert but do not pad if this field ends with a
                    // separator
                    value = new String(ISOUtil.hex2byte(value), charset);
                } else {
                    value = new String(ISOUtil.hex2byte(ISOUtil.zeropad(
                            value, length << 1).substring(0, length << 1)), charset);
                }
                break;
        }

        if (lengthLength == 0 && (!isSeparated(separator) || isBinary(type) || EOM_SEPARATOR.equals(separator) ||(isSeparated(separator) && !unPad)))
          return value;
        else {
            if (lengthLength > 0) {
                String format = String.format("%%0%dd%%s", lengthLength);
                value = String.format(format, value.length(), value);
            } else {
                value = ISOUtil.blankUnPad(value);
            }
        }
        return value;
    }

    private boolean isSeparated(String separator) {
        /*
         * if type's last two characters appear in our Map of separators,
         * return true
         */
        if (separator == null)
            return false;
        else if (separators.containsKey (separator))
            return true;
        else if (isDummySeparator (separator))
            return true;
        else
            try {
                if (Character.isDefined(Integer.parseInt(separator,16))) {
                    setSeparator(separator, (char)Long.parseLong(separator,16));
                    return true;
                }
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Invalid separator '"+ separator + "'");
            }
        throw new IllegalArgumentException("isSeparated called on separator="+
                      separator+" which was not previously defined.");
    }

    private boolean isDummySeparator(String separator) {
        return DUMMY_SEPARATORS.contains(separator);
    }

    private boolean isBinary(String type) {
        /*
         * if type's first digit is a 'B' return true
         */
        return type.startsWith("B");
    }

    /**
     * Tests whether a byte matches any configured separator character.
     * @param b the byte to test
     * @return true if the byte corresponds to any configured separator
     */
    public boolean isSeparator(byte b) {
        return separators.containsValue((char) b);
    }

    private String getSeparatorType(String type) {
        if (type.length() > 2 && !(type.charAt(0) == 'L')) {
            return type.substring(1);
        }
        return null;
    }

    private char getSeparator(String separator) {
        if (separators.containsKey(separator))
            return separators.get(separator);
        else if (isDummySeparator (separator)) {
            // Dummy separator type, return 0 to indicate nothing to add.
            return 0;
        }

        throw new IllegalArgumentException("getSeparator called on separator="+
                      separator+" which was not previously defined.");
    }

    /**
     * Packs this message into the given StringBuilder using the provided schema.
     * @param schema the schema element
     * @param sb     the target StringBuilder
     * @throws ISOException on pack error
     * @throws IOException on I/O error
     * @throws JDOMException on schema error
     */
    protected void pack (Element schema, StringBuilder sb)
        throws JDOMException, IOException, ISOException
    {
        String keyOff = "";
        String defaultKey = "";
        for (Element elem : schema.getChildren("field")) {
            String id    = elem.getAttributeValue ("id");
            int length   = Integer.parseInt (elem.getAttributeValue ("length"));
            String type  = elem.getAttributeValue ("type");
            // For backward compatibility, look for a separator at the end of the type attribute, if no separator has been defined.
            String separator = elem.getAttributeValue ("separator");
            if (type != null && separator == null) {
            	separator = getSeparatorType (type);
            }
            boolean unPad = true;
            if (separator != null && elem.getAttributeValue("pack_unpad") != null) {
                unPad = Boolean.valueOf(elem.getAttributeValue("pack_unpad"));
            }
            boolean key  = "true".equals (elem.getAttributeValue ("key"));
            Map properties = key ? loadProperties(elem) : Collections.EMPTY_MAP;
            String defValue = elem.getText();
            // If properties were specified, then the defValue contains lots of \n and \t in it. It should just be set to the empty string, or null.
            if (!properties.isEmpty()) {
            	defValue = defValue.replace("\n", "").replace("\t", "").replace("\r", "");
            }
            String value = get (id, type, length, defValue, separator, unPad);
            sb.append (value);

            if (isSeparated(separator)) {
                char c = getSeparator(separator);
                if (c > 0)
                    sb.append(c);
            }
            if (key) {
                String v = isBinary(type) ? ISOUtil.hexString(value.getBytes(charset)) : value;
                keyOff = keyOff + normalizeKeyValue(v, properties);
                defaultKey += elem.getAttributeValue ("default-key");
            }
        }
        if (keyOff.length() > 0)
            pack (getSchema (getId (schema), keyOff, defaultKey), sb);
    }

    private Map loadProperties(Element elem) {
    	Map props = new HashMap ();
        for (Element prop : elem.getChildren ("property")) {
    		String name = prop.getAttributeValue ("name");
    		String value = prop.getAttributeValue ("value");
    		props.put (name, value);
    	}
	    return props;
    }

	  private String normalizeKeyValue(String value, Map<?,String> properties) {
    	if (properties.containsKey(value)) {
            return properties.get(value);
    	}
    	return ISOUtil.normalize(value);
    }

    /**
     * Unpacks a message from the reader using the provided schema.
     * @param r      the reader
     * @param schema the schema element
     * @throws IOException on I/O error
     * @throws JDOMException on schema error
     */
    protected void unpack (InputStreamReader r, Element schema)
        throws IOException, JDOMException {

        String keyOff = "";
        String defaultKey = "";
        for (Element elem : schema.getChildren("field")) {
            String id    = elem.getAttributeValue ("id");
            int length   = Integer.parseInt (elem.getAttributeValue ("length"));
            String type  = elem.getAttributeValue ("type").toUpperCase();
            String separator = elem.getAttributeValue ("separator");
            if (/* type != null && */       // can't be null or we would have NPE'ed when .toUpperCase()
                separator == null) {
            	separator = getSeparatorType (type);
            }
            boolean key  = "true".equals (elem.getAttributeValue ("key"));
            Map properties = key ? loadProperties(elem) : Collections.EMPTY_MAP;

            String value = readField(r, id, length, type, separator);

            if (key) {
                keyOff = keyOff + normalizeKeyValue(value, properties);
                defaultKey += elem.getAttributeValue ("default-key");
            }

            // constant fields should have read the constant value
            if ("K".equals(type) && !value.equals (elem.getText()))
                throw new IllegalArgumentException (
                    "Field "+id
                       + " value='"     +value
                       + "' expected='" + elem.getText () + "'"
                );
        }
        if (keyOff.length() > 0) {
            unpack(r, getSchema (getId (schema), keyOff, defaultKey));      // recursion
        }
    }
    private String getId (Element e) {
        String s = e.getAttributeValue ("id");
        return s == null ? "" : s;
    }
    /**
     * Reads a field value from the stream.
     * @param r         the reader
     * @param len       maximum field length
     * @param type      field type
     * @param separator field separator
     * @return the field value
     * @throws IOException on I/O error
     */
    protected String read (InputStreamReader r, int len, String type, String separator)
        throws IOException
    {
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1];
        boolean expectSeparator = isSeparated(separator);
        boolean separated = expectSeparator;
        char separatorChar= expectSeparator ? getSeparator(separator) : '\0';

        if (EOM_SEPARATOR.equals(separator)) {
            // Grab what's left.
            char[] rest = new char[32];
            int con;
            while ((con = r.read(rest, 0, rest.length)) >= 0) {
                readCount += con;
                if (rest.length == con)
                    sb.append(rest);
                else
                    sb.append(Arrays.copyOf(rest, con));
            }
        } else if (isDummySeparator(separator)) {
            /*
             * No need to look for a separator, that is not there! Try and take
             * len bytes from the stream.
             */
            for (int i = 0; i < len; i++) {
                if (r.read(c) < 0) {
                    break; // end of stream indicates end of field?
                }
                readCount++;
                sb.append(c[0]);
            }
        } else {
            int lengthLength = 0;
            if (type != null && type.startsWith("L")) {
                while (type.charAt(0) == 'L') {
                    lengthLength++;
                    type = type.substring(1);
                }
                if (lengthLength > 0) {
                    char[] ll = new char[lengthLength];
                    if (r.read(ll) != lengthLength)
                        throw new EOFException();
                    len = Integer.parseInt(new String(ll));
                }
            }
            for (int i = 0; i < len; i++) {
                if (r.read(c) < 0) {
                    if (!"EOF".equals(separator))
                        throw new EOFException();
                    else {
                        separated = false;
                        break;
                    }
                }
                readCount++;
                if (expectSeparator && c[0] == separatorChar) {
                    separated = false;
                    break;
                }
                sb.append(c[0]);
            }

            if (separated && !"EOF".equals(separator)) {
                // we still need to read the separator and account for it under readCount
                if (r.read(c) < 0) {
                    throw new EOFException();
                } else {
                    readCount++;
                    // BBB extra check, left commented out for now (we don't want to break existing code
//                    if (c[0] != separatorChar)
//                        throw new IOException("Separator '"+separatorChar+"' expected "+
//                              "but found character '"+c[0]+"' instead.");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Reads a named field value from the stream.
     * @param r         the reader
     * @param fieldName the field name
     * @param len       maximum field length
     * @param type      field type
     * @param separator field separator
     * @return the field value
     * @throws IOException on I/O error
     */
    protected String readField (InputStreamReader r, String fieldName, int len,
                                String type, String separator) throws IOException
    {
        String fieldValue = read (r, len, type, separator);

        if (isBinary(type))
            fieldValue = ISOUtil.hexString (fieldValue.getBytes (charset));
        fields.put (fieldName, fieldValue);
        // System.out.println ("++++ "+fieldName + ":" + fieldValue + " " + type + "," + isBinary(type));
        return fieldValue;
    }

    /**
     * Sets a field value; removes the field if value is null.
     * @param name the field name
     * @param value the field value, or null to remove
     */
    public void set (String name, String value) {
        if (value != null)
            fields.put (name, value);
        else
            fields.remove (name);
    }
    /**
     * Sets the binary header bytes for this message.
     * @param h the header bytes to set
     */
    public void setHeader (byte[] h) {
        this.header = h;
    }
    /**
     * Returns the binary header bytes.
     * @return the header bytes, or null if not set
     */
    public byte[] getHeader () {
        return header;
    }
    /**
     * Returns the header as a hex string, or empty string if no header is set.
     * @return the header as a hex string
     */
    public String getHexHeader () {
        return header != null ? ISOUtil.hexString (header).substring (2) : "";
    }
    /**
     * Returns the value of the named field.
     * @param fieldName the field name to retrieve
     * @return the field value, or null if not set
     */
    public String get (String fieldName) {
        return fields.get (fieldName);
    }
    /**
     * Returns the value of the named field, or a default if not set.
     * @param fieldName the field name
     * @param def the default value if not set
     * @return the field value, or the default
     */
    public String get (String fieldName, String def) {
        String s = fields.get (fieldName);
        return s != null ? s : def;
    }
    /**
     * Copies a field value from another FSDMsg into this message.
     * @param fieldName the field to copy
     * @param msg the source FSDMsg
     */
    public void copy (String fieldName, FSDMsg msg) {
        fields.put (fieldName, msg.get (fieldName));
    }
    /**
     * Copies a field value from another FSDMsg into this message, using a default if not present.
     * @param fieldName the field to copy
     * @param msg the source FSDMsg
     * @param def default value if the field is not present in msg
     */
    public void copy (String fieldName, FSDMsg msg, String def) {
        fields.put (fieldName, msg.get(fieldName, def));
    }
    /**
     * Returns the value of the named field decoded from hex.
     * @param name the field name containing a hex-encoded value
     * @return decoded bytes, or null if field not set
     */
    public byte[] getHexBytes (String name) {
        String s = get (name);
        return s == null ? null : ISOUtil.hex2byte (s);
    }
    /**
     * Returns the integer value of the named field, or 0 if absent or non-numeric.
     * @param name the field name
     * @return integer value of the field
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public int getInt (String name) {
        int i = 0;
        try {
            i = Integer.parseInt (get (name));
        } catch (Exception ignored) { }
        return i;
    }
    /**
     * Returns the integer value of the named field, or a default if absent or non-numeric.
     * @param name the field name
     * @param def the default value to return if the field is absent or non-numeric
     * @return integer value of the field or the default
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public int getInt (String name, int def) {
        int i = def;
        try {
            i = Integer.parseInt (get (name));
        } catch (Exception ignored) { }
        return i;
    }
    /**
     * Serializes this FSDMsg to a JDOM XML Element.
     * @return XML Element representing this message
     */
    public Element toXML () {
        Element e = new Element ("message");
        if (header != null) {
            e.addContent (
                new Element ("header")
                    .setText (getHexHeader ())
            );
        }
        for (String fieldName :fields.keySet()) {
            Element inner = new Element (fieldName);
            inner.addContent (ISOUtil.normalize (fields.get (fieldName)));
            e.addContent (inner);
        }
        return e;
    }
    /**
     * Returns the root schema Element for the base schema.
     * @return the root schema Element
     * @throws JDOMException on XML parsing error
     * @throws IOException on I/O error
     */
    protected Element getSchema ()
        throws JDOMException, IOException {
        return getSchema (baseSchema);
    }
    /**
     * Returns the root schema Element for the named message schema.
     * @param message the schema message name
     * @return the root schema Element
     * @throws JDOMException on XML parsing error
     * @throws IOException on I/O error
     */
    protected Element getSchema (String message)
        throws JDOMException, IOException {
        return getSchema (message, "", null);
    }
    /**
     * Returns the root schema Element located at the given path.
     * @param prefix    the schema path prefix
     * @param suffix    the schema path suffix
     * @param defSuffix the default suffix to use if the path is not found
     * @return the root schema Element
     * @throws JDOMException on XML parsing error
     * @throws IOException on I/O error
     */
    protected Element getSchema (String prefix, String suffix, String defSuffix)
        throws JDOMException, IOException {
        if (basePath == null)
            throw new NullPointerException("basePath can not be null");
        StringBuilder sb = new StringBuilder (basePath);
        sb.append (prefix);
        prefix = sb.toString(); // little hack, we'll reuse later with defSuffix
        sb.append (suffix);
        sb.append (".xml");
        String uri = sb.toString ();

        Space sp = SpaceFactory.getSpace();
        Element schema = (Element) sp.rdp (uri);
        if (schema == null) {
            schema = loadSchema(uri, defSuffix == null);
            if (schema == null && defSuffix != null) {
                sb = new StringBuilder (prefix);
                sb.append (defSuffix);
                sb.append (".xml");
                schema = loadSchema(sb.toString(), true);
            }
            sp.out (uri, schema);
        }
        return schema;
    }

    /**
     * Loads and parses the schema from the given URI.
     * @param uri     the URI of the schema resource to load
     * @param throwex if true, throw an exception if the resource is not found
     * @return the parsed root Element, or null if not found and throwex is false
     * @throws JDOMException on XML parsing error
     * @throws IOException on I/O error
     */
    protected Element loadSchema(String uri, boolean throwex)
        throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        if (uri.startsWith("jar:") && uri.length()>4) {
            InputStream is = schemaResouceInputStream(uri.substring(4));
            if (is == null && throwex)
                throw new FileNotFoundException(uri + " not found");
            else if (is != null)
                return builder.build(is).getRootElement();
            else
                return null;
        }

        URL url = new URL(uri);
        try {
            return builder.build(url).getRootElement();
        } catch (FileNotFoundException ex) {
            if (throwex)
                throw ex;
            return null;
        }
    }

    /**
     * Returns an InputStream for the given classpath resource.
     * @param resource the classpath resource path
     * @return the InputStream, or null if the resource is not found
     * @throws IOException on I/O error
     * @throws JDOMException on XML error
     */
    protected InputStream schemaResouceInputStream(String resource)
        throws JDOMException, IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        cl = cl==null ? ClassLoader.getSystemClassLoader() : cl;
        return cl.getResourceAsStream(resource);
    }

    /**
     * @return message's Map
     */
    /**
     * Returns the underlying fields map.
     * @return the fields map
     */
    public Map getMap () {
        return fields;
    }
    /**
     * Sets the underlying fields map.
     * @param fields the fields map to set
     */
    public void setMap (Map fields) {
        this.fields = fields;
    }

    @Override
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.println (indent + "<fsdmsg schema='" + basePath + baseSchema  + "'>");
        if (header != null) {
            append (p, "header", getHexHeader(), inner);
        }
        for (String f :fields.keySet())
            append (p, f, fields.get (f), inner);
        p.println (indent + "</fsdmsg>");
    }
    private void append (PrintStream p, String f, String v, String indent) {
        p.println (indent + f + ": '" + v + "'");
    }
    /**
     * Returns true if this message has a value for the named field.
     * @param fieldName the field name to check
     * @return true if the field is present
     */
    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    @Override
    public Object clone() {
        try {
            FSDMsg m = (FSDMsg) super.clone();
            m.fields = (Map) ((LinkedHashMap) fields).clone();
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    /**
     * Copies all fields from the given message into this message.
     * @param m the source message to merge from
     */
    public void merge (FSDMsg m) {
        for (Entry<String,String> entry: m.fields.entrySet())
             set (entry.getKey(), entry.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FSDMsg fsdMsg = (FSDMsg) o;
        return Objects.equals(fields, fsdMsg.fields) &&
          Objects.equals(separators, fsdMsg.separators) &&
          Objects.equals(baseSchema, fsdMsg.baseSchema) &&
          Objects.equals(basePath, fsdMsg.basePath) &&
          Arrays.equals(header, fsdMsg.header) &&
          Objects.equals(charset, fsdMsg.charset);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fields, separators, baseSchema, basePath, charset);
        result = 31 * result + Arrays.hashCode(header);
        return result;
    }
}
