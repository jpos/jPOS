package org.jpos.iso;

import java.io.*;
import java.util.*;
import org.jpos.util.Loggeable;
import org.jpos.util.LogProducer;

/*
 * $Log$
 * Revision 1.18  2000/03/05 01:23:42  apr
 * Changed XMLdump to lowercase
 * Fixed bug in merge (we were not merging last field in an ISOMsg)
 *
 * Revision 1.17  2000/03/04 00:37:53  apr
 * Show fieldNumber on inner message dumps
 *
 * Revision 1.16  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.15  2000/01/11 01:24:47  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.14  1999/10/01 10:54:08  apr
 * Added merge method
 *
 */

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
public class ISOMsg extends ISOComponent implements Cloneable, Loggeable {
    protected Hashtable fields;
    protected int maxField;
    protected ISOPackager packager;
    protected boolean dirty, maxFieldDirty;
    protected int direction;
    protected byte[] header;
    protected int fieldNumber = -1;
    public static final int INCOMING = 1;
    public static final int OUTGOING = 2;

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
	p.print (indent + "<isomsg");
	switch (direction) {
	    case INCOMING:
		p.print (" direction=\"incoming\"");
		break;
	    case OUTGOING:
		p.print (" direction=\"outgoing\"");
		break;
	}
	if (fieldNumber != -1)
	    p.print (" fieldid=\""+fieldNumber +"\"");
	p.println (">");
	String newIndent = indent + "  ";
        for (int i=0; i<=maxField; i++)
            if ((c = (ISOComponent) fields.get (new Integer (i))) != null)
                c.dump (p, newIndent);
        p.println (indent + "</isomsg>");
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
     * Partially clone an ISOMsg
     * @param fields int array of fields to go
     * @return new ISOMsg instance
     */
    public Object clone(int[] fields) {
        try {
            ISOMsg m = (ISOMsg) super.clone();
            m.fields = new Hashtable();
            for (int i=0; i<fields.length; i++) {
                if (hasField(fields[i])) {
                    try {
                        m.set (getComponent(fields[i]));
                    } catch (ISOException e) { 
                        // it should never happen
                    }
                }
            }
            return (Object) m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * add all fields present on received parameter to this ISOMsg<br>
     * please note that received fields take precedence over 
     * existing ones (simplifying card agent message creation 
     * and template handling)
     * @param m ISOMsg to merge
     */
    public void merge (ISOMsg m) {
	for (int i=0; i<=m.getMaxField(); i++) 
	    try {
		if (m.hasField(i))
		    set (m.getComponent(i));
	    } catch (ISOException e) {
		// should never happen 
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
