/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:11  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFA_LLLCHAR extends ISOFieldPackager {
	public IFA_LLLCHAR (int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		int len;
		String s = (String) c.getValue();
	
		if ((len=s.length()) > getLen() || len>999)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing LLLCHAR field "+(Integer) c.getKey()
			);

		return (ISOUtil.zeropad(Integer.toString(len), 3) + s).getBytes();
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		int len = Integer.parseInt(new String(b, offset, 3));
		c.setValue (new String (b, offset+3, len));
		return len + 3;
	}
}
