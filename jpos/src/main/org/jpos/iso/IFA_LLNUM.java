/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:12  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IFA_LLNUM extends ISOFieldPackager {
	public IFA_LLNUM(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		int len;
		String s = (String) c.getValue();
	
		if ((len=s.length()) > getLen() || len>99)	// paranoia settings
			throw new ISOException (
				"invalid len "+len +" packing LLNUM field "+(Integer) c.getKey()
			);

		return (ISOUtil.zeropad(Integer.toString(len), 2) + s).getBytes();
	}

	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		int len = Integer.parseInt(new String(b, offset, 2));
		c.setValue (new String (b, offset+2, len));
		return len + 2;
	}
}
