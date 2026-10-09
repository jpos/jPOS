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

import java.util.Arrays;

/**
 * Holds one decoded element inside a dataset.
 */
public class DatasetElement {
    private final int id;
    private final ISOComponent component;
    private final boolean constructed;

    /**
     * Creates a primitive dataset element.
     *
     * @param id element identifier, either a TLV tag or DBM bit number
     * @param component backing ISO component
     */
    public DatasetElement(int id, ISOComponent component) {
        this(id, component, false);
    }

    /**
     * Creates a dataset element.
     *
     * @param id element identifier, either a TLV tag or DBM bit number
     * @param component backing ISO component
     * @param constructed whether the source TLV tag was constructed
     */
    public DatasetElement(int id, ISOComponent component, boolean constructed) {
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }
        this.id = id;
        this.component = component;
        this.constructed = constructed;
    }

    /**
     * Returns the element identifier.
     *
     * @return element identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the backing ISO component.
     *
     * @return ISO component
     */
    public ISOComponent getComponent() {
        return component;
    }

    /**
     * Indicates whether the element originated from a constructed TLV tag.
     *
     * @return {@code true} for constructed TLV elements
     */
    public boolean isConstructed() {
        return constructed;
    }

    /**
     * Indicates whether a BER-TLV tag is constructed, that is, whether bit 6
     * of its first byte is set.
     *
     * @param tag TLV tag, for example {@code 0x71} or {@code 0x9F26}
     * @return {@code true} for constructed tags
     */
    public static boolean isConstructedTag(int tag) {
        String hexTag = Integer.toHexString(tag);
        if ((hexTag.length() & 0x01) == 1)
            hexTag = "0" + hexTag;
        byte[] tagBytes = ISOUtil.hex2byte(hexTag);
        return (tagBytes[0] & 0x20) == 0x20;
    }

    /**
     * Returns the component value.
     *
     * @return element value
     * @throws ISOException on component access errors
     */
    public Object getValue() throws ISOException {
        return component.getValue();
    }

    /**
     * Returns a defensive copy of the element bytes.
     *
     * @return encoded element value bytes, or {@code null}
     * @throws ISOException on component access errors
     */
    public byte[] getBytes() throws ISOException {
        byte[] bytes = component.getBytes();
        return bytes != null ? Arrays.copyOf(bytes, bytes.length) : null;
    }
}
