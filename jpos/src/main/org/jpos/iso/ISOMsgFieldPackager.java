package uy.com.cs.jpos.iso;

/**
 * ISOMsgFieldPackager is a packager associated with
 * an ISOMsgField. As oposed to a standard ISOField, an
 * ISOMsgField allows compound ISOMsg (an ISOMsg within
 * anotherone).
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsgField
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
		if (!(c instanceof ISOMsgField)) 
			throw new ISOException(
				"ISOMsgFieldPackager: can't pack non ISOMsgField");

		ISOMsg m = (ISOMsg) ((ISOMsgField) c).getValue();
		m.recalcBitMap();
		ISOBinaryField f = new ISOBinaryField(0);
		f.setValue(msgPackager.pack(m));
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
		if (!(c instanceof ISOMsgField)) 
			throw new ISOException(
				"ISOMsgFieldPackager: can't unpack non ISOMsgField");

		ISOBinaryField f = new ISOBinaryField(0);
		int consumed = fieldPackager.unpack(f, b, offset);
		msgPackager.unpack((ISOMsg) c.getValue(), (byte[]) f.getValue());
		return consumed;
	}
	public ISOComponent createComponent(int fieldNumber) {
		ISOMsg m = new ISOMsg();
		m.setPackager(msgPackager);
		return new ISOMsgField (fieldNumber, m);
	}
}
