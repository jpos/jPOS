/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:17  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFB_NUMERIC extends ISOFieldPackager {
	public IFB_NUMERIC(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = ISOUtil.zeropad ((String) c.getValue(), getLen());
		return ISOUtil.str2bcd (s, true);
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		c.setValue (ISOUtil.bcd2str (b, offset, getLen(), true));
		return ((getLen()+1) >> 1);
	}
}
