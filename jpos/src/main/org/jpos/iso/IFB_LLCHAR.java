/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.2  1998/12/14 22:48:20  apr
 * Added RawChannel support
 * Pruebas OK packaging POSNet
 *
 * Revision 1.1  1998/11/09 23:40:15  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFB_LLCHAR extends ISOFieldPackager {
	public IFB_LLCHAR (int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		int len;
		String s = (String) c.getValue();
	
		if ((len=s.length()) > getLen() || len>99)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing field "+(Integer) c.getKey()
			);

		byte[] b = new byte[len + 1];
		byte[] l = ISOUtil.str2bcd (Integer.toString(len), true);
		b[0] = l[0];
	    System.arraycopy(s.getBytes(), 0, b, 1, len);
		return b;
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		int len = (((b[offset] >> 4) & 0x0F) * 10) + (b[offset] & 0x0F);
		c.setValue(new String(b, ++offset, len));
		return ++len;
	}
}
