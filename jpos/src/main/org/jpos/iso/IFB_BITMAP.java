/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:14  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.util.*;

public class IFB_BITMAP extends ISOFieldPackager {
	public IFB_BITMAP(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		return ISOUtil.bitSet2byte ((BitSet) c.getValue());
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		BitSet bmap = ISOUtil.byte2BitSet (b, offset);
		c.setValue(bmap);
		return (bmap.size() >> 3);
	}
}
