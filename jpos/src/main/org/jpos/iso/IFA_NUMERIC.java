/**
 * ISOFieldPackager ASCII Numeric
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

public class IFA_NUMERIC extends ISOFieldPackager {
	public IFA_NUMERIC(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = ISOUtil.zeropad ((String) c.getValue(), getLen());
		return s.getBytes();
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		c.setValue(new String(b, offset, getLen()));
		return getLen();
	}
}
