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

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Arrays;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

/**
 * @author bharavi
 */
public class TLVMsg implements Loggeable {

    /**
     * Value not used as tag id in accordance with ISO/IEC 7816.
     */
    private static final int SKIP_BYTE1     = 0x00;

    /**
     * Value not used as tag id in accordance with ISO/IEC 7816.
     */
    private static final int SKIP_BYTE2     = 0xFF;

    private static final int EXT_TAG_MASK   = 0x1F;

    /**
     * Enforces fixed tag size.
     * <p>
     * Zero means that the tag size will be determined in accordance with
     * ISO/IEC 7816.
     */
    private final int tagSize;

    /**
     * Enforces fixed length size.
     * <p>
     * Zero means that the length size will be determined in accordance with
     * ISO/IEC 7816.
     */
    private final int lengthSize;

    private final int tag;
    private final byte[] value;

    /**
     * Constructs a TLV message from tag and value.
     *
     * @param tag id
     * @param value tag value
     * @deprecated In most cases, a message is created to attach it to the list.
     * <br>
     * It can be done by:
     * <pre>{@code
     *   TLVList tl = ...;
     *   tl.append(tag, value);
     * }</pre>
     * If for some reason this is not possible then a message can be created:
     * <pre>{@code
     *   TLVList tl = TLVListBuilder.createInstance().build(); // or just new TLVList();
     *   tl.append(tag, value);
     *   TLVMsg tm = tl.find(tag);
     * }</pre>
     * The intention is to not promote the use of TLVMsg outside. Due to
     * the lack of compatibility of various TLV types at TLVList.append(TLVMsg)
     */
    @Deprecated
    public TLVMsg(int tag, byte[] value) throws IllegalArgumentException {
        this(tag, value, 0, 0);
    }

    protected TLVMsg(int tag, byte[] value, int tagSize, int lengthSize)
            throws IllegalArgumentException {
        this.tag = tag;
        this.value = value;
        this.tagSize = tagSize;
        this.lengthSize = lengthSize;

        if (tagSize == 0)
            verifyTag(tag);
        else
            verifyTagLength(tag);

        if (lengthSize > 0)
            verifyValue(value);

    }

    private boolean isExtTagByte(int b) {
        return (b & EXT_TAG_MASK) == EXT_TAG_MASK;
    }

    /**
     * Verify tag identifier.
     * <p>
     * Tag number in accordance with ISO/IEC 7816
     * <ol>
     *   <li>The tag field consists of one or more consecutive bytes. It a number.
     *   <li>ISO/IEC 7816 uses neither ’00’ nor ‘FF’ as tag value.
     *   <li>If the bits B5-B1 of the leading byte are not all set to 1, then
     *       may they shall encode an integer equal to the tag number. Then the
     *       tag field consists of a single byte.
     *       Otherwise <i>(B5-B1 set to 1 in the leading byte)</i>, the tag
     *       field shall continue on one or more subsequent bytes.
     *   </li>
     * </ol>
     *
     * See <a href="http://cardwerk.com/iso7816-4-annex-d-use-of-basic-encoding-rules-asn-1/#AnnexD_2">ISO 7816-4 Annex D.2: Tag field</a>
     *
     * @param tag tag identifier
     * @throws IllegalArgumentException if tag identifier is zero or less or
     * it is included in the illegal ranges.
     */
    private void verifyTag(int tag) throws IllegalArgumentException {
        if (tag <= 0)
            throw new IllegalArgumentException("Tag id must be greater than zero");

        BigInteger bi = BigInteger.valueOf(tag);
        byte[] ba = bi.toByteArray();
        if (ba[0] == 0x00)
            // strip byte array if starts with 0x00
            ba = Arrays.copyOfRange(ba, 1, ba.length);

        int idx = 0;
        do {
            if ((ba[idx] & 0xff) == SKIP_BYTE1) {
                throw new IllegalArgumentException("Tag id: 0x" + Integer.toString(tag, 0x10).toUpperCase()
                        + " cannot contain in any 0x00 byte"
                );
            } else if ((ba[idx] & 0xff) == SKIP_BYTE2) {
                throw new IllegalArgumentException("Tag id: 0x" + Integer.toString(tag, 0x10).toUpperCase()
                        + " cannot contain in any 0xff byte"
                );
            } else if (isExtTagByte(ba[idx])) {
                if (ba.length <= idx + 1)
                    throw new IllegalArgumentException("Tag id: 0x" + Integer.toString(tag, 0x10).toUpperCase()
                            + " shall contain subsequent byte"
                    );
            } else if (idx + 1 < ba.length)
                throw new IllegalArgumentException("Tag id: 0x" + Integer.toString(tag, 0x10).toUpperCase()
                        + " cannot contain subsequent byte"
                );
            idx++;
        } while (idx < ba.length);
    }

