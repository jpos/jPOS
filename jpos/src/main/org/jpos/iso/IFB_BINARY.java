package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary Field
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

public class IFB_BINARY extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFB_BINARY(int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		byte[] b = (byte[]) c.getValue();
		if (b.length != getLength()) 
			throw new ISOException (
				"invalid len "+b.length +" packing field "+(Integer) c.getKey()
				+" expected "+getLength()
			);
		return (byte[]) c.getValue();
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
		byte[] value = new byte[getLength()];
	    System.arraycopy(b, offset, value, 0, getLength());
		c.setValue ((Object) value);
		return getLength();
	}
	public ISOComponent createComponent(int fieldNumber) {
		return new ISOBinaryField (fieldNumber);
	}
	public int getMaxPackedLength() {
		return getLength();
	}
}
