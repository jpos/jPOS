package org.jpos.iso;

/**
 * ISOFieldPackager Binary Hex Fixed LLBINARY
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_LLHFBINARY extends ISOFieldPackager {
	/**
	 * @param len - field len
	 * @param description symbolic descrption
	 */
	public IFB_LLHFBINARY (int len, String description) {
		super(len, description);
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		int len = ((byte[]) c.getValue()).length;
	
		if (len > getLength() || len>99)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing field "+(Integer) c.getKey()
			);

		byte[] b = new byte[getLength() + 1];
		b[0] = (byte) len;
	    System.arraycopy(c.getValue(), 0, b, 1, len);
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
		int len = (int) b[offset] & 0xFF;
		byte[] value = new byte[len];
	    System.arraycopy(b, ++offset, value, 0, len);
		c.setValue ((Object) value);
		return getLength()+1;
	}
	public ISOComponent createComponent(int fieldNumber) {
		return new ISOBinaryField (fieldNumber);
	}
	public int getMaxPackedLength() {
		return getLength() + 1;
	}
}
