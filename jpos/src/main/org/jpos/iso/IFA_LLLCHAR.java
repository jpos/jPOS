package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len CHAR
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_LLLCHAR extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFA_LLLCHAR (int len, String description) {
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
	
		if ((len=s.length()) > getLen() || len>999)	// paranoia settings
			throw new ISOException (
			"invalid len "+len +" packing LLLCHAR field "+(Integer) c.getKey()); 
		System.out.println ("DEBUG len=" +
		          	ISOUtil.zeropad(Integer.toString(len), 3)+
					"string=" +s);

		return (ISOUtil.zeropad(Integer.toString(len), 3) + s).getBytes();
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
		System.out.println ("DEBUG offset=" +offset
		   +"string=" +new String(b, offset, 3));

		int len = Integer.parseInt(new String(b, offset, 3));

		System.out.println ("DEBUG len=" +len);
		c.setValue (new String (b, offset+3, len));
		return len + 3;
	}
}
