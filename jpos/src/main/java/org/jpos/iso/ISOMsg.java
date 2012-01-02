/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso;

import org.jpos.iso.header.BaseHeader;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Loggeable;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * implements <b>Composite</b>
 * whithin a <b>Composite pattern</b>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOField
 */
public class ISOMsg extends ISOComponent 
    implements Cloneable, Loggeable, Externalizable
{
    protected Map<Integer,Object> fields;
    protected int maxField;
    protected ISOPackager packager;
    protected boolean dirty, maxFieldDirty;
    protected int direction;
    protected ISOHeader header;
    protected int fieldNumber = -1;
    public static final int INCOMING = 1;
    public static final int OUTGOING = 2;
    private static final long serialVersionUID = 4306251831901413975L;
    private WeakReference sourceRef;

    /**
     * Creates an ISOMsg
     */
    public ISOMsg () {
        fields = new TreeMap<Integer,Object>();
        maxField = -1;
        dirty = true;
        maxFieldDirty=true;
        direction = 0;
        header = null;
    }
    /**
     * Creates a nested ISOMsg
     * @param fieldNumber (in the outter ISOMsg) of this nested message
     */
    public ISOMsg (int fieldNumber) {
        this();
        setFieldNumber (fieldNumber);
    }
    /**
     * changes this Component field number<br>
     * Use with care, this method does not change
     * any reference held by a Composite.
     * @param fieldNumber new field number
     */
    public void setFieldNumber (int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }
    /**
     * Creates an ISOMsg with given mti
     * @param mti Msg's MTI
     */
    public ISOMsg (String mti) {
        this();
        try {
            setMTI (mti);
        } catch (ISOException e) {
            // should never happen
        }
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
        header = new BaseHeader (b);
    }

    public void setHeader (ISOHeader header) {
        this.header = header;
    }
    /**
     * get optional message header image
     * @return message header image (may be null)
     */
    public byte[] getHeader() {
        return (header != null) ? header.pack() : null;
    } 

    /**
     * Return this messages ISOHeader
     * @return header associated with this ISOMsg, can be null
     */
    public ISOHeader getISOHeader() {
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
        maxField = 0;
        for (Object obj : fields.keySet()) {
            if (obj instanceof Integer)
                maxField = Math.max(maxField, ((Integer) obj).intValue());
        }
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
     */
    public void set (ISOComponent c) throws ISOException {
        if (c != null) {
            Integer i = (Integer) c.getKey();
            fields.put (i, c);
            if (i > maxField)
                maxField = i;
            dirty = true;
        }
    }
   /**
    * Creates an ISOField associated with fldno within this ISOMsg
    * @param fldno field number
    * @param value field value
    * @throws ISOException on error
    */
    public void set(int fldno, String value) throws ISOException {
        if (value != null) {
            if (!(packager instanceof ISOBasePackager)) {
                // No packager is available, we can't tell what the field
                // might be, so treat as a String!
                set(new ISOField(fldno, value));
            }
            else {
                // This ISOMsg has a packager, so use it
                Object obj = ((ISOBasePackager) packager).getFieldPackager(fldno);
                if (obj instanceof ISOBinaryFieldPackager) {
                    set(new ISOBinaryField(fldno, ISOUtil.hex2byte(value)));
                } else {
                    set(new ISOField(fldno, value));
                }
            }
        }
        else
            unset(fldno);
    }

   /**
    * Creates an ISOField associated with fldno within this ISOMsg
    * @param fpath dot-separated field path (i.e. 63.2)
    * @param value field value
    * @throws ISOException on error
    */
    public void set (String fpath, String value) throws ISOException {
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        for (;;) {
            int fldno = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
                Object obj = m.getValue(fldno);
                if (obj instanceof ISOMsg)
                    m = (ISOMsg) obj;
                else
                    /* 
                     * we need to go deeper, however, if the value == null then
                     * there is nothing to do (unset) at the lower levels, so break now and save some processing.
                     */
                    if (value == null) {
                        break;
                    } else {
                        // We have a value to set, so adding a level to hold it is sensible.
                        m.set (m = new ISOMsg (fldno));
                    }
            } else {
                m.set (fldno, value);
                break;
            }
        }
    }
    
    /**
     * Creates an ISOField associated with fldno within this ISOMsg
     * @param fpath dot-separated field path (i.e. 63.2)
     * @param c component
     * @throws ISOException on error
     */
     public void set (String fpath, ISOComponent c) throws ISOException {
         StringTokenizer st = new StringTokenizer (fpath, ".");
         ISOMsg m = this;
         for (;;) {
             int fldno = Integer.parseInt(st.nextToken());
             if (st.hasMoreTokens()) {
                 Object obj = m.getValue(fldno);
                 if (obj instanceof ISOMsg)
                     m = (ISOMsg) obj;
                 else
                     /* 
                      * we need to go deeper, however, if the value == null then
                      * there is nothing to do (unset) at the lower levels, so break now and save some processing.
                      */
                     if (c == null) {
                         break;
                     } else {
                         // We have a value to set, so adding a level to hold it is sensible.
                         m.set (m = new ISOMsg (fldno));
                     }
             } else {
                 m.set (c);
                 break;
             }
         }
     }
    
   
   /**
    * Creates an ISOField associated with fldno within this ISOMsg
    * @param fpath dot-separated field path (i.e. 63.2)
    * @param value binary field value
    * @throws ISOException on error
    */
    public void set (String fpath, byte[] value) throws ISOException {
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        for (;;) {
            int fldno = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
                Object obj = m.getValue(fldno);
                if (obj instanceof ISOMsg)
                    m = (ISOMsg) obj;
                else
                    m.set (m = new ISOMsg (fldno));
            } else {
                m.set (fldno, value);
                break;
            }
        }
    }
   /**
    * Creates an ISOBinaryField associated with fldno within this ISOMsg
    * @param fldno field number
    * @param value field value
    * @throws ISOException on error
    */
    public void set (int fldno, byte[] value) throws ISOException {
        if (value != null)
            set (new ISOBinaryField (fldno, value));
        else
            unset (fldno);
    }


    /**
     * Unset a field if it exists, otherwise ignore.
     * @param fldno - the field number
     */
    public void unset (int fldno) {
        if (fields.remove (fldno) != null)
            dirty = maxFieldDirty = true;
    }
    /**
     * Unsets several fields at once
     * @param flds - array of fields to be unset from this ISOMsg
     */
    public void unset (int[] flds) {
        for (int fld : flds)
            unset(fld);
    }
    /**
     * Unset a field referenced by a fpath if it exists, otherwise ignore.
     * @param fpath dot-separated field path (i.e. 63.2)
     * @throws ISOException on error
    */
    public void unset (String fpath) throws ISOException {
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        ISOMsg lastm = m;
        int fldno = -1 ;
        int lastfldno ;
        for (;;) {
            lastfldno = fldno;
            fldno = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
                Object obj = m.getValue(fldno);
                if (obj instanceof ISOMsg) {
                    lastm = m;
                    m = (ISOMsg) obj;
                }
                else {
                    // No real way of unset further subfield, exit.  Perhaps should be ISOException?
                    break;
                }
            } else {
                m.unset(fldno);
                if (!m.hasFields() && lastfldno != -1) {
                    lastm.unset(lastfldno);
                }
                break;
            }
        }
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
     * @exception ISOException on error
     */
    public void recalcBitMap () throws ISOException {
        if (!dirty)
            return;

        int mf = Math.min (getMaxField(), 192);

        BitSet bmap = new BitSet (((mf+62)>>6)<<6);
        for (int i=1; i<=mf; i++)
            if ((fields.get (i)) != null)
                bmap.set (i);
        set (new ISOBitMap (-1, bmap));
        dirty = false;
    }
    /**
     * clone fields
     */
    public Map getChildren() {
        return (Map) ((TreeMap)fields).clone();
    }
    /**
     * pack the message with the current packager
     * @return the packed message
     * @exception ISOException
     */
    public byte[] pack() throws ISOException {
        synchronized (this) {
            recalcBitMap();
            return packager.pack(this);
        }
    }
    /**
     * unpack a message
     * @param b - raw message
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack(byte[] b) throws ISOException {
        synchronized (this) {
            return packager.unpack(this, b);
        }
    }
    public void unpack (InputStream in) throws IOException, ISOException {
        synchronized (this) {
            packager.unpack(this, in);
        }
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
        p.print (indent + "<" + XMLPackager.ISOMSG_TAG);
        switch (direction) {
            case INCOMING:
                p.print (" direction=\"incoming\"");
                break;
            case OUTGOING:
                p.print (" direction=\"outgoing\"");
                break;
        }
        if (fieldNumber != -1)
            p.print (" "+XMLPackager.ID_ATTR +"=\""+fieldNumber +"\"");
        p.println (">");
        String newIndent = indent + "  ";
        if (getPackager() != null) {
           p.println (
              newIndent
           + "<!-- " + getPackager().getDescription() + " -->"
           );
        }
        if (header instanceof Loggeable)
            ((Loggeable) header).dump (p, newIndent);

        for (int i=0; i<=maxField; i++) {
            if ((c = (ISOComponent) fields.get (i)) != null)
                c.dump (p, newIndent);
            //
            // Uncomment to include bitmaps within logs
            // 
            // if (i == 0) {
            //  if ((c = (ISOComponent) fields.get (new Integer (-1))) != null)
            //    c.dump (p, newIndent);
            // }
            //
        }

        p.println (indent + "</" + XMLPackager.ISOMSG_TAG+">");
    }
    /**
     * get the component associated with the given field number
     * @param fldno the Field Number
     * @return the Component
     */
    public ISOComponent getComponent(int fldno) {
        return (ISOComponent) fields.get(fldno);
    }
    /**
     * Return the object value associated with the given field number
     * @param fldno the Field Number
     * @return the field Object
     * @throws ISOException on error
     */
    public Object getValue(int fldno) throws ISOException {
        ISOComponent c = getComponent(fldno);
        return c != null ? c.getValue() : null;
    }
    /**
     * Return the object value associated with the given field path
     * @param fpath field path
     * @return the field Object (may be null)
     * @throws ISOException on error
     */
    public Object getValue (String fpath) throws ISOException {
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        Object obj;
        for (;;) {
            int fldno = Integer.parseInt(st.nextToken());
            obj = m.getValue (fldno);
            if (st.hasMoreTokens()) {
                if (obj instanceof ISOMsg) {
                    m = (ISOMsg) obj;
                }
                else
                    throw new ISOException ("Invalid path '" + fpath + "'");
            } else 
                break;
        }
        return obj;
    }
    /**
     * get the component associated with the given field number
     * @param fpath field path
     * @return the Component
     * @throws ISOException on error
     */
    public ISOComponent getComponent (String fpath) throws ISOException {
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        ISOComponent obj;
        for (;;) {
            int fldno = Integer.parseInt(st.nextToken());
            obj = m.getComponent(fldno);
            if (st.hasMoreTokens()) {
                if (obj instanceof ISOMsg) {
                    m = (ISOMsg) obj;
                }
                else
                    break; // 'Quick' exit if hierachy is not present.
            } else 
                break;
        }
        return obj;
    }
    /**
     * Return the String value associated with the given ISOField number
     * @param fldno the Field Number
     * @return field's String value
     */
    public String getString (int fldno) {
        String s = null;
        if (hasField (fldno)) {
            try {
                Object obj = getValue(fldno);
                if (obj instanceof String)
                    s = (String) obj;
                else if (obj instanceof byte[])
                    s = ISOUtil.hexString ((byte[]) obj);
            } catch (ISOException e) {
                // ignore ISOException - return null
            }
        }
        return s;
    }
    /**
     * Return the String value associated with the given field path 
     * @param fpath field path
     * @return field's String value (may be null)
     */
    public String getString (String fpath) {
        String s = null;
        try {
            Object obj = getValue(fpath);
            if (obj instanceof String)
                s = (String) obj;
            else if (obj instanceof byte[])
                s = ISOUtil.hexString ((byte[]) obj);
        } catch (ISOException e) {
            // ignore ISOException - return null
        }
        return s;
    }
    /**
     * Return the byte[] value associated with the given ISOField number
     * @param fldno the Field Number
     * @return field's byte[] value
     */
    public byte[] getBytes (int fldno) {
        byte[] b = null;
        if (hasField (fldno)) {
            try {
                Object obj = getValue(fldno);
                if (obj instanceof String)
                    b = ((String) obj).getBytes(ISOUtil.ENCODING);
                else if (obj instanceof byte[])
                    b = ((byte[]) obj);
            } catch (ISOException ignored) {
            } catch (UnsupportedEncodingException ignored) {}
        }
        return b;
    }
    /**
     * Return the String value associated with the given field path 
     * @param fpath field path
     * @return field's byte[] value (may be null)
     */
    public byte[] getBytes (String fpath) {
        byte[] b = null;
        try {
            Object obj = getValue(fpath);
            if (obj instanceof String)
                b = ((String) obj).getBytes(ISOUtil.ENCODING);
            else if (obj instanceof byte[])
                b = ((byte[]) obj);
        } catch (ISOException ignored) {
        } catch (UnsupportedEncodingException ignored) { }
        return b;
    }
    /**
     * Check if a given field is present
     * @param fldno the Field Number
     * @return boolean indicating the existence of the field
     */
    public boolean hasField(int fldno) {
        return fields.get(fldno) != null;
    }
    /**
     * Check if all fields are present
     * @param fields an array of fields to check for presence
     * @return true if all fields are present
     */
    public boolean hasFields (int[] fields) {
        for (int field : fields)
            if (!hasField(field))
                return false;
        return true;
    }
    /**
     * Check if a field indicated by a fpath is present
     * @param fpath dot-separated field path (i.e. 63.2)
     * @return true if field present
     * @throws ISOException on error
     */
     public boolean hasField (String fpath) throws ISOException {
         StringTokenizer st = new StringTokenizer (fpath, ".");
         ISOMsg m = this;
         for (;;) {
             int fldno = Integer.parseInt(st.nextToken());
             if (st.hasMoreTokens()) {
                 Object obj = m.getValue(fldno);
                 if (obj instanceof ISOMsg) {
                     m = (ISOMsg) obj;
                 }
                 else {
                     // No real way of checking for further subfields, return false, perhaps should be ISOException?
                     return false;
                 }
             } else {
                 return m.hasField(fldno);
             }
         }
     }
    /**
     * @return true if ISOMsg has at least one field
     */
    public boolean hasFields () {
        return !fields.isEmpty();
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
    
    @Override
    public Object clone() {
        try {
            ISOMsg m = (ISOMsg) super.clone();
            m.fields = (TreeMap) ((TreeMap) fields).clone();
            if (header != null)
                m.header = (ISOHeader) header.clone();

            for (Integer k : fields.keySet()) {
                ISOComponent c = (ISOComponent) m.fields.get(k);
                if (c instanceof ISOMsg)
                    m.fields.put(k, ((ISOMsg) c).clone());
            }
            return m;
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
            m.fields = new TreeMap();
            for (int field : fields) {
                if (hasField(field)) {
                    try {
                        m.set(getComponent(field));
                    } catch (ISOException e) {
                        // it should never happen
                    }
                }
            }
            return m;
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

        s.append(getString(0));
        if (hasField(11)) {
            s.append(' ');
            s.append(getString(11));
        }
        if (hasField(41)) {
            s.append(' ');
            s.append(getString(41));
        }
        return s.toString();
    }
    public Object getKey() throws ISOException {
        if (fieldNumber != -1)
            return fieldNumber;
        throw new ISOException ("This is not a subField");
    }
    public Object getValue() {
        return this;
    }
    /**
     * @return true on inner messages
     */
    public boolean isInner() {
        return fieldNumber > -1;
    }
    /**
     * @param mti new MTI
     * @exception ISOException if message is inner message
     */
    public void setMTI (String mti) throws ISOException {
        if (isInner())
            throw new ISOException ("can't setMTI on inner message");
        set (new ISOField (0, mti));
    }
    /**
     * moves a field (renumber)
     * @param oldFieldNumber old field number
     * @param newFieldNumber new field number
     * @throws ISOException on error
     */
    public void move (int oldFieldNumber, int newFieldNumber) 
        throws ISOException
    {
        ISOComponent c = getComponent (oldFieldNumber);
        unset (oldFieldNumber);
        if (c != null) {
            c.setFieldNumber (newFieldNumber);
            set (c);
        } else
            unset (newFieldNumber);
    }

    /**
     * @return current MTI
     * @exception ISOException on inner message or MTI not set
     */
    public String getMTI() throws ISOException {
        if (isInner())
            throw new ISOException ("can't getMTI on inner message");
        else if (!hasField(0))
            throw new ISOException ("MTI not available");
        return (String) getValue(0);
    }
    /**
     * @return true if message "seems to be" a request
     * @exception ISOException on MTI not set
     */
    public boolean isRequest() throws ISOException {
        return Character.getNumericValue(getMTI().charAt (2))%2 == 0;
    }
    /**
     * @return true if message "seems not to be" a request
     * @exception ISOException on MTI not set
     */
    public boolean isResponse() throws ISOException {
        return !isRequest();
    }
    /**
     * @return true if message is Retransmission
     * @exception ISOException on MTI not set
     */
    public boolean isRetransmission() throws ISOException {
        return getMTI().charAt(3) == '1';
    }
    /**
     * sets an appropiate response MTI.
     *
     * i.e. 0100 becomes 0110<br>
     * i.e. 0201 becomes 0210<br>
     * i.e. 1201 becomes 1210<br>
     * @exception ISOException on MTI not set or it is not a request
     */
    public void setResponseMTI() throws ISOException {
        if (!isRequest())
            throw new ISOException ("not a request - can't set response MTI");

        String mti = getMTI();
        char c1 = mti.charAt(3);
        char c2 = '0';
        switch (c1)
        {
            case '0' :
            case '1' : c2='0';break;
            case '2' :
            case '3' : c2='2';break;
            case '4' :
            case '5' : c2='4';break;

        }
        set (new ISOField (0,
            mti.substring(0,2)
            +(Character.getNumericValue(getMTI().charAt (2))+1) + c2
            )
        );
    }
    /**
     * sets an appropiate retransmission MTI<br>
     * @exception ISOException on MTI not set or it is not a request
     */
    public void setRetransmissionMTI() throws ISOException {
        if (!isRequest())
            throw new ISOException ("not a request");

        set (new ISOField (0, getMTI().substring(0,3) + "1"));
    }
    protected void writeHeader (ObjectOutput out) throws IOException {
        int len = header.getLength();
        if (len > 0) {
            out.writeByte ('H');
            out.writeShort (len);
            out.write (header.pack());
        }
    }
    
    protected void readHeader (ObjectInput in) 
        throws IOException, ClassNotFoundException
    {
        byte[] b = new byte[in.readShort()];
        in.readFully (b);
        setHeader (b);
    }
    protected void writePackager(ObjectOutput out) throws IOException {
        out.writeByte('P');
        String pclass = packager.getClass().getName();
        byte[] b = pclass.getBytes();
        out.writeShort(b.length);
        out.write(b);
    }
    protected void readPackager(ObjectInput in) throws IOException,
    ClassNotFoundException {
        byte[] b = new byte[in.readShort()];
        in.readFully(b);
        try {
            Class mypClass = Class.forName(new String(b));
            ISOPackager myp = (ISOPackager) mypClass.newInstance();
            setPackager(myp);
        } catch (Exception e) {
            setPackager(null);
        }

}
    protected void writeDirection (ObjectOutput out) throws IOException {
        out.writeByte ('D');
        out.writeByte (direction);
    }
    protected void readDirection (ObjectInput in) 
        throws IOException, ClassNotFoundException
    {
        direction = in.readByte();
    }
 
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeByte (0);  // reserved for future expansion (version id)
        out.writeShort (fieldNumber);

        if (header != null) 
            writeHeader (out);
        if (packager != null)
            writePackager(out);
        if (direction > 0)
            writeDirection (out);

        // List keySet = new ArrayList (fields.keySet());
        // Collections.sort (keySet);
        for (Object o : fields.values()) {
            ISOComponent c = (ISOComponent) o;
            if (c instanceof ISOMsg) {
                writeExternal(out, 'M', c);
            } else if (c instanceof ISOBinaryField) {
                writeExternal(out, 'B', c);
            } else if (c instanceof ISOAmount) {
                writeExternal(out, 'A', c);
            } else if (c instanceof ISOField) {
                writeExternal(out, 'F', c);
            }
        }
        out.writeByte ('E');
    }

    public void readExternal  (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        in.readByte();  // ignore version for now
        fieldNumber = in.readShort();
        byte fieldType;
        ISOComponent c;
        try {
            while ((fieldType = in.readByte()) != 'E') {
                c = null;
                switch (fieldType) {
                    case 'F':
                        c = new ISOField ();
                        break;
                    case 'A':
                        c = new ISOAmount ();
                        break;
                    case 'B':
                        c = new ISOBinaryField ();
                        break;
                    case 'M':
                        c = new ISOMsg ();
                        break;
                    case 'H':
                        readHeader (in);
                        break;
                    case 'P':
                        readPackager(in);
                        break;
                    case 'D':
                        readDirection (in);
                        break;
                    default:
                        throw new IOException ("malformed ISOMsg");
                }
                if (c != null) {
                    ((Externalizable)c).readExternal (in);
                    set (c);
                }
            }
        }
        catch (ISOException e) {
            throw new IOException (e.getMessage());
        }
    }
    /**
     * Let this ISOMsg object hold a weak reference to an ISOSource
     * (usually used to carry a reference to the incoming ISOChannel)
     * @param source an ISOSource
     */
    public void setSource (ISOSource source) {
        this.sourceRef = new WeakReference (source);
    }
    /**
     * @return an ISOSource or null
     */
    public ISOSource getSource () {
        return (sourceRef != null) ? (ISOSource) sourceRef.get () : null;
    }
    private void writeExternal (ObjectOutput out, char b, ISOComponent c) throws IOException {
        out.writeByte (b);
        ((Externalizable) c).writeExternal (out);
    }
}

