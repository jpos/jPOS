/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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


public class IFMC_TCC extends IF_CHAR{
    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        if (c != null) return super.pack(c);
        else return new byte[0];
    }

    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        if (b.length > offset &&  (Character.isAlphabetic(b[offset]) || b[offset] == ' ')) {
            return super.unpack(c, b, offset);
        }
        return 0;
    }

}
