package uy.com.cs.jpos.iso;

import uy.com.cs.jpos.iso.*;

/**
 * @author apr@cs.com.uy & dflc@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see IFA_LLNUM
 */
public class IF_NOP extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IF_NOP () {
        super(0, "<dummy>");
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        return null;
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent c, byte[] b, int offset) {
        return 0;
    }
    public int getMaxPackedLength() {
        return 0;
    }
}
