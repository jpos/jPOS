package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager ASCII NUMERIC
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_NUMERIC extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFA_NUMERIC(int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = ISOUtil.zeropad ((String) c.getValue(), getLength());
		return s.getBytes();
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
		c.setValue(new String(b, offset, getLength()));
		return getLength();
	}
	public int getMaxPackedLength() {
		return getLength();
	}
}
