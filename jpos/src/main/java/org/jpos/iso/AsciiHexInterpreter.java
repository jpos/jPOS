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
 * Implements Hex Interpreter. The Hex digits are stored in ASCII.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class AsciiHexInterpreter implements BinaryInterpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final AsciiHexInterpreter INSTANCE = new AsciiHexInterpreter();

    /** 0-15 to ASCII hex digit lookup table. */
    private static final byte[] HEX_ASCII = new byte[] {
              0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
              0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46
    };

    /**
     * Converts the binary data into ASCII hex digits.
     */
    public void interpret(byte[] data, byte[] b, int offset)
    {
        for (int i = 0; i < data.length; i++) {
            b[offset + i * 2] = HEX_ASCII[(data[i] & 0xF0) >> 4];
            b[offset + i * 2 + 1] = HEX_ASCII[data[i] & 0x0F];
        }
    }

    /**
     * Converts the ASCII hex digits into binary data.
     */
    public byte[] uninterpret(byte[] rawData, int offset, int length)
    {
        byte[] d = new byte[length];
        for (int i=0; i<length*2; i++) {
            int shift = i%2 == 1 ? 0 : 4;
            d[i>>1] |= Character.digit((char) rawData[offset+i], 16) << shift;
        }
        return d;
    }

    /**
     * Returns double nBytes because the hex representation of 1 byte needs 2 hex digits.
     *
     */
    public int getPackedLength(int nBytes)
    {
        return nBytes * 2;
    }
}
