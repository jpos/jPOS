package uy.com.cs.jpos.iso;

/**
 * ISOMsgFieldPackager is a packager able to pack compound ISOMsgs
 * (one message inside another one, and so on...)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see PostPackager
 * @see PostPrivatePackager
 */
public class ISOMsgFieldPackager extends ISOFieldPackager {
	protected ISOPackager msgPackager;
	protected ISOFieldPackager fieldPackager;
	/**
	 * @param fieldPackager low level field packager
	 * @param msgPackager ISOMsgField default packager
	 */
	public ISOMsgFieldPackager (
			ISOFieldPackager fieldPackager,
			ISOPackager msgPackager)
	{
		super(fieldPackager.getLen(), fieldPackager.getDescription());
		this.msgPackager = msgPackager;
		this.fieldPackager = fieldPackager;
	}
	/**
	 * @param c - a component
	 * @return packed component
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent c) throws ISOException {
		ISOMsg m = (ISOMsg) c;
		m.recalcBitMap();
		ISOBinaryField f = new ISOBinaryField(0, msgPackager.pack(m));
		return fieldPackager.pack(f);
	}

	/**
	 * @param c - the Component to unpack
	 * @param b - binary image
	 * @param offset - starting offset within the binary image
	 * @return consumed bytes
	 * @exception ISOException
	 */
	public int unpack (ISOComponent c, byte[] b, int offset)
		throws ISOException
	{
		ISOBinaryField f = new ISOBinaryField(0);
		int consumed = fieldPackager.unpack(f, b, offset);
		msgPackager.unpack((ISOMsg) c, (byte[]) f.getValue());
		return consumed;
	}
	public ISOComponent createComponent(int fieldNumber) {
		ISOMsg m = new ISOMsg(fieldNumber);
		m.setPackager(msgPackager);
		return m;
	}
}
