package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary LL Hex NUM
 * Almost the same as IFB_LLNUM but len is encoded as a binary
 * value. A len of 16 is encoded as 0x10 instead of 0x16
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_LLHNUM extends ISOFieldPackager {
	private boolean pad;
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFB_LLHNUM(int len, String description, boolean pad) {
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
	
		if ((len=s.length()) > getLength() || len>99)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing LLHNUM field "+(Integer) c.getKey()
			);

		byte[] bcd = ISOUtil.str2bcd (s, pad);
		byte[] b   = new byte[bcd.length + 1];
		b[0] = (byte) len;
	    System.arraycopy(bcd, 0, b, 1, bcd.length);
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
		int len = (int) b[offset] & 0xFF;
		c.setValue (ISOUtil.bcd2str (b, offset+1, len, pad));
		return 1 + (++len >> 1);
	}
	public int getMaxPackedLength() {
		return 1 + ((getLength()+1) >> 1);
	}
}
