package uy.com.cs.jpos.iso;

import java.util.*;

/**
 * provides base functionality for the actual packagers
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISO87APackager
 * @see ISO87BPackager
 */
public abstract class ISOBasePackager implements ISOPackager {
	protected ISOFieldPackager[] fld;

	protected void setFieldPackager (ISOFieldPackager[] fld) {
		this.fld = fld;
	}
	/**
	 * @param	m	the Component to pack
	 * @return		Message image
	 * @exception ISOException
	 */
	public byte[] pack (ISOComponent m) throws ISOException {
		if (m.getComposite() != m) 
			throw new ISOException ("Can't call packager on non Composite");

		ISOComponent c;
        Vector v = new Vector();
		Hashtable fields = m.getChildren();
		int len = 0;

        for (int i=0; i<=m.getMaxField(); i++) {
            if ((c = (ISOComponent) fields.get (new Integer (i))) != null) {
				byte[] b = fld[i].pack(c);
				len += b.length;
                v.addElement (b);
            }
		}
        for (int i=0; i<v.size(); i++) {
			byte[] b = (byte[]) v.elementAt(i);
            String s = ISOUtil.hexString (b);
        }
		int k = 0;
		byte[] d = new byte[len];
        for (int i=0; i<v.size(); i++) {
			byte[] b = (byte[]) v.elementAt(i);
			for (int j=0; j<b.length; j++)
				d[k++] = b[j];
		}
		return d;
	}

	/**
	 * @param	m	the Container of this message
	 * @param	b	ISO message image
	 * @return		consumed bytes
	 * @exception ISOException
	 */
	public int unpack (ISOComponent m, byte[] b) throws ISOException {
		if (m.getComposite() != m) 
			throw new ISOException ("Can't call packager on non Composite");

		int consumed;
		ISOField mti     = new ISOField (0);
		ISOBitMap bitmap = new ISOBitMap (1);
		consumed  = fld[0].unpack(mti, b, 0);
		consumed += fld[1].unpack(bitmap, b, consumed);
		BitSet bmap = (BitSet) bitmap.getValue();
		m.set (mti);
		m.set (bitmap);

		for (int i=2; i<=bmap.size(); i++) {
			if (bmap.get(i)) {
				ISOComponent c = fld[i].createComponent(i);
				consumed += fld[i].unpack (c, b, consumed);
				if (c instanceof ISOField) {
					System.out.println (i + ":" + consumed + ":"+(String)
						  c.getValue() + ":");
				}
				else {
					System.out.println (i + ":" + consumed + "<BINARY>");
				}
				m.set(c);
			}
		}
		if (b.length != consumed) {
			System.out.println (
				"Warning: unpack len=" +b.length +" consumed=" +consumed
			);
			// throw new ISOException (
			//	"unpack error len=" +b.length +" consumed=" +consumed
			// );
		}
		return consumed;
	}
	/**
	 * @param	m	the Container (i.e. an ISOMsg)
	 * @param	fldNumber the Field Number
	 * @return	Field Description
	 * @exception ISOException
	 */
	public String getFieldDescription(ISOComponent m, int fldNumber)
	{
		return fld[fldNumber].getDescription();
	}
}
