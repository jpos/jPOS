/**
 * ISOFieldPackager ASCII AMOUNT
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

public class IFA_AMOUNT extends ISOFieldPackager {
	public IFA_AMOUNT(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		String s = (String) c.getValue();
		return (s.substring(0,1) 
				+ISOUtil.zeropad(s.substring(1),getLen()-1)).getBytes();
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		c.setValue(new String(b, offset, getLen()));
		return getLen();
	}
}
