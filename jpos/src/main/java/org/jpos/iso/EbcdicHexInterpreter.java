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
 * Implements Hex Interpreter. The Hex digits are stored in EBCDIC.
 * 
 * @author ayakovlev
 * @version $Revision:$ $Date:$
 */
public class EbcdicHexInterpreter implements BinaryInterpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final EbcdicHexInterpreter INSTANCE = new EbcdicHexInterpreter();

    /** 0-15 to EBCDIC hex digit lookup table. */
    private static final byte[] HEX_EBCDIC = new byte[] {
              (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, 
              (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7,
              (byte)0xF8, (byte)0xF9, (byte)0xC1, (byte)0xC2, 
              (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6
    };

    /**
     * Converts the binary data into EBCDIC hex digits.
     */
    public void interpret(byte[] data, byte[] b, int offset)
    {
        for (int i = 0; i < data.length; i++) {
            b[offset + i * 2] = HEX_EBCDIC[(data[i] & 0xF0) >> 4];
            b[offset + i * 2 + 1] = HEX_EBCDIC[data[i] & 0x0F];
        }
    }

    /**
     * Converts the EBCDIC hex digits into binary data.
     */
    public byte[] uninterpret(byte[] rawData, int offset, int length)
    {
        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++)
        {
        	//TODO: what if the data is not EBCDIC? validation is required.
            byte hi = rawData[offset + i * 2];
            byte lo = rawData[offset + i * 2 + 1];
            int h = hi < (byte) 0xF0 ? 10 + hi - 0xC0 : hi - 0xF0;
            int l = lo < (byte) 0xF0 ? 10 + lo - 0xC0 : lo - 0xF0;
            ret[i] = (byte)(h << 4 | l);
        }
        return ret;
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
