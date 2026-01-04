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

public enum TLVClass {
    UNIVERSAL(0),
    APPLICATION(1 << 6),
    CONTEXT_SPECIFIC(2 << 6),
    PRIVATE(3 << 6);

    int value;

    TLVClass(int value) {
        this.value = value;
    }

    public static TLVClass valueOf (byte firstByte) {
        int i = (int) firstByte & 0xC0;
        for (TLVClass c : values()) {
            if (c.value == i)
                return c;
        }
        return UNIVERSAL;
    }
}