    private void verifyTagLength(int tag) throws IllegalArgumentException {
        if (tag < 0)
            throw new IllegalArgumentException("The tag id must be greater than or equals zero");

        int maxTag = 1 << (tagSize << 3);
        maxTag -= 1;
        if (tag > maxTag)
            throw new IllegalArgumentException("The tag id cannot be greater that: " + maxTag);
    }

    private void verifyValue(byte[] value) throws IllegalArgumentException {
        if (value == null)
            return;

        int maxLength = 1 << (lengthSize << 3);
        maxLength -= 1;
        if (value.length > maxLength)
            throw new IllegalArgumentException("The tag value length cannot exceed: " + maxLength);
    }

    /**
     * @return tag
     */
    public int getTag() {
        return tag;
    }

    /**
     * @return tag value
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * @return tag + length + value of the TLV Message
     */
    public byte[] getTLV() {
        String hexTag = Integer.toHexString(tag);
        byte[] bTag = ISOUtil.hex2byte(hexTag);
        if (tagSize > 0)
            bTag = fitInArray(bTag, tagSize);

        byte[] bLen = getL();
        byte[] bVal = getValue();
        if (bVal == null)
            //Value can be null
            bVal = new byte[0];

        int tLength = bTag.length + bLen.length + bVal.length;
        byte[] out = new byte[tLength];
        System.arraycopy(bTag, 0, out, 0, bTag.length);
        System.arraycopy(bLen, 0, out, bTag.length, bLen.length);
        System.arraycopy(bVal, 0, out, bTag.length + bLen.length,
                bVal.length
        );
        return out;
    }

    private byte[] fitInArray(byte[] bytes, int length) {
        byte[] ret = new byte[length];
        if (bytes.length <= length)
            // copy bytes at end of ret
            System.arraycopy(bytes, 0, ret, ret.length - bytes.length, bytes.length);
        else
            // copy last tagSize bytes of bytes
            System.arraycopy(bytes, bytes.length - ret.length, ret, 0, ret.length);
        return ret;
    }

    private byte[] getLengthArray() {
        int length = 0;
        if (value != null)
            length = value.length;

        byte[] ret = BigInteger.valueOf(length).toByteArray();
        return fitInArray(ret, lengthSize);
    }

    /**
     * Value up to 127 can be encoded in single byte and multiple bytes are
     * required for length bigger than 127
     *
     * @return encoded length
     */
    public byte[] getL() {

        if (lengthSize > 0)
            return getLengthArray();

        if (value == null)
            return new byte[1];

        // if Length is less than 128
        // set the 8bit as 0 indicating next 7 bits is the length
        // of the message
        // if length is more than 127 then, set the first bit as 1 indicating
        // next 7 bits will indicate the length of following bytes used for
        // length

        BigInteger bi = BigInteger.valueOf(value.length);
        /* Value to be encoded on multiple bytes */
        byte[] rBytes = bi.toByteArray();
        /* If value can be encoded on one byte */
        if (value.length < 0x80)
            return rBytes;

        //we need 1 byte to indicate the length
        //for that is used sign byte (first 8-bits equals 0),
        //if it is not present it is added
        if (rBytes[0] > 0)
            rBytes = ISOUtil.concat(new byte[1], rBytes);
        rBytes[0] = (byte) (0x80 | rBytes.length - 1);

        return rBytes;
    }

    /**
     * @return value
     */
    public String getStringValue() {
        return ISOUtil.hexString(value);
    }

    @Override
    public String toString(){
        String t = Integer.toHexString(tag);
        if (t.length() % 2 > 0)
            t = "0" + t;
        return String.format("[tag: 0x%s, %s]", t
                ,value == null ? null : getStringValue()
        );
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.print(indent);
        p.print("<tag id='");
        p.print(Integer.toHexString(getTag()));
        p.print("' value='");
        p.print(ISOUtil.hexString(getValue()));
        p.println("' />");
    }

}
