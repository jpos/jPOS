package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager ASCII Binary
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_BINARY extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFA_BINARY(int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		byte[] b = (byte[]) c.getValue();
		if (b.length != getLen()) 
			throw new ISOException (
				"invalid len "+b.length +" packing field "+(Integer) c.getKey()
				+" expected "+getLen()
			);
		return ISOUtil.hexString( (byte[]) c.getValue() ).getBytes();
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
		c.setValue (ISOUtil.hex2byte(b, offset, getLen()));
		return getLen() << 1;
	}
	/**
	 * component factory
	 * @param fieldNumber - the field number
	 * @return the newly created component
	 */
	public ISOComponent createComponent(int fieldNumber) {
		return new ISOBinaryField (fieldNumber);
	}
}
