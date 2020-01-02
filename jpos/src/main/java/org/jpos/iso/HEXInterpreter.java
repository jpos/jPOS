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
 * Implements BCD Interpreter. Numeric Strings (consisting of chars '0'..'9' are converted
 * to and from BCD bytes. Thus, "1234" is converted into 2 bytes: 0x12, 0x34.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class HEXInterpreter implements Interpreter
{
    /** This HEXInterpreter sometimes adds a 0-nibble to the left. */
    public static final HEXInterpreter LEFT_PADDED = new HEXInterpreter(true, false);
    /** This HEXInterpreter sometimes adds a 0-nibble to the right. */
    public static final HEXInterpreter RIGHT_PADDED = new HEXInterpreter(false, false);
    /** This HEXInterpreter sometimes adds a F-nibble to the right. */
    public static final HEXInterpreter RIGHT_PADDED_F = new HEXInterpreter(false, true);
    /** This HEXInterpreter sometimes adds a F-nibble to the left. */
    public static final HEXInterpreter LEFT_PADDED_F = new HEXInterpreter(true, true);

    private boolean leftPadded;
    private boolean fPadded;

    /** Kept private. Only two instances are possible. */
    private HEXInterpreter(boolean leftPadded, boolean fPadded) {
        this.leftPadded = leftPadded;
        this.fPadded = fPadded;
    }

    /**
	 * (non-Javadoc)
	 *
     */
    public void interpret(String data, byte[] b, int offset)
    {
        ISOUtil.str2hex(data, leftPadded, b, offset);
        // if (fPadded && !leftPadded && data.length()%2 == 1)
        //   b[b.length-1] |= (byte)(b[b.length-1] << 4) == 0 ? 0x0F : 0x00;
        int paddedSize = data.length() >> 1;
        if (fPadded && data.length()%2 == 1)
            if (leftPadded)
                b[offset] |= (byte) 0xF0;
            else
                b[offset+paddedSize] |= (byte) 0x0F;
    }

    /**
	 * (non-Javadoc)
	 *
     */
    public String uninterpret(byte[] rawData, int offset, int length)
    {
        return ISOUtil.hex2str (rawData, offset, length, leftPadded);
    }

    /**
	 * Each numeric digit is packed into a nibble, so 2 digits per byte, plus the
     * possibility of padding.
	 *
     */
    public int getPackedLength(int nDataUnits)
    {
        return (nDataUnits + 1) / 2;
    }
}

