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
 * BER-TLV tag class encoded in bits 7–8 of the first tag byte.
 */
public enum TLVClass {
    /** Class bits {@code 00} — universally defined tag. */
    UNIVERSAL(0),
    /** Class bits {@code 01} — application-specific tag. */
    APPLICATION(1 << 6),
    /** Class bits {@code 10} — context-specific tag (most EMV tags). */
    CONTEXT_SPECIFIC(2 << 6),
    /** Class bits {@code 11} — private-use tag. */
    PRIVATE(3 << 6);

    int value;

    TLVClass(int value) {
        this.value = value;
    }

    /**
     * Resolves the {@link TLVClass} encoded in the top two bits of {@code firstByte}.
     *
     * @param firstByte the first byte of a BER-TLV tag
     * @return the matching class, defaulting to {@link #UNIVERSAL}
     */
    public static TLVClass valueOf (byte firstByte) {
        int i = (int) firstByte & 0xC0;
        for (TLVClass c : values()) {
            if (c.value == i)
                return c;
        }
        return UNIVERSAL;
    }
}
