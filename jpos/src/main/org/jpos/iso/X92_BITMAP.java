package uy.com.cs.jpos.iso;

import java.util.*;

/**
 * ASCII packaged Bitmap
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class X92_BITMAP extends ISOBitMapPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public X92_BITMAP(int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		byte[] b = ISOUtil.bitSet2byte ((BitSet) c.getValue());
		return ISOUtil.hexString(b).getBytes();
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
		BitSet bmap = ISOUtil.hex2BitSet (b, offset, false);
		c.setValue(bmap);
		return (bmap.size() >> 2);
	}
	public int getMaxPackedLength() {
		return getLength() >> 2;
	}
}
