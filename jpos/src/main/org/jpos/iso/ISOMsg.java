/**
 * ISOMsg implementa <b>Composite</b>
 * dentro de un <b>Composite pattern</b>
 *
 * @see "Design Patterns ISBN 0-201-63361-2"
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:28  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;

public class ISOMsg extends ISOComponent implements Cloneable {
	protected Hashtable fields;
	protected int maxField;
	protected ISOPackager packager;
	protected boolean dirty;

	public ISOMsg () {
		fields = new Hashtable ();
		maxField = -1;
		dirty = true;
	}
	public int getMaxField() {
		return maxField;
	}
	public void setPackager (ISOPackager p) {
		packager = p;
	}
	public ISOPackager getPackager () {
		return packager;
	}
	public void set (ISOComponent c) throws ISOException {
		Integer i = (Integer) c.getKey();
		fields.put (i, c);
		if (i.intValue() > maxField)
			maxField = i.intValue();
		dirty = true;
	}
	public void unset (int fldno) throws ISOException {
		ISOComponent c = (ISOComponent) fields.remove (new Integer (fldno));
		if (c == null)
			throw new ISOException ("Field " +fldno +" not found. unset failed");
		dirty = true;
	}
	public ISOComponent getComposite() {
		return this;
	}

	public void recalcBitMap () throws ISOException {
		if (!dirty)
			return;

		ISOComponent c;
        BitSet bmap = new BitSet (getMaxField() > 64 ? 128 : 64);
		for (int i=0; i<=maxField; i++)
			if ((c = (ISOComponent) fields.get (new Integer (i))) != null)
				if (i > 1)
					bmap.set (i);

        if (getMaxField() > 64)
            bmap.set(0);
		else
			bmap.clear(0);

		set (new ISOBitMap (1, bmap));
		dirty = false;
	}
	public Hashtable getChildren() {
		return (Hashtable) fields.clone();
	}
	public byte[] pack() throws ISOException {
		recalcBitMap();
		return packager.pack(this);
	}
	public int unpack(byte[] b) throws ISOException {
		return packager.unpack(this, b);
	}
	public void dump (PrintStream p, String indent) {
		ISOComponent c;
		p.println (indent + "<ISOMsg>");
		for (int i=0; i<=maxField; i++)
			if ((c = (ISOComponent) fields.get (new Integer (i))) != null)
				c.dump (p, indent + " ");
		p.println (indent + "</ISOMsg>");
	}
	public ISOComponent getComponent(int fldno) {
		return (ISOComponent) fields.get(new Integer(fldno));
	}
	public Object getValue(int fldno) throws ISOException {
		return getComponent(fldno).getValue();
	}
	public boolean hasField(int fldno) {
		return fields.get(new Integer(fldno)) != null;
	}
	public void setValue(Object obj) throws ISOException {
		throw new ISOException ("setValue N/A in ISOMsg");
	}

	public Object clone() {
		try {
			ISOMsg m = (ISOMsg) super.clone();
			m.fields = (Hashtable) fields.clone();
			return (Object) m;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	public static void main (String args[]) {
		try {
			ISOMsg m = new ISOMsg ();
			ISOPackager packager = new ISO87APackager ();
			m.setPackager (packager);
			m.set(new ISOField (0,  "0200"));
			m.set(new ISOField (2,  "1234")); // LLNUM

			m.set(new ISOField (3,  "000001"));
			m.set(new ISOField (4,  "0000001000"));
			m.set(new ISOField (11, "000001"));
			m.set(new ISOField (30, "C100"));
			m.set(new ISOField (34, "TEST"));	// LLCHAR
			m.set(new ISOField (36, "5678")); // Prueba LLLCHAR
			m.set(new ISOField (37, "RETREFNBR")); // Prueba CHAR
			m.set(new ISOBinaryField (52, "ABCDEFGH".getBytes())); // BINARY 
			m.set(new ISOField (95, "1234"));
			m.dump(System.out, "");
			byte[] b = m.pack();
			System.out.println (
				"<ISOMsg hexdump>"+ISOUtil.hexString(b) +"</ISOMsg>"
			);
			ISOMsg d = new ISOMsg ();
			d.setPackager (packager);
			System.out.println ("message=" + new String(b));
			d.unpack (b);
			d.dump(System.out, "");
		} catch (ISOException e) {
			e.printStackTrace();
		}
	}
}
