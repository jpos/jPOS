/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.2  1998/12/11 14:06:25  apr
 * Added 'pad' parameter en 'IFB_[L*]NUM*' y 'IFB_AMOUNT'
 *
 * Revision 1.1  1998/11/09 23:40:17  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFB_NUMERIC extends ISOFieldPackager {
	private boolean pad;
	public IFB_NUMERIC(int len, String description, boolean pad) {
		super(len, description);
		this.pad = pad;
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = ISOUtil.zeropad ((String) c.getValue(), getLen());
		return ISOUtil.str2bcd (s, pad);
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		c.setValue (ISOUtil.bcd2str (b, offset, getLen(), pad));
		return ((getLen()+1) >> 1);
	}
}
