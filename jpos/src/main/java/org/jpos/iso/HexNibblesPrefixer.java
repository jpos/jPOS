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

package org.jpos.iso;

/**
 * HexNibblesPrefixer constructs a prefix storing the length in BCD.
 * 
 * @author joconnor, apr
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unused")
public class HexNibblesPrefixer implements Prefixer {
    public static final HexNibblesPrefixer LL = new HexNibblesPrefixer(2);
    public static final HexNibblesPrefixer LLL = new HexNibblesPrefixer(3);
    private int nDigits;

    public HexNibblesPrefixer(int nDigits) {
        this.nDigits = nDigits;
    }

    @Override
    public void encodeLength(int length, byte[] b) {
        length <<= 1;
        for (int i = getPackedLength() - 1; i >= 0; i--) {
            int twoDigits = length % 100;
            length /= 100;
            b[i] = (byte)((twoDigits / 10 << 4) + twoDigits % 10);
        }
    }

    @Override
    public int decodeLength(byte[] b, int offset) {
        int len = 0;
        for (int i = 0; i < (nDigits + 1) / 2; i++)
        {
            len = 100 * len + ((b[offset + i] & 0xF0) >> 4) * 10 + (b[offset + i] & 0x0F);
        }
        return len >> 1;
    }

    @Override
    public int getPackedLength()
    {
        return nDigits + 1 >> 1;
    }
}
