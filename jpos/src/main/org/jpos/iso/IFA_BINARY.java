/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:06  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFA_BINARY extends ISOFieldPackager {
	public IFA_BINARY(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		byte[] b = (byte[]) c.getValue();
		if (b.length != getLen()) 
			throw new ISOException (
				"invalid len "+b.length +" packing field "+(Integer) c.getKey()
				+" expected "+getLen()
			);
		return ISOUtil.hexString( (byte[]) c.getValue() ).getBytes();
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		c.setValue (ISOUtil.hex2byte(b, offset, getLen()));
		return getLen() << 1;
	}
	public ISOComponent createComponent(int fieldNumber) {
		return new ISOBinaryField (fieldNumber);
	}
}
