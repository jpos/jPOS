package org.jpos.iso;

import java.io.*;
import java.util.*;
import org.jpos.iso.packager.XMLPackager;

/**
 * implements <b>Leaf</b> for binary fields
 *
 * See the
 * <a href="API_users_guide.html">API User's Guide</a>
 * for details.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class ISOBinaryField extends ISOComponent implements Cloneable {
    protected int fieldNumber;
    protected byte[] value;

    /**
     * @param n - the FieldNumber
     */
    public ISOBinaryField(int n) {
        fieldNumber = n;
    }
    /**
     * @param n - fieldNumber
     * @param v - fieldValue
     */
    public ISOBinaryField(int n, byte[] v) {
        fieldNumber = n;
        value = v;
    }
    /**
     * not available on Leaf - always throw ISOException
     * @exception ISOException
     */
    public byte[] pack() throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * not available on Leaf - always throw ISOException
     * @exception ISOException
     */
    public int unpack(byte[] b) throws ISOException {
        throw new ISOException ("Not available on Leaf");
    }
    /**
     * @return Object representing this field number
     */
    public Object getKey() {
        return new Integer(fieldNumber);
    }
    /**
     * @return Object representing this field value
     */
    public Object getValue() {
        return value;
    }
    /**
     * @param obj - Object representing this field value
     * @exception ISOException
     */
    public void setValue(Object obj) throws ISOException {
        value = (byte[]) obj;
    }
    /**
     * @return byte[] representing this field
     */
    public byte[] getBytes() {
	return value;
    }
    /**
     * dump this field to PrintStream. The output is sorta
     * XML, intended to be easily parsed.
     * @param p - print stream
     * @param indent - optional indent string
     */
    public void dump (PrintStream p, String indent) {
        p.println (indent +"<"+XMLPackager.ISOFIELD_TAG + " " +
	    XMLPackager.ID_ATTR +"=\"" +fieldNumber +"\" "+
	    XMLPackager.VALUE_ATTR +"=\"" +this.toString() + "\" " +
	    XMLPackager.TYPE_ATTR +"=\"" + XMLPackager.TYPE_BINARY + "\"/>"
	);
    }
    public String toString() {
        return ISOUtil.hexString(value);
    }
}
