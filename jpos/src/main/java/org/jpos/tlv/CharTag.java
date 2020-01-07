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

/**
 * Class represents TLV Tag stored as sequence of characters.
 * <p>
 * Processing format:
 * <ul>
 *   <li><tt>TAG</tt> - the identifier of the tag</li>
 *   <li><tt>LEN</tt> - the length <i>(encoded as decimal digits)</i> of the tag value</li>
 *   <li><tt>VAL</tt> - the value of the tag or missing if length is 0</li>
 * </ul>
 *
 * @author Robert Demski &lt;drdemsey@gmail.com>
 */
public class CharTag {

    protected int lengthSize = 0x03;

    private final String tagId;
    private final String value;

    private boolean swapTagWithLength;

    /**
     * Internal Tag constructor.
     *
     * @apiNote this is internal method should stay in protected scope
     *
     * @param tagId tag identifier, not {@code null}
     * @param value tag value
     */
    protected CharTag(String tagId, String value) {
        this.tagId = tagId;
        this.value = value;
    }

    /**
     * Sets size of length element.
     *
     * @param size size of length element
     */
    protected void setLengthSize(int size) {
        lengthSize = size;
    }

    /**
     * Swap tag with length.
     *
     * @param swap indicates if tag element will be swapped with length element
     */
    protected void withTagLengthSwap(boolean swap) {
        swapTagWithLength = swap;
    }

    /**
     * Form TLV for this tag.
     *
     * @return TLV string
     */
    public String getTLV() {
        int vLen = 0;
        if (value != null)
            vLen = value.length();

        String length = ISOUtil.zeropad(vLen, lengthSize);
        String tlv;
        if (swapTagWithLength)
            tlv = length + tagId;
        else
            tlv = tagId + length;

        if (vLen == 0)
            return tlv;

        return tlv + value;
    }

    /**
     * Gets tag identifier.
     *
     * @return tag identifier
     */
    public String getTagId() {
        return tagId;
    }

    /**
     * Gets tag value.
     *
     * @return tag value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        int vLen = 0;
        if (value != null)
            vLen = value.length();

        int sbSize = tagId.length() + lengthSize + vLen + 32;
        StringBuilder sb = new StringBuilder(sbSize)
            .append("tag: ")
            .append(tagId)
            .append(", len: ")
            .append(vLen);
        if (vLen > 0)
            sb.append(", value: ")
              .append(value);

        return sb.toString();
    }

}
