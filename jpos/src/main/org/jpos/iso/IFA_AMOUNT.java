package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager ASCII AMOUNT
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOFieldPackager
 */
public class IFA_AMOUNT extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFA_AMOUNT(int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = (String) c.getValue();
		return (s.substring(0,1) 
				+ISOUtil.zeropad(s.substring(1),getLen()-1)).getBytes();
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
		c.setValue(new String(b, offset, getLen()));
		return getLen();
	}
}
