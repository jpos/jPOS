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

import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.util.HashMap;

/**
 * Class represents TLV tag map encoded as sequence of characters.
 * <p>
 * The {@code CharTagMap} features:
 * <ul>
 *   <li>operates ({@code pack} and {@code unpack}) on character sequences
 *   <li>only one occurrence of the tag in the sequence is possible
 *   <li>after build the tag size and length size are fixed
 *   <li>length is encoded as decimal characters
 * </ul>
 *
 * @author Grzegorz Wieczorek <grw1@wp.pl>
 */
public class CharTagMap extends HashMap<String, CharTag> {

    static final String EXCEPTION_PREFIX = "BAD TLV FORMAT:";

    private int tagLen = 0x02;
    private int lenLen = 0x03;

    private boolean swapTagWithLength;

    /**
     * Creates new empty instance of text TLV tag map.
     * <p>
     * This method creates default TLV tag map which works on TLV data with
     * followng parameters:
     * <ul>
     *   <li><em>TT</em> - 2 <tt>ASCII</tt> characters of tag identifier
     *   <li><em>LLL</em> - 3 <tt>ASCII</tt> encoded decimal digits represents
     *      tag value length
     *   </li>
     *   <li><em>VAL</em> - 0 or more <i>(up to 999)</i> <tt>ASCII</tt>
     *      encoded characters represents tag value
     *   </li>
     * </ul>
     *
     * @return new default sized instance of {@code CharTagMap}
     */
    public static CharTagMap getInstance() {
        return new CharTagMap();
    }

    /**
     * Sets size of tag element.
     *
     * @param size size of tag elament
     */
    protected void setTagSize(int size) throws IllegalArgumentException {
        if (size < 1)
            throw new IllegalArgumentException("The size of the tag should be greater than 0");

        if (size > 4)
            throw new IllegalArgumentException("The size of the tag should not be greater than 4");

        tagLen = size;
    }

    /**
     * Sets size of length element.
     *
     * @param size size of length elament
     */
    protected void setLengthSize(int size) throws IllegalArgumentException {
        if (size < 1)
            throw new IllegalArgumentException("The size of the length should be greater than 0");

        if (size > 5)
            throw new IllegalArgumentException("The size of the length should be less than 5");

        lenLen = size;
    }

    /**
     * Sets size of length element.
     *
     * @param swap indicates if tag element will be swapped with length element
     */
    protected void withTagLengthSwap(boolean swap) {
        swapTagWithLength = swap;
    }

    /**
     * Unpack string to TLV tag map.
     *
     * @param data sequence of characters encoded as TLV
     * @throws IllegalArgumentException if {@code null} or parsing error occurs
     */
    public void unpack(CharSequence data) throws IllegalArgumentException {
        if (data == null)
            throw new IllegalArgumentException("TLV data are required to unpack");

        CharBuffer buffer = CharBuffer.wrap(data);
        CharTag currentTag;
        while (buffer.hasRemaining()) {
            currentTag = getTLVMsg(buffer);
            put(currentTag.getTagId(), currentTag);
        }
    }

    /**
     * Pack TLV Tags.
     *
     * @return string containing tags in TLV Format
     */
    public String pack() {
        StringBuilder sb = new StringBuilder();
        for (CharTag tag : values())
            sb.append(tag.getTLV());
        return sb.toString();
    }

    /**
     * Adds a new tag to map.
     *
     * @param tagId tag identifier, not {@code null}
     * @param value tag value
     * @return tag map instance for chaining
     * @throws IllegalArgumentException if {@code tagId} is {@code null} or has
     * invalid length.
     */
    public CharTagMap addTag(String tagId, String value) throws IllegalArgumentException {
        put(tagId, createTLV(tagId, value));
        return this;
    }

    /**
     * Create new TLV tag.
     *
     * @param tagId tag identifier, not {@code null}
     * @param value tag value
     * @return TLV instance
     * @throws IllegalArgumentException if {@code tagId} is {@code null} or has
     * invalid length.
     */
    public CharTag createTLV(String tagId, String value) throws IllegalArgumentException {
        validateTag(tagId);

        int maxValueLength = (int) Math.pow(BigDecimal.TEN.doubleValue(), lenLen) - 1;
        if (value != null && value.length() > maxValueLength)
            throw new IllegalArgumentException(
                String.format("The value size %d of the tag '%s' has"
                        + " exceeded the maximum allowable value %d"
                        , value.length(), tagId, maxValueLength
                )
            );

        CharTag tag = new CharTag(tagId, value);
        tag.setLengthSize(lenLen);
        tag.withTagLengthSwap(swapTagWithLength);
        return tag;
    }

    protected void validateTag(String tagId) throws IllegalArgumentException {
        if (tagId == null)
            throw new IllegalArgumentException("Tag identifier have to be specified");

        if (tagId.length() != tagLen)
            throw new IllegalArgumentException(
                String.format("Invalid tag '%s' size: expected %d, but got %d"
                        , tagId, tagLen, tagId.length()
                )
            );
    }

    /**
     * Gets the value of the tag with given tagId from map.
     *
     * @param tagId tag identifier
     * @return value tag value
     */
    public String getTagValue(String tagId) {
        CharTag t = get(tagId);
        return t == null ? null : t.getValue();
    }

    /**
     * Chceck if the tag with given tag identifier is in this tag map.
     *
     * @param tagId tag identifier
     * @return {@code true} if this map contains the tag, otherwise return {@code false}
     */
    public boolean hasTag(String tagId) {
        return containsKey(tagId);
    }

    private String stripTagId(CharBuffer buffer) throws IllegalArgumentException {
        if (buffer.remaining() < tagLen)
            throw new IllegalArgumentException(
                String.format(
                    "%s tag id requires %d characters", EXCEPTION_PREFIX, tagLen
                )
            );

        return getStr(buffer, tagLen);
    }

    private int stripLength(CharBuffer buffer) throws IllegalArgumentException {
        if (buffer.remaining() < lenLen)
            throw new IllegalArgumentException(
                String.format(
                    "%s tag length requires %d digits", EXCEPTION_PREFIX, lenLen
                )
            );

        return Integer.parseInt(getStr(buffer, lenLen));
    }

    private CharTag getTLVMsg(CharBuffer buffer) throws IllegalArgumentException {
        String tagId;
        int len;
        if (swapTagWithLength) {
            len = stripLength(buffer);
            tagId = stripTagId(buffer);
        } else {
            tagId = stripTagId(buffer);
            len = stripLength(buffer);
        }

        if (buffer.remaining() < len)
            throw new IllegalArgumentException(
                String.format("%s tag '%s' length '%03d' exceeds available"
                        + " data length '%03d'.", EXCEPTION_PREFIX
                        , tagId, len, buffer.remaining()
                )
            );
        String value = getStr(buffer, len);
        return createTLV(tagId, value);
    }

    private String getStr(CharBuffer buffer, int len) {
        char[] ca = new char[len];
        buffer.get(ca);
        return String.valueOf(ca);
    }

}
