/**
 * ISOPackager
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:32  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public interface ISOPackager {
	/**
	 * @param	m	the Component to pack
	 * @return		Message image
	 */
	public byte[] pack (ISOComponent m) throws ISOException;

	/**
	 * @param	m	the Container of this message
	 * @param	b	ISO message image
	 * @return		consumed bytes
	 */
	public int unpack (ISOComponent m, byte[] b) throws ISOException;
}
