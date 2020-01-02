/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.tlv;

import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author bharavi
 */

public class TLVList implements Serializable, Loggeable {

    private static final long serialVersionUID = 6962311407331957465L;

    /**
     * Value not used as tag id in accordance with ISO/IEC 7816.
     */
    private static final int SKIP_BYTE1     = 0x00;

    /**
     * Value not used as tag id in accordance with ISO/IEC 7816.
     */
    private static final int SKIP_BYTE2     = 0xFF;

    private static final int EXT_TAG_MASK   = 0x1F;

    private static final int LEN_SIZE_MASK  = 0x7F;
    private static final int EXT_LEN_MASK   = 0x80;

    private final List<TLVMsg> tags = new ArrayList<>();

    /**
     * Enforces fixed tag size.
     * <p>
     * Zero means that the tag size will be determined in accordance with
     * ISO/IEC 7816.
     */
    private int tagSize = 0;

    /**
     * Enforces fixed length size.
     * <p>
     * Zero means that the length size will be determined in accordance with
     * ISO/IEC 7816.
     */
    private int lengthSize = 0;

    private int tagToFind = -1;
    private int indexLastOccurrence = -1;

    public static class TLVListBuilder {

        private int tagSize = 0;
        private int lengthSize = 0;

        /**
         * Creates instance of TLV engine builder.
         *
         * @return instance of TLV builder.
         */
        public static TLVListBuilder createInstance() {
            return new TLVListBuilder();
        }

        /**
         * Forces a fixed size of tag.
         * <p>
         * It disables tag size autodetection according with ISO/IEC 7816-4
         * BER-TLV.
         *
         * @param tagSize The size of tag in bytes
         * @return TLVList builder with fixed tag size
         */
        public TLVListBuilder fixedTagSize(int tagSize) {
            if (tagSize <= 0)
                throw new IllegalArgumentException("The fixed tag size must be greater than zero");

            this.tagSize = tagSize;
            return this;
        }

        /**
         * Forces a fixed size of length.
         * <p>
         * It disables length size autodetection according with ISO/IEC 7816-4
         * BER-TLV.
         *
         * @param lengthSize The size of length in bytes <i>(1 - 4)</i>
         * @return TLVList builder with fixed length size
         */
        public TLVListBuilder fixedLengthSize(int lengthSize) {
            if (lengthSize <= 0)
                throw new IllegalArgumentException("The fixed length size must be greater than zero");

            if (lengthSize > 4)
                throw new IllegalArgumentException("The fixed length size must be greater than zero");

            this.lengthSize = lengthSize;
            return this;
        }

        /**
         * Build TLV engine.
         *
         * @return configured TLV engine
         */
        public TLVList build() {
            TLVList tl = new TLVList();
            tl.tagSize = tagSize;
            tl.lengthSize = lengthSize;
            return tl;
        }

    }

    /**
     * Creates instance of TLV engine.
     * <p>
     * It is a shorter form of:
     * <pre>{@code
     *   TLVListBuilder.createInstance().build();
     * }</pre>
     *
     */
    public TLVList() {
        super();
    }

    /**
     * Unpack a message.
     *
     * @param buf raw message
     * @throws IllegalArgumentException
     */
    public void unpack(byte[] buf) throws IllegalArgumentException {
        unpack(buf, 0);
    }

    /**
     * @return a list of tags.
     */
    public List<TLVMsg> getTags() {
        return tags;
    }

    /**
     * @return an enumeration of the List of tags.
     */
    public Enumeration<TLVMsg> elements() {
        return Collections.enumeration(tags);
    }

    /**
     * Unpack a message with a starting offset.
     *
     * @param buf raw message
     * @param offset the offset
     * @throws IndexOutOfBoundsException if {@code offset} exceeds {code buf.length}
     * @throws IllegalArgumentException
     */
    public void unpack(byte[] buf, int offset) throws IllegalArgumentException
            , IndexOutOfBoundsException {
        ByteBuffer buffer = ByteBuffer.wrap(buf, offset, buf.length - offset);
        TLVMsg currentNode;
        while (buffer.hasRemaining()) {
            currentNode = getTLVMsg(buffer);    // null is returned if no tag found (trailing padding)
            if (currentNode != null)
                append(currentNode);
        }
    }

    /**
     * Append TLVMsg to the TLV list.
     *
     * @param tlv the TLV message
     * @throws NullPointerException if {@code tlv} is {@code null}
     */
    public void append(TLVMsg tlv) throws NullPointerException {
        Objects.requireNonNull(tlv, "TLV message cannot be null");

        tags.add(tlv);
    }

