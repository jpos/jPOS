/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:13  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFB_BINARY extends ISOFieldPackager {
	public IFB_BINARY(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		byte[] b = (byte[]) c.getValue();
		if (b.length != getLen()) 
			throw new ISOException (
				"invalid len "+b.length +" packing field "+(Integer) c.getKey()
				+" expected "+getLen()
			);
		return (byte[]) c.getValue();
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		byte[] value = new byte[getLen()];
	    System.arraycopy(b, offset, value, 0, getLen());
		c.setValue ((Object) value);
		return getLen();
	}
	public ISOComponent createComponent(int fieldNumber) {
		return new ISOBinaryField (fieldNumber);
	}
}
