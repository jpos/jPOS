package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary Amount
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_AMOUNT extends ISOFieldPackager {
	private boolean pad;
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFB_AMOUNT(int len, String description, boolean pad) {
		super(len, description);
		this.pad = pad;
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = (String) c.getValue();
		String amnt = ISOUtil.zeropad(s.substring(1),getLength()-1);
		byte[] bcd = ISOUtil.str2bcd (amnt, pad);
		byte[] b   = new byte[bcd.length + 1];
		b[0] = (byte) s.charAt(0);
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
		String d = (new String(b, offset, 1)) 
					+ISOUtil.bcd2str (b, offset+1, getLength()-1, pad);
		c.setValue(d);
		return 1 + ((getLength()+1) >> 1);
	}
	public int getMaxPackedLength() {
		return 1 + ((getLength()+1) >> 1);
	}
}
