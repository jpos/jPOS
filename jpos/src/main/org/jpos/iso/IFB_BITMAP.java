package uy.com.cs.jpos.iso;

import java.util.*;

/**
 * ISOFieldPackager Binary Bitmap
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class IFB_BITMAP extends ISOBitMapPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_BITMAP(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        return ISOUtil.bitSet2byte ((BitSet) c.getValue());
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        int len;
        BitSet bmap = ISOUtil.byte2BitSet (b, offset, true);
        c.setValue(bmap);
        len = ((len=bmap.size()) > 128) ? 128 : len;
        return (len >> 3);
    }
    public int getMaxPackedLength() {
        return getLength() >> 3;
    }
}
