package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;

/*
 * $Log$
 * Revision 1.5  1999/11/18 23:33:41  apr
 * Bugfix to bug introduced on 1.4 with setValue and intern() when obj == null
 *
 * Revision 1.4  1999/10/01 19:20:27  apr
 * Added String.intern() in order to minimize memory usage
 *
 */

/**
 * implements <b>Leaf</b> for standard fields
 *
 * See the
 * <a href="API_users_guide.html">API User's Guide</a>
 * for details.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class ISOField extends ISOComponent implements Cloneable {
    protected int fieldNumber;
    protected String value;

    /**
     * @param n - the FieldNumber
     */
    public ISOField (int n) {
        fieldNumber = n;
    }
    /**
     * @param n - fieldNumber
     * @param v - fieldValue
     */
    public ISOField (int n, String v) {
        fieldNumber = n;
        value = v.intern();
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
	if (obj instanceof String)
	    value = ((String) obj).intern();
	else
	    value = (String) obj;
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
