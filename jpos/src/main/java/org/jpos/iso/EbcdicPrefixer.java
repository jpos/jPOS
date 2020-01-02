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
 * EbcdicPrefixer constructs a prefix for EBCDIC messages.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class EbcdicPrefixer implements Prefixer
{
    /**
     * A length prefixer for up to 9 chars. The length is encoded with 1 EBCDIC
     * chars representing 1 decimal digits.
     */
    public static final EbcdicPrefixer L = new EbcdicPrefixer(1);
    /**
     * A length prefixer for up to 99 chars. The length is encoded with 2 EBCDIC
     * chars representing 2 decimal digits.
     */
    public static final EbcdicPrefixer LL = new EbcdicPrefixer(2);
    /**
     * A length prefixer for up to 999 chars. The length is encoded with 3 EBCDIC
     * chars representing 3 decimal digits.
     */
    public static final EbcdicPrefixer LLL = new EbcdicPrefixer(3);
    /**
     * A length prefixer for up to 9999 chars. The length is encoded with 4
     * EBCDIC chars representing 4 decimal digits.
     */
    public static final EbcdicPrefixer LLLL = new EbcdicPrefixer(4);

    private static byte[] EBCDIC_DIGITS = {(byte)0xF0, (byte)0xF1, (byte)0xF2,
        (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, (byte)0xF8, 
        (byte)0xF9}; 

    /** The number of digits allowed to express the length */
    private int nDigits;

    public EbcdicPrefixer(int nDigits)
    {
        this.nDigits = nDigits;
    }


    @Override
    public void encodeLength(int length, byte[] b)
    {
        for (int i = nDigits - 1; i >= 0; i--)
        {
            b[i] = EBCDIC_DIGITS[length % 10];
            length /= 10;
        }
    }

    @Override
    public int decodeLength(byte[] b, int offset)
    {
        int len = 0;
        for (int i = 0; i < nDigits; i++)
        {
            len = len * 10 + (b[offset + i] & 0x0F);
        }
        return len;
    }

    @Override
    public int getPackedLength()
    {
        return nDigits;
    }
}