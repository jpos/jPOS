package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary LLLNUM
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

public class IFB_LLLNUM extends ISOFieldPackager {
	private boolean pad;
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFB_LLLNUM(int len, String description, boolean pad) {
		super(len, description);
		this.pad = pad;
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		int len;
		String s = (String) c.getValue();
	
		if ((len=s.length()) > getLen() || len>999)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing LLNUM field "+(Integer) c.getKey()
			);

		byte[] bcd = ISOUtil.str2bcd (s, pad);
		byte[] b   = new byte[bcd.length + 2];
		String slen = ISOUtil.zeropad(Integer.toString(len),3);
		byte[] l   = ISOUtil.str2bcd (slen, true);
		b[0] = l[0];
		b[1] = l[1];
	    System.arraycopy(bcd, 0, b, 2, bcd.length);
		return b;
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
		int len = (b[offset++] & 0x0F) * 100;
		len += (((b[offset] >> 4) & 0x0F) * 10) + (b[offset] & 0x0F);
		c.setValue (ISOUtil.bcd2str (b, offset+1, len, pad));
		return 2 + (++len >> 1);
	}
}
