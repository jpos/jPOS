/**
 * ISOFieldPackager CHARACTERS (ASCII & BINARY)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:18  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public class IF_CHAR extends ISOFieldPackager {
	public IF_CHAR(int len, String description) {
		super(len, description);
	}
	public byte[] pack (ISOComponent c) throws ISOException {
		return (ISOUtil.strpad ((String) c.getValue(), getLen())).getBytes();
	}
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		c.setValue(new String(b, offset, getLen()));
		return getLen();
	}
}
