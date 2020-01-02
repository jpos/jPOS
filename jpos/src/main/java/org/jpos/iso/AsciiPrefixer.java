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
 * AsciiPrefixer constructs a prefix for ASCII messages.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class AsciiPrefixer implements Prefixer
{
    /**
     * A length prefixer for up to 9 chars. The length is encoded with 1 ASCII
     * char representing 1 decimal digit.
     */
    public static final AsciiPrefixer L = new AsciiPrefixer(1);
    /**
	 * A length prefixer for up to 99 chars. The length is encoded with 2 ASCII
	 * chars representing 2 decimal digits.
	 */
    public static final AsciiPrefixer LL = new AsciiPrefixer(2);
    /**
	 * A length prefixer for up to 999 chars. The length is encoded with 3 ASCII
	 * chars representing 3 decimal digits.
	 */
    public static final AsciiPrefixer LLL = new AsciiPrefixer(3);
    /**
	 * A length prefixer for up to 9999 chars. The length is encoded with 4
	 * ASCII chars representing 4 decimal digits.
	 */
    public static final AsciiPrefixer LLLL = new AsciiPrefixer(4);
    /**
     * A length prefixer for up to 99999 chars. The length is encoded with 5
     * ASCII chars representing 5 decimal digits.
     */
    public static final AsciiPrefixer LLLLL = new AsciiPrefixer(5);

    /**
     * A length prefixer for up to 999999 chars. The length is encoded with 6
     * ASCII chars representing 6 decimal digits.
     */
    public static final AsciiPrefixer LLLLLL = new AsciiPrefixer(6);

    //private static final LeftPadder PADDER = LeftPadder.ZERO_PADDER;
    //private static final AsciiInterpreter INTERPRETER = AsciiInterpreter.INSTANCE;

    /** The number of digits allowed to express the length */
    private int nDigits;
    
    public AsciiPrefixer(int nDigits)
    {
        this.nDigits = nDigits;
    }

    @Override
    public void encodeLength(int length, byte[] b) throws ISOException
    {
        int n = length;
        // Write the string backwards - I don't know why I didn't see this at first.
        for (int i = nDigits - 1; i >= 0; i--)
        {
            b[i] = (byte)(n % 10 + '0');
            n /= 10;
        }
        if (n != 0)
        {
            throw new ISOException("invalid len "+ length + ". Prefixing digits = " + nDigits);
        }
    }

    @Override
    public int decodeLength(byte[] b, int offset) throws ISOException {
        int len = 0;
        for (int i = 0; i < nDigits; i++)
        {
            byte d = b[offset + i];
            if(d < '0' || d > '9')
            {
                throw new ISOException("Invalid character found. Expected digit.");
            }
            len = len * 10 + d - (byte)'0';
        }
        return len;
    }

    @Override
    public int getPackedLength()
    {
        return nDigits;
    }
}
