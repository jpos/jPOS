/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.2  1998/12/11 14:06:24  apr
 * Added 'pad' parameter en 'IFB_[L*]NUM*' y 'IFB_AMOUNT'
 *
 * Revision 1.1  1998/11/09 23:40:17  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFB_LLNUM extends ISOFieldPackager {
	private boolean pad;
	public IFB_LLNUM(int len, String description, boolean pad) {
		super(len, description);
		this.pad = pad;
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		int len;
		String s = (String) c.getValue();
	
		if ((len=s.length()) > getLen() || len>99)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing LLNUM field "+(Integer) c.getKey()
			);

		byte[] bcd = ISOUtil.str2bcd (s, pad);
		byte[] b   = new byte[bcd.length + 1];
		byte[] l   = ISOUtil.str2bcd (Integer.toString(len), true);
		b[0] = l[0];
	    System.arraycopy(bcd, 0, b, 1, bcd.length);
		return b;
	}

	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		int len = ((b[offset] >> 4) & 0x0F) * 10 + (b[offset] & 0x0F);
		c.setValue (ISOUtil.bcd2str (b, offset+1, len, pad));
		return 1 + (++len >> 1);
	}
}