    /**
     * Append TLVMsg to the TLVList.
     *
     * @param tag tag id
     * @param value tag value
     * @return the TLV list instance
     * @throws IllegalArgumentException when contains tag with illegal id
     */
    public TLVList append(int tag, byte[] value) throws IllegalArgumentException {
        append(createTLVMsg(tag, value));
        return this;
    }

    /**
     * Append TLVMsg to the TLVList.
     *
     * @param tag id
     * @param value in hexadecimal character representation
     * @return the TLV list instance
     * @throws IllegalArgumentException when contains tag with illegal id
     */
    public TLVList append(int tag, String value) throws IllegalArgumentException {
        append(createTLVMsg(tag, ISOUtil.hex2byte(value)));
        return this;
    }

    /**
     * delete the specified TLV from the list using a Zero based index
     * @param index number
     */
    public void deleteByIndex(int index) {
        tags.remove(index);
    }

    /**
     * Delete the specified TLV from the list by tag value
     * @param tag id
     */
    public void deleteByTag(int tag) {
        List<TLVMsg> t = new ArrayList<>();
        for (TLVMsg tlv2 : tags) {
            if (tlv2.getTag() == tag)
                t.add(tlv2);
        }
        tags.removeAll(t);
    }

    /**
     * Searches the list for a specified tag and returns a TLV object.
     *
     * @param tag id
     * @return TLV message
     */
    public TLVMsg find(int tag) {
        tagToFind = tag;
        for (TLVMsg tlv : tags) {
            if (tlv.getTag() == tag) {
                indexLastOccurrence = tags.indexOf(tlv);
                return tlv;
            }
        }
        indexLastOccurrence = -1;
        return null;
    }

    /**
     * Searches the list for a specified tag and returns a zero based index for
     * that tag.
     *
     * @param tag tag identifier
     * @return index for a given {@code tag}
     */
    public int findIndex(int tag) {
        tagToFind = tag;
        for (TLVMsg tlv : tags) {
            if (tlv.getTag() == tag) {
                indexLastOccurrence = tags.indexOf(tlv);
                return indexLastOccurrence;
            }
        }
        indexLastOccurrence = -1;
        return -1;
    }

