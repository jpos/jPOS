package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary Hex EBCDIC LLCHAR
 * IFB_LLHCHAR with EBCDIC conversion
 * (VISA's SMS field 54, additional amounts)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see IFB_LLHCHAR
 * @see IF_ECHAR
 */
public class IFB_LLHECHAR extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFB_LLHECHAR (int len, String description) {
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
		b[0] = (byte) len;
	    System.arraycopy(ISOUtil.asciiToEbcdic(s), 0, b, 1, len);
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
		int len = Math.min ((int) b[offset] & 0xFF, getLen());
		c.setValue(ISOUtil.ebcdicToAscii(b, ++offset, len));
		return ++len;
	}
}
