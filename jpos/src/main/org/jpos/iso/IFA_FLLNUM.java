package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len padded (fixed) NUMERIC
 * (suitable to use in ANSI X9.2 interchanges.
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see IFA_LLNUM
 */
public class IFA_FLLNUM extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFA_FLLNUM(int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = (String) c.getValue();
		int len;
	
		if ((len=s.length()) > getLen() || len>99)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing FLLNUM field "
				+(Integer) c.getKey()
			);

		s = ISOUtil.strpad(s, getLen());
		return (ISOUtil.zeropad(Integer.toString(getLen()), 2) + s).getBytes();
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
		int len = Integer.parseInt(new String(b, offset, 2));
		c.setValue (new String (b, offset+2, len));
		return getLen() + 2;
	}
}
