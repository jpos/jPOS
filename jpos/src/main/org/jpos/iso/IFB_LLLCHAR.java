/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:15  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFB_LLLCHAR extends ISOFieldPackager {
	public IFB_LLLCHAR (int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		int len;
		String s = (String) c.getValue();
	
		if ((len=s.length()) > getLen() || len>999)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing field "+(Integer) c.getKey()
			);

		byte[] b = new byte[len + 2];
		String slen = ISOUtil.zeropad(Integer.toString(len),3);
		byte[] l = ISOUtil.str2bcd (slen, true);
		b[0] = l[0];
		b[1] = l[1];
	    System.arraycopy(s.getBytes(), 0, b, 2, len);
		return b;
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		int len = b[offset++] & 0x0F;
		len += ((b[offset] >> 4) & 0x0F) * 10 + b[offset] & 0x0F;
		c.setValue(new String(b, ++offset, len));
		return len+2;
	}
}
