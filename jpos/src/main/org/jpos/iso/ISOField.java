package org.jpos.iso;

import java.io.*;
import java.util.*;

/*
 * $Log$
 * Revision 1.11  2000/04/07 01:07:17  apr
 * Added setFieldNumber() method
 *
 * Revision 1.10  2000/04/06 16:45:19  apr
 * Added getBytes() when possible as suggested
 * by Eoin Flood <eoin.flood@orbiscom.com>
 *
 * Revision 1.9  2000/04/06 12:31:03  apr
 * XML normalize
 *
 * Revision 1.8  2000/03/20 21:56:39  apr
 * DocBugFix: broken links to API_users_guide
 *
 * Revision 1.7  2000/03/05 01:56:41  apr
 * Take XML tag and attributes names from constants in XMLPackager
 *
 * Revision 1.6  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
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
     * @return byte[] representing this field
     */
    public byte[] getBytes() {
	return value.getBytes();
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
	    XMLPackager.VALUE_ATTR
	    +"=\"" +ISOUtil.normalize (value) +"\"/>");
    }
    /**
     * changes this Component field number<br>
     * Use with care, this method does not change
     * any reference held by a Composite.
     * @param fieldNumber new field number
     */
    public void setFieldNumber (int fieldNumber) {
	this.fieldNumber = fieldNumber;
    }
}
