package uy.com.cs.jpos.iso;

/**
 * base class for the various IF*.java Field Packagers
 * Implements "FlyWeight" pattern
 *
 * @author apr@cs.com.uy
 * @version $Id$
 *
 * @see IFA_AMOUNT
 * @see IFA_BINARY
 * @see IFA_BITMAP
 * @see IFA_LLCHAR
 * @see IFA_LLLCHAR
 * @see IFA_LLLNUM
 * @see IFA_LLNUM
 * @see IFA_NUMERIC
 * @see IFB_AMOUNT
 * @see IFB_BINARY
 * @see IFB_BITMAP
 * @see IFB_CHAR
 * @see IFB_LLCHAR
 * @see IFB_LLLCHAR
 * @see IFB_LLLNUM
 * @see IFB_LLNUM
 * @see IFB_NUMERIC
 * @see IF_CHAR
 */
public abstract class ISOFieldPackager {
	private int len;
	private String description;
	/**
	 * @param len - field Len
	 * @param description - details
	 */
	public ISOFieldPackager(int len, String description) {
		this.len = len;
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public int getLen() {
		return len;
	}
	public ISOComponent createComponent(int fieldNumber) {
		return new ISOField (fieldNumber);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public abstract byte[] pack (ISOComponent c) throws ISOException;

	/**
	 * @param c - the Component to unpack
	 * @param b - binary image
	 * @param offset - starting offset within the binary image
	 * @return consumed bytes
	 * @exception ISOException
	 */
	public abstract int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException;
}
