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

package org.jpos.tlv;

/**
 * TLV tag value implementation backed by a binary (byte[]) payload.
 * @author Vishnu Pillai
 */
/**
 * A TLV tag-value pair whose value is a raw byte array.
 */
public class BinaryTagValue extends TagValueBase<byte[]> {

    /**
     * Creates a BinaryTagValue with the given tag and byte-array value.
     * @param tag the hex string tag identifier
     * @param value the raw byte value
     */
    public BinaryTagValue(String tag, byte[] value) {
        super(tag, value);
    }

}
