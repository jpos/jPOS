/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.2  1998/12/11 14:06:21  apr
 * Added 'pad' parameter en 'IFB_[L*]NUM*' y 'IFB_AMOUNT'
 *
 * Revision 1.1  1998/11/09 23:40:13  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFB_AMOUNT extends ISOFieldPackager {
	private boolean pad;
	public IFB_AMOUNT(int len, String description, boolean pad) {
		super(len, description);
		this.pad = pad;
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = (String) c.getValue();
		String amnt = ISOUtil.zeropad(s.substring(1),getLen()-1);
		byte[] bcd = ISOUtil.str2bcd (amnt, pad);
		byte[] b   = new byte[bcd.length + 1];
		b[0] = (byte) s.charAt(0);
	    System.arraycopy(bcd, 0, b, 1, bcd.length);
		return b;
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		String d = (new String(b, offset, 1)) 
					+ISOUtil.bcd2str (b, offset+1, getLen()-1, pad);
		c.setValue(d);
		return 1 + ((getLen()+1) >> 1);
	}
}
