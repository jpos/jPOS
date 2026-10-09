/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import java.nio.charset.StandardCharsets;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * implements <b>Composite</b>
 * within a <b>Composite pattern</b>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOField
 */
@SuppressWarnings("unchecked")
public class ISOMsg extends ISOComponent
    implements Cloneable, Loggeable, Externalizable
{
    /** Map of field number to field value. */
    protected Map<Integer,Object> fields;
    /** Highest field number currently set in this message. */
    protected int maxField;
    /** The packager used to pack/unpack this message. */
    protected ISOPackager packager;
    /** Dirty flags for tracking state changes. */
    protected boolean dirty, maxFieldDirty;
    /** Message direction: INCOMING or OUTGOING. */
    protected int direction;
    /** Optional ISO header for this message. */
    protected ISOHeader header;
    /** Optional trailer bytes appended to the packed message. */
    protected byte[] trailer;
    /** Field number of this message when nested inside another ISOMsg. */
    protected int fieldNumber = -1;
    /** Explicitly asserted trace id; transient, never packed. See {@link #getTraceId()}. */
    private transient String claimedTraceId;
    /** Fields, besides the MTI class, that identify one wire exchange. */
    private static volatile int[] traceIdFields = { 3, 7, 11, 12, 41, 42 };
    /** Constant indicating an incoming message direction. */
    public static final int INCOMING = 1;
    /** Constant indicating an outgoing message direction. */
    public static final int OUTGOING = 2;
    private static final long serialVersionUID = 4306251831901413975L;
    private static final int MAX_EXTERNALIZED_ENTRIES = 10_000;
    private static final int MAX_EXTERNALIZED_DEPTH = 64;
    private static final int MAX_PACKAGER_CLASS_NAME_LENGTH = 1_024;
    private static final ThreadLocal<ExternalReadContext> EXTERNAL_READ_CONTEXT = new ThreadLocal<>();
    private WeakReference sourceRef;

    private static class ExternalReadContext {
        private int entries;
        private int depth;
        private Boolean allowPackagerMetadata;
    }

    /**
     * Creates an ISOMsg
     */
    public ISOMsg () {
        fields = new TreeMap<>();
        maxField = -1;
        dirty = true;
        maxFieldDirty=true;
        direction = 0;
        header = null;
        trailer = null;
    }
    /**
     * Creates a nested ISOMsg
     * @param fieldNumber (in the outer ISOMsg) of this nested message
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
    @Override
    public void setFieldNumber (int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }
    /**
     * Creates an ISOMsg with given mti
     * @param mti Msg's MTI
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public ISOMsg (String mti) {
        this();
        try {
            setMTI (mti);
        } catch (ISOException ignored) {
            // Should never happen as this is not an inner message
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

    /**
     * Sets the ISO header for this message.
     * @param header the ISOHeader to set on this message
     */
    public void setHeader (ISOHeader header) {
        this.header = header;
    }
    /**
     * get optional message header image
     * @return message header image (may be null)
     */
    public byte[] getHeader() {
        return header != null ? header.pack() : null;
    }

    /**
     * Sets optional trailer data.
     * <p>
     * Note: The trailer data requires a customised channel that explicitly handles the trailer data from the ISOMsg.
     *
     * @param trailer The trailer data.
     * @see BaseChannel#getMessageTrailer(ISOMsg)
     * @see BaseChannel#sendMessageTrailer(ISOMsg, byte[])
     */
    public void setTrailer(byte[] trailer) {
        this.trailer = trailer;
    }

    /**
     * Get optional trailer image.
     *
     * @return message trailer image (may be null)
     */
    public byte[] getTrailer() {
        return this.trailer;
    }

    /**
     * Return this messages ISOHeader
     * @return header associated with this ISOMsg, can be null
     */
    public ISOHeader getISOHeader() {
        return header;
    }
    /**
     * Returns the message direction.
     * @return the direction ({@code ISOMsg.INCOMING} or {@code ISOMsg.OUTGOING})
     * @see ISOChannel
     */
    public int getDirection() {
        return direction;
    }
    /**
     * Returns true if this message was received from a channel.
     * @return true if this is an incoming message
     * @see ISOChannel
     */
    public boolean isIncoming() {
        return direction == INCOMING;
    }
    /**
     * Returns true if this message is to be sent via a channel.
     * @return true if this is an outgoing message
     * @see ISOChannel
     */
    public boolean isOutgoing() {
        return direction == OUTGOING;
    }
    /**
     * Returns the highest field number present in this message.
     * @return the max field number
     */
    @Override
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
     * Sets the packager used to pack/unpack this message.
     * @param p - a peer packager
     */
    public void setPackager (ISOPackager p) {
        packager = p;
        if (packager == null) {
            for (Object o : fields.values()) {
                if (o instanceof ISOMsg)
                    ((ISOMsg) o).setPackager(null);
            }
        }
    }
    /**
     * Returns the packager associated with this message.
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
     * Creates an ISOField associated with fldno within this ISOMsg.
     *
     * @param fldno field number
     * @param value field value
     */
    public void set(int fldno, String value) {
        if (value == null) {
            unset(fldno);
            return;
        }

        try {
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
        } catch (ISOException ex) {}; //NOPMD: never happens for the given arguments of set methods
    }

    /**
     * Sets a top-level character field and returns this message for fluent chaining.
     *
     * @param fldno field number
     * @param value field value
     * @return this message
     */
    public ISOMsg with(int fldno, String value) {
        set(fldno, value);
        return this;
    }

    /**
     * Creates an ISOField associated with fldno within this ISOMsg.
     *
     * @param fpath dot-separated field path (i.e. 63.2)
     * @param value field value
     * @throws IllegalArgumentException if fpath addresses a dataset field and does not
     *         match the field packager's dataset envelope, or the value cannot be stored there
     */
    public void set(String fpath, String value) {
        try {
            if (setDatasetPath(fpath, value))
                return;
        } catch (ISOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        for (;;) {
            int fldno = parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
                Object obj = m.getValue(fldno);
                if (obj instanceof ISOMsg)
                    m = (ISOMsg) obj;
                else
                    /**
                     * we need to go deeper, however, if the value == null then
                     * there is nothing to do (unset) at the lower levels, so break now and save some processing.
                     */
                    if (value == null) {
                        break;
                    } else {
                        try {
                            // We have a value to set, so adding a level to hold it is sensible.
                            m.set(m = new ISOMsg (fldno));
                        } catch (ISOException ex) {} //NOPMD: never happens for the given arguments of set methods
                    }
            } else {
                m.set(fldno, value);
                break;
            }
        }
    }

    /**
     * Sets a character field by path and returns this message for fluent chaining.
     *
     * @param fpath dot-separated field path
     * @param value field value
     * @return this message
     */
    public ISOMsg with(String fpath, String value) {
        set(fpath, value);
        return this;
    }

    /**
     * Creates an ISOField associated with fldno within this ISOMsg
     * @param fpath dot-separated field path (i.e. 63.2)
     * @param c component
     * @throws ISOException on error, including a path that addresses a dataset field
     *         and does not match the field packager's dataset envelope
     */
    public void set(String fpath, ISOComponent c) throws ISOException {
        if (setDatasetPath(fpath, c))
            return;
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        for (;;) {
            int fldno = parseInt(st.nextToken());
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
                        m.set(m = new ISOMsg(fldno));
                    }
            } else {
                if (c != null)
                    c.setFieldNumber(fldno);
                m.set(c);
                break;
            }
        }
    }

    /**
     * Sets a component by path and returns this message for fluent chaining.
     *
     * @param fpath dot-separated field path
     * @param c component to store
     * @return this message
     * @throws ISOException on path or component errors
     */
    public ISOMsg with(String fpath, ISOComponent c) throws ISOException {
        set(fpath, c);
        return this;
    }
    /**
     * Creates an ISOField associated with fldno within this ISOMsg.
     *
     * @param fpath dot-separated field path (i.e. 63.2)
     * @param value binary field value
     * @throws IllegalArgumentException if fpath addresses a dataset field and does not
     *         match the field packager's dataset envelope, or the value cannot be stored there
     */
    public void set(String fpath, byte[] value) {
        try {
            if (setDatasetPath(fpath, value))
                return;
        } catch (ISOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        for (;;) {
            int fldno = parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
                Object obj = m.getValue(fldno);
                if (obj instanceof ISOMsg)
                    m = (ISOMsg) obj;
                else
                    try {
                        m.set(m = new ISOMsg (fldno));
                    } catch (ISOException ex) {} //NOPMD: never happens for the given arguments of set methods
            } else {
                m.set(fldno, value);
                break;
            }
        }
    }

    /**
     * Sets a top-level binary field and returns this message for fluent chaining.
     *
     * @param fldno field number
     * @param value field value
     * @return this message
     */
    public ISOMsg with(int fldno, byte[] value) {
        set(fldno, value);
        return this;
    }

    /**
     * Sets a binary field by path and returns this message for fluent chaining.
     *
     * @param fpath dot-separated field path
     * @param value field value
     * @return this message
     */
    public ISOMsg with(String fpath, byte[] value) {
        set(fpath, value);
        return this;
    }

    /**
     * Creates an ISOBinaryField associated with fldno within this ISOMsg.
     *
     * @param fldno field number
     * @param value field value
     */
    public void set(int fldno, byte[] value) {
        if (value == null) {
            unset(fldno);
            return;
        }

        try {
            set(new ISOBinaryField(fldno, value));
        } catch (ISOException ex) {}; //NOPMD: never happens for the given arguments of set methods
    }


    /**
     * Unset a field if it exists, otherwise ignore.
     * @param fldno - the field number
     */
    @Override
    public void unset (int fldno) {
        if (fields.remove (fldno) != null)
            dirty = maxFieldDirty = true;
    }

    /**
     * Unsets several fields at once
     * @param flds - array of fields to be unset from this ISOMsg
     */
    public void unset (int ... flds) {
        for (int fld : flds)
            unset(fld);
    }

    /**
     * Unset a field referenced by a fpath if it exists, otherwise ignore.
     *
     * @param fpath dot-separated field path (i.e. 63.2)
     * @throws IllegalArgumentException if fpath addresses a dataset field and does not
     *         match the field packager's dataset envelope
     */
    public void unset(String fpath) {
        try {
            if (unsetDatasetPath(fpath))
                return;
        } catch (ISOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        ISOMsg lastm = m;
        int fldno = -1 ;
        int lastfldno ;
        for (;;) {
            lastfldno = fldno;
            fldno = parseInt(st.nextToken());
            if (st.hasMoreTokens()) {
                Object obj = m.getValue(fldno);
                if (obj instanceof ISOMsg) {
                    lastm = m;
                    m = (ISOMsg) obj;
                }
                else {
                    // No real way of unset further subfield, exit.
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
     * Unset a a set of fields referenced by fpaths if any ot them exist, otherwise ignore.
     *
     * @param fpaths dot-separated field paths (i.e. 63.2)
     */
    public void unset(String ... fpaths) {
        for (String fpath : fpaths) {
            unset(fpath);
        }
    }

    /**
     * Unsets one or more top-level fields and returns this message for fluent chaining.
     *
     * @param flds field numbers to remove
     * @return this message
     */
    public ISOMsg without(int ... flds) {
        unset(flds);
        return this;
    }

    /**
     * Unsets one or more field paths, including nested composites and dataset elements,
     * and returns this message for fluent chaining.
     *
     * @param fpaths field paths to remove
     * @return this message
     */
    public ISOMsg without(String ... fpaths) {
        unset(fpaths);
        return this;
    }

    /**
     * Sets an element in a dataset field without a dataset envelope (such as DE 55
     * ICC data), where the dataset identifier is the field number.
     *
     * <p>Unlike {@link #set(String, String)}, no packager is needed: the dataset
     * field is created if absent, along with any intermediate composite fields.
     * New datasets use {@link DatasetFormat#TLV}. The value replaces every existing
     * element with the same identifier in the field's first dataset; TLV elements
     * record whether the tag is constructed.</p>
     *
     * @param fpath dot-separated path of the dataset field (i.e. 55, 127.55)
     * @param elementId element identifier (TLV tag)
     * @param value a {@code String}, {@code byte[]} or {@link ISOComponent};
     *        {@code null} removes the element, as {@link #unsetDatasetElement(String, int)}
     * @throws IllegalArgumentException if fpath is malformed or elementId is negative
     * @throws ISOException if the path runs through, or ends at, a field of the wrong
     *         type, or the value type is not supported
     */
    public void setDatasetElement(String fpath, int elementId, Object value) throws ISOException {
        writeDatasetElement(fpath, false, 0, elementId, value);
    }

    /**
     * Sets an element in a dataset field with a dataset envelope (ISO 8583:2023
     * composite data elements such as DE 104).
     *
     * <p>Unlike {@link #set(String, String)}, no packager is needed: the dataset
     * field is created if absent, along with any intermediate composite fields.
     * New datasets use {@link DatasetFormat#TLV} for identifiers up to {@code 0x70}
     * and {@link DatasetFormat#DBM} above. The value replaces every existing element
     * with the same identifier in the first dataset carrying {@code datasetId};
     * TLV elements record whether the tag is constructed.</p>
     *
     * @param fpath dot-separated path of the dataset field (i.e. 104)
     * @param datasetId dataset identifier
     * @param elementId element identifier (TLV tag or DBM bit number)
     * @param value a {@code String}, {@code byte[]} or {@link ISOComponent};
     *        {@code null} removes the element, as {@link #unsetDatasetElement(String, int, int)}
     * @throws IllegalArgumentException if fpath is malformed or an identifier is negative
     * @throws ISOException if the path runs through, or ends at, a field of the wrong
     *         type, or the value type is not supported
     */
    public void setDatasetElement(String fpath, int datasetId, int elementId, Object value) throws ISOException {
        writeDatasetElement(fpath, true, datasetId, elementId, value);
    }

    /**
     * Sets an element in a dataset field without a dataset envelope and returns
     * this message for fluent chaining.
     *
     * @param fpath dot-separated path of the dataset field (i.e. 55)
     * @param elementId element identifier (TLV tag)
     * @param value element value
     * @return this message
     * @throws ISOException on errors, see {@link #setDatasetElement(String, int, Object)}
     */
    public ISOMsg withDatasetElement(String fpath, int elementId, Object value) throws ISOException {
        setDatasetElement(fpath, elementId, value);
        return this;
    }

    /**
     * Sets an element in a dataset field with a dataset envelope and returns this
     * message for fluent chaining.
     *
     * @param fpath dot-separated path of the dataset field (i.e. 104)
     * @param datasetId dataset identifier
     * @param elementId element identifier (TLV tag or DBM bit number)
     * @param value element value
     * @return this message
     * @throws ISOException on errors, see {@link #setDatasetElement(String, int, int, Object)}
     */
    public ISOMsg withDatasetElement(String fpath, int datasetId, int elementId, Object value) throws ISOException {
        setDatasetElement(fpath, datasetId, elementId, value);
        return this;
    }

    /**
     * Removes an element from a dataset field without a dataset envelope.
     *
     * <p>Every occurrence is removed. Datasets left empty are removed, and so is
     * the dataset field once it has no datasets. An absent path is ignored.</p>
     *
     * @param fpath dot-separated path of the dataset field (i.e. 55)
     * @param elementId element identifier (TLV tag)
     * @throws IllegalArgumentException if fpath is malformed or elementId is negative
     * @throws ISOException if the path ends at a field that is not a dataset field
     */
    public void unsetDatasetElement(String fpath, int elementId) throws ISOException {
        writeDatasetElement(fpath, false, 0, elementId, null);
    }

    /**
     * Removes an element from a dataset field with a dataset envelope.
     *
     * <p>Every occurrence is removed from every dataset carrying {@code datasetId}.
     * Datasets left empty are removed, and so is the dataset field once it has no
     * datasets. An absent path is ignored.</p>
     *
     * @param fpath dot-separated path of the dataset field (i.e. 104)
     * @param datasetId dataset identifier
     * @param elementId element identifier (TLV tag or DBM bit number)
     * @throws IllegalArgumentException if fpath is malformed or an identifier is negative
     * @throws ISOException if the path ends at a field that is not a dataset field
     */
    public void unsetDatasetElement(String fpath, int datasetId, int elementId) throws ISOException {
        writeDatasetElement(fpath, true, datasetId, elementId, null);
    }
    /**
     * In order to interchange <b>Composites</b> and <b>Leafs</b> we use
     * getComposite(). A <b>Composite component</b> returns itself and
     * a Leaf returns null.
     *
     * @return ISOComponent
     */
    @Override
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

        BitSet bmap = new BitSet (mf+62 >>6 <<6);
        for (int i=1; i<=mf; i++)
            if (fields.get (i) != null)
                bmap.set (i);
        set (new ISOBitMap (-1, bmap));
        dirty = false;
    }
    /**
     * clone fields
     * @return copy of fields
     */
    @Override
    public Map getChildren() {
        return (Map) ((TreeMap)fields).clone();
    }
    /**
     * Packs this message using the configured packager.
     * @return the packed message
     * @exception ISOException on packing error
     */
    @Override
    public byte[] pack() throws ISOException {
        synchronized (this) {
            recalcBitMap();
            return packager.pack(this);
        }
    }
    /**
     * Unpacks the raw byte array into this message.
     * @param b - raw message
     * @return consumed bytes
     * @exception ISOException on unpacking error
     */
    @Override
    public int unpack(byte[] b) throws ISOException {
        synchronized (this) {
            return packager.unpack(this, b);
        }
    }
    /** {@inheritDoc}
     * @throws IOException on I/O failure
     * @throws ISOException on unpacking error
     */
    @Override
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
    @Override
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

        for (int i : fields.keySet()) {
           //If you want the bitmap dumped in the log, change the condition from (i >= 0) to (i >= -1). 
            if (i >= 0) {
                if ((c = (ISOComponent) fields.get(i)) != null)
                    c.dump(p, newIndent);
            }
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
     */
    public Object getValue(int fldno) {
        ISOComponent c = getComponent(fldno);
        try {
            return c != null ? c.getValue() : null;
        } catch (ISOException ex) {
            return null; //never happens for the given arguments of getValue method
        }
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
            int fldno = parseInt(st.nextToken());
            obj = m.getValue (fldno);
            if (obj==null){
                // The user will always get a null value for an incorrect path or path not present in the message
                // no point having the ISOException thrown for fields that were not received.
                break;
            }
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
     * get the component associated with the given field path
     * @param fpath field path
     * @return the Component, or {@code null} if it is not present or any
     *         intermediate path element is absent or not an {@link ISOMsg}
     * @throws ISOException on error
     */
    public ISOComponent getComponent (String fpath) throws ISOException {
        StringTokenizer st = new StringTokenizer (fpath, ".");
        ISOMsg m = this;
        ISOComponent obj;
        for (;;) {
            int fldno = parseInt(st.nextToken());
            obj = m.getComponent(fldno);
            if (st.hasMoreTokens()) {
                if (obj instanceof ISOMsg) {
                    m = (ISOMsg) obj;
                }
                else
                    return null; // hierarchy is not present
            } else
                break;
        }
        return obj;
    }

    /**
     * Strict variant of {@link #getValue(String)}.
     *
     * <p>The whole path is validated before the message is read, so a malformed
     * path always throws {@link IllegalArgumentException}, regardless of the
     * message contents. Each segment is a decimal number ({@code 62}) or a
     * {@code 0x}-prefixed hexadecimal number ({@code 0x9F26}) in the range
     * {@code 0..Integer.MAX_VALUE}; signs, whitespace and empty segments are invalid.</p>
     *
     * <p>Every segment but the last must address a sub-{@link ISOMsg}. A path
     * through a leaf field or an absent field returns {@link Optional#empty()}.
     * The value is a {@code String}, a {@code byte[]}, the sub-{@code ISOMsg} for
     * a composite field, or the {@code List<Dataset>} of a dataset field.</p>
     *
     * <p>When the path reaches an {@link ISODatasetField} with segments left, it
     * addresses a dataset element, using the same layout as {@link #set(String, String)}:
     * {@code 55.0x9F26} (no envelope, the dataset identifier is the field number) or
     * {@code 104.0x71.0x01} (envelope: dataset identifier, then element identifier).
     * If the field packager of the message holding the dataset field is a
     * {@link DatasetFieldPackager}, its {@link ISODatasetPackager#hasDatasetEnvelope()}
     * decides the layout and a path that does not match it returns empty. Otherwise
     * the layout is inferred from the number of remaining segments. More than two
     * segments past a dataset field return empty.</p>
     *
     * <p>A dataset element path returns the value of the first element in
     * {@link #findDatasetElements(String)}, so {@code findValue(p).isPresent()} is
     * equivalent to {@code !findDatasetElements(p).isEmpty()}. Constructed TLV
     * elements return their encoded value; decode their children with
     * {@link org.jpos.tlv.TLVList}.</p>
     *
     * <p>Without a packager, {@code set("55.0x9F26", value)} creates a plain
     * composite field rather than a dataset field; this method reads it back
     * through the normal composite walk. Use
     * {@link #setDatasetElement(String, int, Object)} to create dataset fields
     * without a packager.</p>
     *
     * @param fpath dot-separated field path (i.e. 62.1, 55.0x9F26)
     * @return the value, or empty if the path is not present
     * @throws IllegalArgumentException if fpath is malformed
     * @throws ISOException if a dataset element value cannot be read
     */
    public Optional<Object> findValue(String fpath) throws ISOException {
        int[] path = parsePath(fpath);
        PathTarget target = locate(path);
        if (target == null)
            return Optional.empty();
        if (target.datasetField() == null)
            return Optional.ofNullable(target.msg().getValue(path[target.index()]));
        List<DatasetElement> elements = target.msg().datasetElements(target.datasetField(), path, target.index());
        return elements.isEmpty() ? Optional.empty() : Optional.ofNullable(elements.get(0).getValue());
    }

    /**
     * Returns every occurrence of the dataset element addressed by a path.
     *
     * <p>Uses the same path grammar and dataset layout rules as {@link #findValue(String)}.
     * Elements are collected from every dataset carrying the addressed dataset
     * identifier, in dataset order and then element order, so repeated tags (such
     * as EMV issuer scripts) are all returned with their constructed flag.</p>
     *
     * @param fpath dot-separated dataset element path (i.e. 55.0x71, 104.0x71.0x01)
     * @return unmodifiable list of matching elements; empty if the path is absent or
     *         does not address a dataset element
     * @throws IllegalArgumentException if fpath is malformed
     * @throws ISOException on dataset access errors
     */
    public List<DatasetElement> findDatasetElements(String fpath) throws ISOException {
        int[] path = parsePath(fpath);
        PathTarget target = locate(path);
        if (target == null || target.datasetField() == null)
            return Collections.emptyList();
        return target.msg().datasetElements(target.datasetField(), path, target.index());
    }
    /**
     * Return the String value associated with the given ISOField number
     * @param fldno the Field Number
     * @return field's String value
     */
    public String getString (int fldno) {
        String s = null;
        if (hasField (fldno)) {
            Object obj = getValue(fldno);
            if (obj instanceof String)
                s = (String) obj;
            else if (obj instanceof byte[])
                s = ISOUtil.hexString((byte[]) obj);
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
            return null;
        }
        return s;
    }
    /**
     * Return the byte[] value associated with the given ISOField number
     * @param fldno the Field Number
     * @return field's byte[] value or null if ISOException or UnsupportedEncodingException happens
     */
    public byte[] getBytes (int fldno) {
        byte[] b = null;
        if (hasField (fldno)) {
            Object obj = getValue(fldno);
            if (obj instanceof String)
                b = ((String) obj).getBytes(ISOUtil.CHARSET);
            else if (obj instanceof byte[])
                b = (byte[]) obj;
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
                b = ((String) obj).getBytes(ISOUtil.CHARSET);
            else if (obj instanceof byte[])
                b = (byte[]) obj;
        } catch (ISOException ignored) {
            return null;
        }
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
     * Check if the message has any of these fields
     * @param fields an array of fields to check for presence
     * @return true if at least one field is present
     */
    public boolean hasAny (int[] fields) {
        for (int field : fields)
            if (hasField(field))
                return true;
        return false;
    }
    /**
     * Check if the message has any of these fields
     * @param fields to check for presence
     * @return true if at least one field is present
     */
    public boolean hasAny (String... fields) {
        for (String field : fields)
            if (hasField (field))
                return true;
        return false;
    }

    /**
     * Check if a field indicated by a fpath is present
     * @param fpath dot-separated field path (i.e. 63.2)
     * @return true if field present
     */
     public boolean hasField (String fpath) {
         StringTokenizer st = new StringTokenizer (fpath, ".");
         ISOMsg m = this;
         for (;;) {
             int fldno = parseInt(st.nextToken());
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
     * Returns true if this message has at least one field set.
     * @return true if at least one field is present
     */
    public boolean hasFields () {
        return !fields.isEmpty();
    }
    /**
     * Don't call setValue on an ISOMsg. You'll sure get
     * an ISOException. It's intended to be used on Leafs
     * @param obj value to set (not supported on ISOMsg)
     * @throws org.jpos.iso.ISOException always
     * @see ISOField
     * @see ISOException
     */
    @Override
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
            if (trailer != null)
                m.trailer = trailer.clone();
            for (Integer k : fields.keySet()) {
                ISOComponent c = (ISOComponent) m.fields.get(k);
                if (c instanceof ISOMsg || c instanceof ISODatasetField)
                    m.fields.put(k, cloneComponent(c));
            }
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        } catch (ISOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Partially clone an ISOMsg
     * @param fields int array of fields to go
     * @return new ISOMsg instance
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Object clone(int ... fields) {
        try {
            ISOMsg m = (ISOMsg) super.clone();
            m.fields = new TreeMap();
            for (int field : fields) {
                if (hasField(field)) {
                    try {
                        ISOComponent c = getComponent(field);
                        if (c instanceof ISOMsg || c instanceof ISODatasetField) {
                            m.set(cloneComponent(c));
                        } else {
                            m.set(c);
                        }
                    } catch (ISOException ignored) {
                        // should never happen
                    }
                }
            }
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Partially clone an ISOMsg by field paths.
     *
     * <p>Paths use the grammar of {@link #findValue(String)}. Absent paths are
     * skipped. A dataset element path (i.e. 55.0x9F26, 104.0x71.0x01) copies every
     * occurrence of the element, keeping dataset identifiers, formats and constructed
     * flags; elements are added in path order, and a path repeated or covered by an
     * earlier path replaces the copied elements instead of duplicating them.</p>
     *
     * @param fpaths string array of field paths to copy
     * @return new ISOMsg instance
     * @throws IllegalArgumentException if a path is malformed
     */
    public ISOMsg clone(String ... fpaths) {
        try {
            ISOMsg m = (ISOMsg) super.clone();
            m.fields = new TreeMap();
            Map<ISODataset,ISODatasetField> createdDatasets = new IdentityHashMap<>();
            for (String fpath : fpaths) {
                int[] path = parsePath(fpath);
                try {
                    PathTarget target = locate(path);
                    if (target == null)
                        continue;
                    if (target.datasetField() != null) {
                        m.cloneDatasetElements(target, path, createdDatasets);
                        continue;
                    }
                    ISOComponent component = target.msg().getComponent(path[target.index()]);
                    if (component instanceof ISOMsg || component instanceof ISODatasetField) {
                        m.set(fpath, cloneComponent(component));
                    } else if (component != null) {
                        m.set(fpath, component);
                    }
                } catch (ISOException ignored) {
                    //should never happen
                }
            }
            // datasets created only to keep repeated dataset identifiers aligned
            for (Map.Entry<ISODataset,ISODatasetField> e : createdDatasets.entrySet()) {
                if (e.getKey().isEmpty())
                    e.getValue().removeDataset(e.getKey());
            }
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Merges the content of the specified ISOMsg into this ISOMsg instance.
     * It iterates over the fields of the input message and, for each field that is present,
     * sets the corresponding component in this message to the value from the input message.
     * This operation includes all fields that are present in the input message, but does not remove
     * any existing fields from this message unless they are explicitly overwritten by the input message.
     * <p>
     * If the input message contains a header (non-null), this method also clones the header
     * and sets it as the header of this message.
     *
     * @param m The ISOMsg to merge into this ISOMsg. It must not be {@code null}.
     *          The method does nothing if {@code m} is {@code null}.
     * @param mergeHeader A boolean flag indicating whether to merge the header of the input message into this message.
     *
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void merge (ISOMsg m, boolean mergeHeader) {
        for (int i : m.fields.keySet()) {
            try {
                if (i >= 0 && m.hasField(i))
                    set(m.getComponent(i));
            } catch (ISOException ignored) {
                // should never happen
            }
        }
        if (mergeHeader && m.header != null)
            header = (ISOHeader) m.header.clone();
    }

    /**
     * Merges the content of the specified ISOMsg into this ISOMsg instance, excluding the header.
     * This method is a convenience wrapper around {@link #merge(ISOMsg, boolean)} with the {@code mergeHeader}
     * parameter set to {@code false} for backward compatibility, indicating that the header of the input message
     * will not be merged.
     * @param m the ISOMsg to merge into this message
     */
    public void merge (ISOMsg m) {
        merge (m, false);
    }

    /**
     * @return a string suitable for a log
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (isIncoming())
            s.append(" In: ");
        else if (isOutgoing())
            s.append("Out: ");
        else
            s.append("     ");

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
    @Override
    public Object getKey() throws ISOException {
        if (fieldNumber != -1)
            return fieldNumber;
        throw new ISOException ("This is not a subField");
    }
    /** Returns this message itself as its value.
     * @return this ISOMsg
     */
    @Override
    public Object getValue() {
        return this;
    }
    /**
     * Returns true if this is an inner (sub-) message.
     * @return true on inner messages
     */
    public boolean isInner() {
        return fieldNumber > -1;
    }
    /**
     * Sets the message type indicator.
     * @param mti new MTI
     * @exception ISOException if message is inner message
     */
    public void setMTI (String mti) throws ISOException {
        if (isInner())
            throw new ISOException ("can't setMTI on inner message");
        set (new ISOField (0, mti));
    }
    /**
     * Sets the fields used by {@link #naturalTraceId()} in addition to the
     * MTI class. System-wide; defaults to 3, 7, 11, 12, 41 and 42: fields a
     * response echoes unchanged from its request. Fields the server may mint
     * (37, 32) are deliberately excluded; 7 and 12 keep a terminal that wraps
     * its STAN from linking to old exchanges.
     *
     * @param fields field numbers
     */
    public static void setTraceIdFields (int... fields) {
        traceIdFields = fields.clone();
    }

    /**
     * Trace id derived from message content: the first two MTI digits plus
     * the {@link #setTraceIdFields(int...) trace id fields} present, trimmed.
     * A request and its response yield the same value, so one wire exchange
     * shares one id with no carrier on the wire.
     *
     * @return a 32-hex id, or {@code null} when none of the key fields is present
     */
    public String naturalTraceId () {
        StringBuilder sb = new StringBuilder();
        String mti = getString (0);
        if (mti != null && mti.length() >= 2)
            sb.append (mti, 0, 2);
        boolean hasKey = false;
        for (int f : traceIdFields) {
            String v = getString (f);
            if (v != null && !(v = v.trim()).isEmpty()) {
                sb.append ('.').append (v);
                hasKey = true;
            }
        }
        return hasKey
          ? UUID.nameUUIDFromBytes (sb.toString().getBytes (StandardCharsets.UTF_8)).toString().replace ("-", "")
          : null;
    }

    /**
     * Asserts a trace id on this message, for example the id of the request a
     * response belongs to. The claim is transient: copied by {@link #clone()},
     * never packed. Channel events report it as {@code trace-claimed} when it
     * differs from the {@link #naturalTraceId() natural} id.
     *
     * @param traceId the claimed id, or {@code null} to clear
     */
    public void setTraceId (String traceId) {
        this.claimedTraceId = traceId;
    }

    /**
     * The id asserted through {@link #setTraceId(String)}.
     *
     * @return the claimed id, or {@code null}
     */
    public String getClaimedTraceId () {
        return claimedTraceId;
    }

    /**
     * Effective trace id of the wire exchange this message belongs to: the
     * {@link #naturalTraceId() natural} id when computable, else the
     * {@link #getClaimedTraceId() claimed} id, else a random id minted once
     * and kept as the claim.
     *
     * @return a 32-hex trace id, never {@code null}
     */
    public String getTraceId () {
        String natural = naturalTraceId();
        if (natural != null)
            return natural;
        if (claimedTraceId == null)
            claimedTraceId = UUID.randomUUID().toString().replace ("-", "");
        return claimedTraceId;
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

    @Override
    public int getFieldNumber () {
        return fieldNumber;
    }

    /**
     * Returns true if this message has an MTI field (field 0) set.
     * @return true if MTI is present
     * @exception ISOException if this is an inner message
     */
    public boolean hasMTI() throws ISOException {
        if (isInner())
            throw new ISOException ("can't hasMTI on inner message");
        else
            return hasField(0);
    }
    /**
     * Returns the message type indicator.
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
     * Returns true if the MTI suggests this is a request message.
     * @return true if message "seems to be" a request
     * @exception ISOException on MTI not set
     */
    public boolean isRequest() throws ISOException {
        return Character.getNumericValue(getMTI().charAt (2))%2 == 0;
    }
    /**
     * Returns true if the MTI suggests this is a response message.
     * @return true if message "seems not to be" a request
     * @exception ISOException on MTI not set
     */
    public boolean isResponse() throws ISOException {
        return !isRequest();
    }
    /**
     * Returns true if this is an authorization message (MTI second digit = 1).
     * @return true if message class is "authorization"
     * @exception ISOException on MTI not set
     */
    public boolean isAuthorization() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '1';
    }
    /**
     * Returns true if this is a financial message (MTI second digit = 2).
     * @return true if message class is "financial"
     * @exception ISOException on MTI not set
     */
    public boolean isFinancial() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '2';
    }
    /**
     * Returns true if this is a file action message (MTI second digit = 3).
     * @return true if message class is "file action"
     * @exception ISOException on MTI not set
     */
    public boolean isFileAction() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '3';
    }
    /**
     * Returns true if this is a reversal message (MTI second digit = 4, last digit 0 or 1).
     * @return true if message class is "reversal"
     * @exception ISOException on MTI not set
     */
    public boolean isReversal() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '4' && (getMTI().charAt(3) == '0' || getMTI().charAt(3) == '1');
    }
    /**
     * Returns true if this is a chargeback message (MTI second digit = 4, last digit 2 or 3).
     * @return true if message class is "chargeback"
     * @exception ISOException on MTI not set
     */
    public boolean isChargeback() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '4' && (getMTI().charAt(3) == '2' || getMTI().charAt(3) == '3');
    }
    /**
     * Returns true if this is a reconciliation message (MTI second digit = 5).
     * @return true if message class is "reconciliation"
     * @exception ISOException on MTI not set
     */
    public boolean isReconciliation() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '5';
    }
    /**
     * Returns true if this is an administrative message (MTI second digit = 6).
     * @return true if message class is "administrative"
     * @exception ISOException on MTI not set
     */
    public boolean isAdministrative() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '6';
    }
    /**
     * Returns true if this is a fee collection message (MTI second digit = 7).
     * @return true if message class is "fee collection"
     * @exception ISOException on MTI not set
     */
    public boolean isFeeCollection() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '7';
    }
    /**
     * Returns true if this is a network management message (MTI second digit = 8).
     * @return true if message class is "network management"
     * @exception ISOException on MTI not set
     */
    public boolean isNetworkManagement() throws ISOException {
        return hasMTI() && getMTI().charAt(1) == '8';
    }
    /**
     * Returns true if this is a retransmission (MTI last digit = 1).
     * @return true if message is a retransmission
     * @exception ISOException on MTI not set
     */
    public boolean isRetransmission() throws ISOException {
        return getMTI().charAt(3) == '1';
    }
    /**
     * sets an appropriate response MTI.
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
     * sets an appropriate retransmission MTI<br>
     * @exception ISOException on MTI not set or it is not a request
     */
    public void setRetransmissionMTI() throws ISOException {
        if (!isRequest())
            throw new ISOException ("not a request");

        set (new ISOField (0, getMTI().substring(0,3) + "1"));
    }
    /**
     * Serializes the message header to the given ObjectOutput.
     * @param out the ObjectOutput to write to
     * @throws IOException on write error
     */
    protected void writeHeader (ObjectOutput out) throws IOException {
        int len = header.getLength();
        if (len > 0) {
            out.writeByte ('H');
            out.writeShort (len);
            out.write (header.pack());
        }
    }

    /**
     * Deserializes the message header from the given ObjectInput.
     * @param in the ObjectInput to read from
     * @throws IOException on read error
     * @throws ClassNotFoundException if a referenced class cannot be found
     */
    protected void readHeader (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        int len = in.readShort();
        if (len < 0)
            throw new InvalidObjectException("Invalid ISOMsg header length: " + len);
        byte[] b = new byte[len];
        in.readFully (b);
        setHeader (b);
    }
    /**
     * Serializes the packager class name to the given ObjectOutput.
     * @param out the ObjectOutput to write to
     * @throws IOException on write error
     */
    protected void writePackager(ObjectOutput out) throws IOException {
        out.writeByte('P');
        String pclass = packager.getClass().getName();
        byte[] b = pclass.getBytes(StandardCharsets.UTF_8);
        out.writeShort(b.length);
        out.write(b);
    }
    /**
     * Deserializes the packager from the given ObjectInput.
     * @param in the ObjectInput to read from
     * @throws IOException on read error
     * @throws ClassNotFoundException if the packager class cannot be found
     */
    protected void readPackager(ObjectInput in) throws IOException,
    ClassNotFoundException {
        int classNameLength = in.readShort();
        if (classNameLength <= 0 || classNameLength > MAX_PACKAGER_CLASS_NAME_LENGTH)
            throw new InvalidClassException("Invalid ISOPackager class name length: " + classNameLength);
        byte[] b = new byte[classNameLength];
        in.readFully(b);
        checkPackagerMetadataPolicy(in);
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> packagerClass = Class.forName(
                new String(b, StandardCharsets.UTF_8), false,
                loader != null ? loader : ISOMsg.class.getClassLoader()
            );
            if (!ISOPackager.class.isAssignableFrom(packagerClass))
                throw new InvalidClassException(packagerClass.getName(), "Class does not implement ISOPackager");
            checkPackagerClass(in, packagerClass);
            setPackager((ISOPackager) packagerClass.getDeclaredConstructor().newInstance());
        } catch (InvalidClassException e) {
            throw e;
        } catch (ReflectiveOperationException | LinkageError e) {
            InvalidClassException ice = new InvalidClassException("Unable to instantiate ISOPackager");
            ice.initCause(e);
            throw ice;
        }
    }

    private void checkPackagerMetadataPolicy(ObjectInput in) throws InvalidClassException {
        ExternalReadContext context = EXTERNAL_READ_CONTEXT.get();
        Boolean allowed = context != null ? context.allowPackagerMetadata : null;
        if (Boolean.FALSE.equals(allowed))
            throw new InvalidClassException("ISOPackager metadata deserialization is disabled");
        if (allowed == null && (!(in instanceof ObjectInputStream objectInputStream)
          || objectInputStream.getObjectInputFilter() == null))
            throw new InvalidClassException("ISOPackager metadata deserialization requires an explicit allow filter");
    }

    private void checkPackagerClass(ObjectInput in, Class<?> clazz) throws InvalidClassException {
        if (in instanceof ObjectInputStream objectInputStream) {
            ObjectInputFilter filter = objectInputStream.getObjectInputFilter();
            if (filter != null) {
                ObjectInputFilter.Status status = filter.checkInput(new ObjectInputFilter.FilterInfo() {
                    public Class<?> serialClass() { return clazz; }
                    public long arrayLength() { return -1; }
                    public long depth() { return 1; }
                    public long references() { return 1; }
                    public long streamBytes() { return -1; }
                });
                ExternalReadContext context = EXTERNAL_READ_CONTEXT.get();
                Boolean allowed = context != null ? context.allowPackagerMetadata : null;
                if (status == ObjectInputFilter.Status.REJECTED
                  || (allowed == null && status != ObjectInputFilter.Status.ALLOWED))
                    throw new InvalidClassException(clazz.getName(), "Rejected by deserialization filter");
            }
        }
    }

    /**
     * Reads an externalizable value while applying a policy to legacy ISOMsg
     * packager metadata. This method is intended for deserialization boundaries
     * that need to opt in explicitly for compatibility with streams containing
     * {@code P} records.
     *
     * @param value the value to read
     * @param in the input containing the external form
     * @param allowPackagerMetadata whether legacy packager metadata is allowed
     * @throws IOException on read error
     * @throws ClassNotFoundException if a referenced class cannot be found
     */
    public static void readExternal(
      Externalizable value, ObjectInput in, boolean allowPackagerMetadata
    ) throws IOException, ClassNotFoundException {
        if (!(value instanceof ISOMsg)) {
            value.readExternal(in);
            return;
        }
        ExternalReadContext context = EXTERNAL_READ_CONTEXT.get();
        boolean owner = context == null;
        if (owner) {
            context = new ExternalReadContext();
            EXTERNAL_READ_CONTEXT.set(context);
        }
        Boolean previous = context.allowPackagerMetadata;
        context.allowPackagerMetadata = allowPackagerMetadata;
        try {
            value.readExternal(in);
        } finally {
            context.allowPackagerMetadata = previous;
            if (owner)
                EXTERNAL_READ_CONTEXT.remove();
        }
    }
    /**
     * Serializes the message direction to the given ObjectOutput.
     * @param out the ObjectOutput to write to
     * @throws IOException on write error
     */
    protected void writeDirection (ObjectOutput out) throws IOException {
        out.writeByte ('D');
        out.writeByte (direction);
    }
    /**
     * Deserializes the message direction from the given ObjectInput.
     * @param in the ObjectInput to read from
     * @throws IOException on read error
     * @throws ClassNotFoundException if a class cannot be found
     */
    protected void readDirection (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        direction = in.readByte();
    }

    @Override
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

    @Override
    public void readExternal  (ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        ExternalReadContext context = EXTERNAL_READ_CONTEXT.get();
        boolean root = context == null;
        if (root) {
            context = new ExternalReadContext();
            EXTERNAL_READ_CONTEXT.set(context);
        }
        context.depth++;
        try {
            if (context.depth > MAX_EXTERNALIZED_DEPTH)
                throw new InvalidObjectException("Serialized ISOMsg nesting is too deep");
            in.readByte();  // ignore version for now
            fieldNumber = in.readShort();
            byte fieldType;
            ISOComponent c;
            try {
                while ((fieldType = in.readByte()) != 'E') {
                    if (++context.entries > MAX_EXTERNALIZED_ENTRIES)
                        throw new InvalidObjectException("Too many entries in serialized ISOMsg");
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
            } catch (ISOException e) {
                throw new IOException (e.getMessage());
            }
        } finally {
            context.depth--;
            if (root)
                EXTERNAL_READ_CONTEXT.remove();
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
     * Returns the associated ISOSource (e.g. the channel that received this message).
     * @return an ISOSource or null
     */
    public ISOSource getSource () {
        return sourceRef != null ? (ISOSource) sourceRef.get () : null;
    }
    private void writeExternal (ObjectOutput out, char b, ISOComponent c) throws IOException {
        out.writeByte (b);
        ((Externalizable) c).writeExternal (out);
    }
    private int parseInt (String s) {
        return s.startsWith("0x") ? Integer.parseInt(s.substring(2), 16) : Integer.parseInt(s);
    }

    private boolean setDatasetPath(String fpath, Object value) throws ISOException {
        DatasetAddress address = datasetAddress(fpath);
        if (address == null)
            return false;
        putDatasetElement(address, value);
        return true;
    }

    private boolean unsetDatasetPath(String fpath) throws ISOException {
        DatasetAddress address = datasetAddress(fpath);
        if (address == null)
            return false;
        removeDatasetElement(address);
        return true;
    }

    /**
     * Resolves a lenient top-level dataset path against this message's packager.
     *
     * @return the address, or {@code null} if fpath is not a dataset path
     * @throws ISOException if the field packager is a dataset packager and the
     *         number of segments does not match its dataset envelope
     */
    private DatasetAddress datasetAddress(String fpath) throws ISOException {
        StringTokenizer st = new StringTokenizer(fpath, ".");
        if (st.countTokens() < 2)
            return null;

        int fieldNo = parseInt(st.nextToken());
        Boolean envelope = datasetEnvelope(fieldNo);
        if (envelope == null)
            return null;
        int expected = envelope ? 2 : 1;
        if (st.countTokens() != expected)
            throw new ISOException(
              "Path '" + fpath + "' does not match the dataset " + (envelope ? "envelope" : "layout")
                + " of field " + fieldNo + " (expected " + expected + " segment" + (expected > 1 ? "s" : "")
                + " after the field)"
            );
        int datasetId = envelope ? parseInt(st.nextToken()) : fieldNo;
        int elementId = parseInt(st.nextToken());
        return new DatasetAddress(fieldNo, envelope, datasetId, elementId);
    }

    /**
     * Resolves the dataset element addressed by the segments of a strict path that
     * follow the dataset field at {@code path[index]}, held by this message.
     *
     * @return the address, or {@code null} if the segments do not address an element
     */
    private DatasetAddress datasetAddress(int[] path, int index) {
        int fieldNo = path[index];
        int remaining = path.length - index - 1;
        Boolean envelope = datasetEnvelope(fieldNo);
        if (envelope == null)
            envelope = remaining == 2;
        if (envelope)
            return remaining == 2 ? new DatasetAddress(fieldNo, true, path[index + 1], path[index + 2]) : null;
        return remaining == 1 ? new DatasetAddress(fieldNo, false, fieldNo, path[index + 1]) : null;
    }

    /**
     * @return whether this message's packager defines a dataset envelope for
     *         the field, or {@code null} if it does not define a dataset field there
     */
    private Boolean datasetEnvelope(int fieldNo) {
        if (packager instanceof ISOBasePackager bp
          && bp.getFieldPackager(fieldNo) instanceof DatasetFieldPackager dfp)
            return dfp.getISODatasetPackager().hasDatasetEnvelope();
        return null;
    }

    private void putDatasetElement(DatasetAddress address, Object value) throws ISOException {
        int elementId = address.elementId();
        ISOComponent elementComponent = toDatasetComponent(elementId, value);
        ISODatasetField field;
        ISOComponent component = getComponent(address.fieldNo());
        if (component == null) {
            field = new ISODatasetField(address.fieldNo());
            set(field);
        } else if (component instanceof ISODatasetField f) {
            field = f;
        } else {
            throw new ISOException("Field " + address.fieldNo() + " is not a dataset field");
        }

        ISODataset dataset = (ISODataset) field.getDataset(address.datasetId());
        if (dataset == null) {
            // without an envelope the dataset is raw TLV (i.e. ICC data);
            // otherwise ISO 8583:2023 identifiers above 0x70 are DBM
            DatasetFormat format = address.envelope() && address.datasetId() > 0x70 ? DatasetFormat.DBM : DatasetFormat.TLV;
            dataset = new ISODataset(address.datasetId(), format);
            field.addDataset(dataset);
        }
        dataset.putElement(elementId, elementComponent,
          dataset.getFormat() == DatasetFormat.TLV && DatasetElement.isConstructedTag(elementId));
    }

    private void removeDatasetElement(DatasetAddress address) throws ISOException {
        ISOComponent component = getComponent(address.fieldNo());
        if (component == null)
            return;
        if (!(component instanceof ISODatasetField field))
            throw new ISOException("Field " + address.fieldNo() + " is not a dataset field");

        for (Dataset dataset : field.getDatasets(address.datasetId())) {
            if (dataset instanceof ISODataset isoDataset) {
                isoDataset.removeElement(address.elementId());
                if (isoDataset.isEmpty())
                    field.removeDataset(isoDataset);
            }
        }
        if (!field.hasDatasets())
            unset(address.fieldNo());
    }

    private void writeDatasetElement(String fpath, boolean envelope, int datasetId, int elementId, Object value)
      throws ISOException {
        if (datasetId < 0 || elementId < 0)
            throw new IllegalArgumentException("Invalid dataset element " + datasetId + "/" + elementId + " for '" + fpath + "'");
        int[] path = parsePath(fpath);
        ISOMsg m = this;
        for (int i = 0; i < path.length - 1; i++) {
            ISOComponent c = m.getComponent(path[i]);
            if (c instanceof ISOMsg sub) {
                m = sub;
            } else if (value == null) {
                return; // nothing to remove
            } else if (c != null) {
                throw new ISOException("Field " + path[i] + " in path '" + fpath + "' is not a composite field");
            } else {
                ISOMsg sub = new ISOMsg(path[i]);
                m.set(sub);
                m = sub;
            }
        }
        int fieldNo = path[path.length - 1];
        DatasetAddress address = new DatasetAddress(fieldNo, envelope, envelope ? datasetId : fieldNo, elementId);
        if (value == null)
            m.removeDatasetElement(address);
        else
            m.putDatasetElement(address, value);
    }

    private List<DatasetElement> datasetElements(ISODatasetField field, int[] path, int index) {
        DatasetAddress address = datasetAddress(path, index);
        if (address == null)
            return Collections.emptyList();
        List<DatasetElement> elements = new ArrayList<>();
        for (Dataset dataset : field.getDatasets(address.datasetId()))
            elements.addAll(dataset.getElements(address.elementId()));
        return Collections.unmodifiableList(elements);
    }

    /**
     * Copies the dataset element addressed by {@code source} into this (cloned) message.
     * Same-identifier datasets are matched by position, so repeated datasets stay apart.
     */
    private void cloneDatasetElements(PathTarget source, int[] path, Map<ISODataset,ISODatasetField> createdDatasets)
      throws ISOException {
        DatasetAddress address = source.msg().datasetAddress(path, source.index());
        if (address == null)
            return;
        int elementId = address.elementId();
        List<Dataset> sourceDatasets = source.datasetField().getDatasets(address.datasetId());
        if (sourceDatasets.stream().allMatch(d -> d.getElements(elementId).isEmpty()))
            return;

        ISOMsg m = this;
        for (int i = 0; i < source.index(); i++) {
            ISOComponent c = m.getComponent(path[i]);
            ISOMsg sub;
            if (c instanceof ISOMsg msg) {
                sub = msg;
            } else {
                sub = new ISOMsg(path[i]);
                m.set(sub);
            }
            m = sub;
        }
        ISODatasetField field;
        ISOComponent c = m.getComponent(address.fieldNo());
        if (c instanceof ISODatasetField f) {
            field = f;
        } else if (c == null) {
            field = new ISODatasetField(address.fieldNo());
            m.set(field);
        } else {
            return; // cannot happen: the clone mirrors the source hierarchy
        }

        List<Dataset> targetDatasets = field.getDatasets(address.datasetId());
        for (int k = 0; k < sourceDatasets.size(); k++) {
            Dataset src = sourceDatasets.get(k);
            ISODataset dst;
            if (k < targetDatasets.size() && targetDatasets.get(k) instanceof ISODataset d) {
                dst = d;
            } else {
                dst = new ISODataset(address.datasetId(), src.getFormat());
                field.addDataset(dst);
                createdDatasets.put(dst, field);
            }
            dst.removeElement(elementId);
            for (DatasetElement element : src.getElements(elementId))
                dst.addElement(elementId, cloneDatasetComponent(element.getComponent()), element.isConstructed());
        }
    }

    /**
     * Walks a strict path.
     *
     * @return the message holding the component addressed by the last segment
     *         ({@code datasetField} is {@code null}), the message holding the dataset
     *         field at {@code path[index]} that the path descends into, or {@code null}
     *         if the path runs through an absent or leaf field
     */
    private PathTarget locate(int[] path) {
        ISOMsg m = this;
        for (int i = 0; i < path.length - 1; i++) {
            ISOComponent c = m.getComponent(path[i]);
            if (c instanceof ISODatasetField field)
                return new PathTarget(m, i, field);
            if (!(c instanceof ISOMsg sub))
                return null;
            m = sub;
        }
        return new PathTarget(m, path.length - 1, null);
    }

    /**
     * Parses a strict field path. Segments are decimal ({@code 62}) or
     * {@code 0x}-prefixed hexadecimal ({@code 0x9F26}) numbers in the range
     * {@code 0..Integer.MAX_VALUE}.
     *
     * @throws IllegalArgumentException if fpath is malformed
     */
    private static int[] parsePath(String fpath) {
        if (fpath == null || fpath.isEmpty())
            throw new IllegalArgumentException("Invalid path '" + fpath + "': empty path");
        String[] segments = fpath.split("\\.", -1);
        int[] path = new int[segments.length];
        for (int i = 0; i < segments.length; i++)
            path[i] = parsePathSegment(fpath, segments[i], i + 1);
        return path;
    }

    private static int parsePathSegment(String fpath, String segment, int position) {
        if (segment.isEmpty())
            throw new IllegalArgumentException("Invalid path '" + fpath + "': empty segment at position " + position);
        boolean hex = segment.startsWith("0x");
        String digits = hex ? segment.substring(2) : segment;
        boolean valid = !digits.isEmpty();
        for (int i = 0; valid && i < digits.length(); i++) {
            char ch = digits.charAt(i);
            valid = ch >= '0' && ch <= '9' || hex && (ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F');
        }
        if (!valid)
            throw new IllegalArgumentException(
              "Invalid path '" + fpath + "': invalid segment '" + segment + "' at position " + position);
        try {
            return Integer.parseInt(digits, hex ? 16 : 10);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
              "Invalid path '" + fpath + "': segment '" + segment + "' at position " + position + " is out of range", e);
        }
    }

    private record PathTarget(ISOMsg msg, int index, ISODatasetField datasetField) { }

    private record DatasetAddress(int fieldNo, boolean envelope, int datasetId, int elementId) { }

    private ISOComponent toDatasetComponent(int elementId, Object value) throws ISOException {
        if (value instanceof ISOComponent) {
            ISOComponent component = (ISOComponent) value;
            component.setFieldNumber(elementId);
            return component;
        }
        if (value instanceof byte[]) {
            return new ISOBinaryField(elementId, (byte[]) value);
        }
        if (value instanceof String) {
            return new ISOField(elementId, (String) value);
        }
        throw new ISOException("Unsupported dataset value type " + (value != null ? value.getClass().getName() : "null"));
    }

    private ISOComponent cloneComponent(ISOComponent c) throws ISOException {
        if (c instanceof ISOMsg)
            return (ISOComponent) ((ISOMsg) c).clone();
        if (c instanceof ISODatasetField)
            return cloneDatasetField((ISODatasetField) c);
        return c;
    }

    private ISODatasetField cloneDatasetField(ISODatasetField field) throws ISOException {
        ISODatasetField clone = new ISODatasetField(field.getFieldNumber());
        for (Dataset dataset : field.getDatasets()) {
            clone.addDataset(cloneDataset(dataset));
        }
        return clone;
    }

    private ISODataset cloneDataset(Dataset dataset) throws ISOException {
        ISODataset clone = new ISODataset(dataset.getIdentifier(), dataset.getFormat());
        for (DatasetElement element : dataset.getElements()) {
            clone.addElement(element.getId(), cloneDatasetComponent(element.getComponent()), element.isConstructed());
        }
        return clone;
    }

    private ISOComponent cloneDatasetComponent(ISOComponent component) throws ISOException {
        if (component instanceof ISOMsg)
            return (ISOComponent) ((ISOMsg) component).clone();
        if (component instanceof ISOBinaryField)
            return new ISOBinaryField(component.getFieldNumber(), component.getBytes() != null ? component.getBytes().clone() : null);
        if (component instanceof ISOField)
            return new ISOField(component.getFieldNumber(), (String) component.getValue());
        return component;
    }
}
