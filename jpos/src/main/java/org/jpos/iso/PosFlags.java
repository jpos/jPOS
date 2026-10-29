/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

@SuppressWarnings("unused")
public abstract class PosFlags {

    public interface Flag {
        int getOffset();
        int intValue();
    }

    /**
     * Sets or unsets a set of flags according to value
     * @param value if true flags are set, else unset
     * @param flags flag set to set or unset
     */
    protected void setFlags(boolean value, Flag... flags) {
        byte[] b = getBytes();
        if (value) {
            for (Flag flag  : flags) {
                for (int v = flag.intValue(), offset = flag.getOffset(); v != 0; v >>>= 8, offset++) {
                    if (offset < b.length)
                        b[offset] |= (byte) v;
                }
            }
        } else {
            for (Flag flag  : flags) {
                for (int v = flag.intValue(), offset = flag.getOffset(); v != 0; v >>>= 8, offset++) {
                    if (offset < b.length)
                        b[offset] &= (byte) ~v;
                }
            }
        }
    }
    public abstract byte[] getBytes();

    public String toString() {
        return super.toString() + "[" + ISOUtil.hexString (getBytes())+ "]";
    }
}
