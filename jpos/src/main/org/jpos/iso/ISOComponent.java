package org.jpos.iso;

import java.io.*;
import java.util.*;

/**
 * implements a <b>Component</b>
 * within a <b>Composite pattern</b>
 *
 * See 
 * <a href="/doc/javadoc/overview-summary.html">Overview</a> for details.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOField
 * @see ISOException
 */
public abstract class ISOComponent implements Cloneable {
    /**
     * Set a field within this message
     * @param c - a component
     * @exception ISOException
     */
    public void set (ISOComponent c) throws ISOException {
        throw new ISOException ("Can't add to Leaf");
    }
    /**
     * Unset a field
     * @param fldno - the field number
     * @exception ISOException
     */
    public void unset (int fldno) throws ISOException {
        throw new ISOException ("Can't remove from Leaf");
    }
    /**
     * In order to interchange <b>Composites</b> and <b>Leafs</b> we use
     * getComposite(). A <b>Composite component</b> returns itself and
     * a Leaf returns null. The base class ISOComponent provides
     * <b>Leaf</b> functionality.
     *
     * @return ISOComponent
     */
    public ISOComponent getComposite() {
        return null;
    }
    /**
     * valid on Leafs only.
     * The value returned is used by ISOMsg as a key
     * to this field.
     *
     * @return object representing the field number
     * @exception ISOException
     */
    public Object getKey() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * valid on Leafs only.
     * @return object representing the field value
     * @exception ISOException
     */
    public Object getValue() throws ISOException {
        throw new ISOException ("N/A in Composite");
    }
    /**
     * a Composite must override this function
     * @return the max field number associated with this message
     */
    public int getMaxField() {
        return 0;
    }
    /**
     * dummy behaviour - return 0 elements Hashtable
     * @return children (in this case 0 children)
     */
    public Hashtable getChildren() {
        return new Hashtable();
    }
    public abstract void setValue(Object obj) throws ISOException;
    public abstract byte[] pack() throws ISOException;
    public abstract int unpack(byte[] b) throws ISOException;
    public abstract void dump (PrintStream p, String indent);
}
