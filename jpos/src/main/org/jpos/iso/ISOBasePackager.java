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


/*
 * $Log$
 * Revision 1.13  1999/08/06 13:55:46  apr
 * Added support for Bitmap-less ISOMsgs (usually nested messages)
 *
 */

public abstract class ISOBasePackager implements ISOPackager {
    protected ISOFieldPackager[] fld;

    protected void setFieldPackager (ISOFieldPackager[] fld) {
        this.fld = fld;
    }
    /**
     * @param   m   the Component to pack
     * @return      Message image
     * @exception ISOException
     */
    public byte[] pack (ISOComponent m) throws ISOException {
        if (m.getComposite() != m) 
            throw new ISOException ("Can't call packager on non Composite");

        ISOComponent c;
        Vector v = new Vector();
        Hashtable fields = m.getChildren();
        int len = 0;

        // MTI (field 0)
        c = (ISOComponent) fields.get (new Integer (0));
        byte[] b = fld[0].pack(c);
        len += b.length;
        v.addElement (b);

        boolean hasBitMap = (fld[1] instanceof ISOBitMapPackager);

        if (hasBitMap) {
            // BITMAP (-1 in HashTable)
            c = (ISOComponent) fields.get (new Integer (-1));
            b = getBitMapfieldPackager().pack(c);
            len += b.length;
            v.addElement (b);
        }

        // if Field 1 is a BitMap then we are packing an
        // ISO-8583 message so next field is fld#2.
        // else we are packing an ANSI X9.2 message, first field is 1
        for (int i=hasBitMap ? 2 : 1;
            i<=m.getMaxField(); i++)
        {
            if ((c = (ISOComponent) fields.get (new Integer (i))) != null) {
                b = fld[i].pack(c);
                len += b.length;
                v.addElement (b);
            }
        }
        int k = 0;
        byte[] d = new byte[len];
        for (int i=0; i<v.size(); i++) {
            b = (byte[]) v.elementAt(i);
            for (int j=0; j<b.length; j++)
                d[k++] = b[j];
        }
        return d;
    }

    /**
     * @param   m   the Container of this message
     * @param   b   ISO message image
     * @return      consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent m, byte[] b) throws ISOException {
        if (m.getComposite() != m) 
            throw new ISOException ("Can't call packager on non Composite");

        int consumed;
        ISOField mti     = new ISOField (0);
        ISOBitMap bitmap = new ISOBitMap (-1);
        consumed  = fld[0].unpack(mti, b, 0);
        m.set (mti);

        if (fld[1] instanceof ISOBitMapPackager) {
            consumed += getBitMapfieldPackager().unpack(bitmap, b, consumed);
            BitSet bmap = (BitSet) bitmap.getValue();
            m.set (bitmap);
            for (int i=2; i<bmap.size(); i++) {
                if (bmap.get(i)) {
                    ISOComponent c = fld[i].createComponent(i);
                    consumed += fld[i].unpack (c, b, consumed);
                    m.set(c);
                }
            }
        }
        else {
            for (int i=1; i<fld.length; i++) {
                ISOComponent c = fld[i].createComponent(i);
                consumed += fld[i].unpack (c, b, consumed);
                m.set(c);
            }
        }
        if (b.length != consumed) {
            // System.out.println (
            //  "Warning: unpack len=" +b.length +" consumed=" +consumed
            // );
            // throw new ISOException (
            //  "unpack error len=" +b.length +" consumed=" +consumed
            // );
        }
        return consumed;
    }
    /**
     * @param   m   the Container (i.e. an ISOMsg)
     * @param   fldNumber the Field Number
     * @return  Field Description
     * @exception ISOException
     */
    public String getFieldDescription(ISOComponent m, int fldNumber)
    {
        return fld[fldNumber].getDescription();
    }
    /**
     * @return 128 for ISO-8583, should return 64 for ANSI X9.2
     */
    protected int getMaxValidField() {
        return 128;
    }
    /**
     * @return suitable ISOFieldPackager for Bitmap
     */
    protected ISOFieldPackager getBitMapfieldPackager() {
        return fld[1];
    }
}
