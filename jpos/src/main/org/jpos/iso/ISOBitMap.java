package org.jpos.iso;

import java.io.*;
import java.util.*;

/**
 * implements <b>Leaf</b> for Bitmap field
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class ISOBitMap extends ISOComponent implements Cloneable {
    protected int fieldNumber;
    protected BitSet value;

    /**
     * @param n - the FieldNumber
     */
    public ISOBitMap (int n) {
        fieldNumber = n;
    }
    /**
     * @param n - fieldNumber
     * @param v - field value (Bitset)
     * @see BitSet
     */
    public ISOBitMap (int n, BitSet v) {
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
        value = (BitSet) obj;
    }
    /**
     * dump this field to PrintStream. The output is sorta
     * XML, intended to be easily parsed.
     * @param p - print stream
     * @param indent - optional indent string
     */
    public void dump (PrintStream p, String indent) {
        p.println (indent +"<fld id=\"" 
            +fieldNumber +"\" value=\"" +value +"\"/>");
    }
}