    /**
     * Return the next TLVMsg of same TAG value.
     *
     * @return TLV message or {@code null} if not found.
     * @throws IllegalStateException when the search has not been initiated
     */
    public TLVMsg findNextTLV() throws IllegalStateException {
        if (tagToFind < 0)
            throw new IllegalStateException(
                    "The initialization of the searched tag is required"
            );
        for ( int i=indexLastOccurrence + 1 ; i < tags.size(); i++) {
            if (tags.get(i).getTag() == tagToFind) {
                indexLastOccurrence = i;
                return tags.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a {@code TLVMsg} instance stored within the {@code TLVList} at
     * the given {@code index}.
     *
     * @param index zero based index of TLV message
     * @return TLV message instance
     * @throws IndexOutOfBoundsException if the index is out of range
     * (index < 0 || index >= size())
     */
    public TLVMsg index(int index) throws IndexOutOfBoundsException {
        return tags.get(index);
    }

    /**
     * Pack the TLV message (BER-TLV Encoding).
     *
     * @return the packed message
     */
    public byte[] pack() {
        ByteBuffer buffer = ByteBuffer.allocate(516);
        for (TLVMsg tlv : tags)
            buffer.put(tlv.getTLV());
        byte[] b = new byte[buffer.position()];
        buffer.flip();
        buffer.get(b);
        return b;
    }

    private boolean isExtTagByte(int b) {
        return (b & EXT_TAG_MASK) == EXT_TAG_MASK;
    }

    /**
     * Read next TLV Message from stream and return it.
     *
     * @param buffer the buffer
     * @return TLVMsg
     * @throws IllegalArgumentException
     */
    private TLVMsg getTLVMsg(ByteBuffer buffer) throws IllegalArgumentException {
        int tag = getTAG(buffer);  // tag id 0x00 if tag not found
        if (tagSize == 0 && tag == SKIP_BYTE1)
            return null;

        // Get Length if buffer remains!
        if (!buffer.hasRemaining())
            throw new IllegalArgumentException(String.format("BAD TLV FORMAT: tag (%x)"
                    + " without length or value",tag)
            );
        int length = getValueLength(buffer);
        if (length > buffer.remaining())
            throw new IllegalArgumentException(String.format("BAD TLV FORMAT: tag (%x)"
                    + " length (%d) exceeds available data", tag, length)
            );
        byte[] arrValue = new byte[length];
        buffer.get(arrValue);

        return createTLVMsg(tag, arrValue);
    }

    /**
     * Create TLV message instance.
     *
     * @apiNote The protected scope is intended to not promote the use of TLVMsg
     * outside.
     *
     * @param tag tag identifier
     * @param value the value of tag
     * @return TLV message instance
     * @throws IllegalArgumentException when contains tag with illegal id
     */
    protected TLVMsg createTLVMsg(int tag, byte[] value) throws IllegalArgumentException {
        return new TLVMsg(tag, value, tagSize, lengthSize);
    }

    /**
     * Skip padding bytes of TLV message.
     * <p>
     * ISO/IEC 7816 uses neither ’00’ nor ‘FF’ as tag value.
     *
     * @param buffer sequence of TLV data bytes
     */
    private void skipBytes(ByteBuffer buffer) {
        buffer.mark();
        int b;
        do {
            if (!buffer.hasRemaining())
                break;

            buffer.mark();
            b = buffer.get() & 0xff;
        } while (b == SKIP_BYTE1 || b == SKIP_BYTE2);
        buffer.reset();
    }

    private int readTagID(ByteBuffer buffer) throws IllegalArgumentException {
        // Get first byte of Tag Identifier
        int b = buffer.get() & 0xff;
        int tag = b;
        if (isExtTagByte(b)) {
            // Get rest of Tag identifier
            do {
                tag <<= 8;
                if (buffer.remaining() < 1)
                    throw new IllegalArgumentException("BAD TLV FORMAT: encoded tag id is too short");

                b = buffer.get() & 0xff;
                tag |= b;
            } while ((b & EXT_LEN_MASK) == EXT_LEN_MASK);
        }
        return tag;
    }

    /**
     * Return the next Tag identifier.
     *
     * @param buffer contains TLV data
     * @return tag identifier
     * @throws IllegalArgumentException
     */
    private int getTAG(ByteBuffer buffer) throws IllegalArgumentException {
        if (tagSize > 0)
            return bytesToInt(readBytes(buffer, tagSize));

        skipBytes(buffer);
        return readTagID(buffer);
    }

    /**
     * Read length bytes and return the int value
     * @param buffer buffer
     * @return value length
     * @throws IllegalArgumentException
     */
    protected int getValueLength(ByteBuffer buffer) throws IllegalArgumentException {
        if (lengthSize > 0) {
            byte[] bb = readBytes(buffer, lengthSize);
            return bytesToInt(bb);
        }

        byte b = buffer.get();
        int count = b & LEN_SIZE_MASK;
        // check first byte for more bytes to follow
        if ((b & EXT_LEN_MASK) == 0 || count == 0)
            return count;

        //fetch rest of bytes
        byte[] bb = readBytes(buffer, count);
        return bytesToInt(bb);
    }

    private int bytesToInt(byte[] bb){
        //adjust buffer if first bit is turn on
        //important for BigInteger reprsentation
        if ((bb[0] & 0x80) > 0)
            bb = ISOUtil.concat(new byte[1], bb);

        return new BigInteger(bb).intValue();
    }

    private byte[] readBytes(ByteBuffer buffer, int length) throws IllegalArgumentException {
        if (length > buffer.remaining())
            throw new IllegalArgumentException(
                    String.format("BAD TLV FORMAT: (%d) remaining bytes are not"
                            + " enough to get tag id of length (%d)"
                            , buffer.remaining(), length
                    )
            );
        byte[] bb = new byte[length];
        buffer.get(bb);
        return bb;
    }

    /**
     * searches the list for a specified tag and returns a hex String
     * @param tag id
     * @return hexString
     */
    public String getString(int tag) {
        TLVMsg msg = find(tag);
        if (msg == null)
            return null;

        return msg.getStringValue();
    }

    /**
     * searches the list for a specified tag and returns it raw
     * @param tag id
     * @return byte[]
     */
    public byte[] getValue(int tag) {
        TLVMsg msg = find(tag);
        if (msg == null)
            return null;

        return msg.getValue();
    }

    /**
     * Indicates if TLV measege with passed {@code tag} is on list.
     *
     * @param tag tag identifier
     * @return {@code true} if tag contains on list, {@code false} otherwise
     */
    public boolean hasTag(int tag) {
        return findIndex(tag) > -1;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        String inner = indent + "   ";
        p.println(indent + "<tlvlist>");
        for (TLVMsg msg : getTags())
            msg.dump(p, inner);
        p.println(indent + "</tlvlist>");
    }

}
