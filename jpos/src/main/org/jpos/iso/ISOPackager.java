package uy.com.cs.jpos.iso;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public interface ISOPackager {
	/**
	 * @param	m	the Component to pack
	 * @return		Message image
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent m) throws ISOException;

	/**
	 * @param	m	the Container of this message
	 * @param	b	ISO message image
	 * @return		consumed bytes
	 * @exception ISOException
	 */
	public int unpack (ISOComponent m, byte[] b) throws ISOException;
}
