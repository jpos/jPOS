/**
 * ISOFieldPackager
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:26  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public abstract class ISOFieldPackager {
	private int len;
	private String description;
	public ISOFieldPackager(int len, String description) {
		this.len = len;
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public int getLen() {
		return len;
	}
	public ISOComponent createComponent(int fieldNumber) {
		return new ISOField (fieldNumber);
	}
	public abstract byte[] pack (ISOComponent c) throws ISOException;
	public abstract int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException;
}
