package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;

/**
 * implements <b>Composite</b>
 * whithin a <b>Composite pattern</b>
 *
 * See the
 * <a href="API_users_guide.html">API User's Guide</a>
 * for details.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOField
 */
public class ISOMsg extends ISOComponent implements Cloneable {
	protected Hashtable fields;
	protected int maxField;
	protected ISOPackager packager;
	protected boolean dirty, maxFieldDirty;
	protected int direction;
	protected byte[] header;
	protected int fieldNumber = -1;
	public static int INCOMING = 1;
	public static int OUTGOING = 2;

	public ISOMsg () {
		fields = new Hashtable ();
		maxField = -1;
		dirty = true;
		maxFieldDirty=true;
		direction = 0;
		header = null;
	}
	public ISOMsg (int fieldNumber) {
		this();
		this.fieldNumber = fieldNumber;
	}
	/**
	 * Sets the direction information related to this message
	 * @param direction can be either ISOMsg.INCOMING or ISOMsg.OUTGOING
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}
	/**
	 * Sets an optional message header image
	 * @param b header image
	 */
	 public void setHeader(byte[] b) {
	 	header = b;
	 }
	/**
	 * get optional message header image
	 * @return message header image (may be null)
	 */
	 public byte[] getHeader() {
	 	return header;
	 }
	/**
	 * @return the direction (ISOMsg.INCOMING or ISOMsg.OUTGOING)
	 * @see ISOChannel
	 */
	public int getDirection() {
		return direction;
	}
	/**
	 * @return true if this message is an incoming message
	 * @see ISOChannel
	 */
	public boolean isIncoming() {
		return direction == INCOMING;
	}
	/**
	 * @return true if this message is an outgoing message
	 * @see ISOChannel
	 */
	public boolean isOutgoing() {
		return direction == OUTGOING;
	}
	/**
	 * @return the max field number associated with this message
	 */
	public int getMaxField() {
		if (maxFieldDirty)
			recalcMaxField();
		return maxField;
	}
	private void recalcMaxField() {
		ISOComponent c;
		maxField = 0;
		for (int i=1; i<=128; i++)
			if ((c = (ISOComponent) fields.get (new Integer (i))) != null)
				maxField = i;
		maxFieldDirty = false;
	}
	/**
	 * @param p - a peer packager
	 */
	public void setPackager (ISOPackager p) {
		packager = p;
	}
	/**
	 * @return the peer packager
	 */
	public ISOPackager getPackager () {
		return packager;
	}
	/**
	 * Set a field within this message
	 * @param c - a component
	 * @exception ISOException
	 */
	public void set (ISOComponent c) throws ISOException {
		Integer i = (Integer) c.getKey();
		fields.put (i, c);
		if (i.intValue() > maxField)
			maxField = i.intValue();
		dirty = true;
	}
	/**
	 * Unset a field
	 * @param fldno - the field number
	 * @exception ISOException
	 */
	public void unset (int fldno) throws ISOException {
		ISOComponent c = (ISOComponent) fields.remove (new Integer (fldno));
		if (c == null)
			throw new ISOException ("Field " +fldno +" not found. unset failed");
		dirty = true;
		maxFieldDirty = true;
	}
	/**
	 * In order to interchange <b>Composites</b> and <b>Leafs</b> we use
	 * getComposite(). A <b>Composite component</b> returns itself and
	 * a Leaf returns null.
	 *
	 * @return ISOComponent
	 */
	public ISOComponent getComposite() {
		return this;
	}
	/**
	 * setup BitMap
	 * @exception ISOException
	 */
	public void recalcBitMap () throws ISOException {
		if (!dirty)
			return;

		ISOComponent c;
        BitSet bmap = new BitSet (getMaxField() > 64 ? 128 : 64);

		for (int i=1; i<=maxField; i++)
			if ((c = (ISOComponent) fields.get (new Integer (i))) != null) 
				bmap.set (i);
		set (new ISOBitMap (-1, bmap));
		dirty = false;
	}
	/**
	 * clone fields
	 */
	public Hashtable getChildren() {
		return (Hashtable) fields.clone();
	}
	/**
	 * pack the message with the current packager
	 * @return the packed message
	 * @exception ISOException
	 */
	public byte[] pack() throws ISOException {
		recalcBitMap();
		return packager.pack(this);
	}
	/**
	 * unpack a message
	 * @param b - raw message
	 * @return consumed bytes
	 * @exception ISOException
	 */
	public int unpack(byte[] b) throws ISOException {
		return packager.unpack(this, b);
	}
	/**
	 * dump the message to a PrintStream. The output is sorta
	 * XML, intended to be easily parsed.
	 * <br>
	 * Each component is responsible for its own dump function,
	 * ISOMsg just calls dump on every valid field.
	 * @param p - print stream
	 * @param indent - optional indent string
	 */
	public void dump (PrintStream p, String indent) {
		ISOComponent c;
		p.println (indent + "<ISOMsg>");
		for (int i=0; i<=maxField; i++)
			if ((c = (ISOComponent) fields.get (new Integer (i))) != null)
				c.dump (p, indent + " ");
		p.println (indent + "</ISOMsg>");
	}
	/**
	 * get the component associated with the given field number
	 * @param fldno the Field Number
	 * @return the Component
	 */
	public ISOComponent getComponent(int fldno) {
		return (ISOComponent) fields.get(new Integer(fldno));
	}
	/**
	 * Return the object value associated with the given field number
	 * @param fldno the Field Number
	 * @return the field Object
	 */
	public Object getValue(int fldno) throws ISOException {
		return getComponent(fldno).getValue();
	}
	/**
	 * Check if a given field is valid
	 * @param fldno the Field Number
	 * @return boolean indicating the existence of the field
	 */
	public boolean hasField(int fldno) {
		return fields.get(new Integer(fldno)) != null;
	}
	/**
	 * Don't call setValue on an ISOMsg. You'll sure get
	 * an ISOException. It's intended to be used on Leafs
	 * @see ISOField
	 * @see ISOException
	 */
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

	/**
	 * @return a string suitable for a log
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		if (isIncoming())
			s.append("<-- ");
		else if (isOutgoing())
			s.append("--> ");
		else
			s.append("    ");

		try {
			s.append((String) getValue(0));
			if (hasField(11)) {
				s.append(' ');
				s.append((String) getValue(11));
			}
			if (hasField(41)) {
				s.append(' ');
				s.append((String) getValue(41));
			}
		} catch (ISOException e) { }
		return s.toString();
	}
	public Object getKey() throws ISOException {
		if (fieldNumber != -1)
			return new Integer(fieldNumber);
		throw new ISOException ("This is not a subField");
	}
	public Object getValue() {
		return this;
	}
}
