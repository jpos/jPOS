/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:07  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.util.*;

public class IFA_BITMAP extends ISOFieldPackager {
	public IFA_BITMAP(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		byte[] b = ISOUtil.bitSet2byte ((BitSet) c.getValue());
		return ISOUtil.hexString(b).getBytes();
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		BitSet bmap = ISOUtil.hex2BitSet (b, offset);
		c.setValue(bmap);
		return (bmap.size() >> 2);
	}
}
