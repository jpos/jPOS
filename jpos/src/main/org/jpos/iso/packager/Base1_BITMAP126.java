package org.jpos.iso.packager;

import org.jpos.iso.*;
import java.util.*;

/**
 * ISOFieldPackager Binary Bitmap
 *
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class Base1_BITMAP126 extends ISOBitMapPackager 
{
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public Base1_BITMAP126(int len, String description) 
    {
        super(len, description);
    }
    /**
    * @param c - a component
    * @return packed component
    * @exception ISOException
    */
    public byte[] pack (ISOComponent c) throws ISOException 
    {
        return ISOUtil.bitSet2byte ((BitSet) c.getValue());
    }
    /**
    * @param c - the Component to unpack
    * @param b - binary image
    * @param offset - starting offset within the binary image
    * @return consumed bytes
    * @exception ISOException
    */
    public int unpack (ISOComponent c, byte[] b, int offset) throws ISOException
    {
        int len;
        // 
        // For a this type of Bitmap bit0 does not mean
        // that there is a secondary bitmap to follow
        // It simply means that field 1 is present
        // The standard IFB_BITMAP class assumes that
        // bit0 always means extended bitmap 
        //
        BitSet bmap = ISOUtil.byte2BitSet (b, offset, false); // False => no extended bitmap

        c.setValue(bmap);
        len = ((len=bmap.size()) > 128) ? 128 : len;
        return (len >> 3);
    }
    public int getMaxPackedLength() 
    {
        return getLength() >> 3;
    }
}
