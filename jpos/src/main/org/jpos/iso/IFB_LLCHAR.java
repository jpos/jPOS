package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary LLCHAR
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_LLCHAR extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFB_LLCHAR (int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		int len;
		String s = (String) c.getValue();
	
		if ((len=s.length()) > getLen() || len>99)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing field "+(Integer) c.getKey()
			);

		byte[] b = new byte[len + 1];
		byte[] l = ISOUtil.str2bcd (Integer.toString(len), true);
		b[0] = l[0];
	    System.arraycopy(s.getBytes(), 0, b, 1, len);
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
		int len = (((b[offset] >> 4) & 0x0F) * 10) + (b[offset] & 0x0F);
		c.setValue(new String(b, ++offset, len));
		return ++len;
	}
}
