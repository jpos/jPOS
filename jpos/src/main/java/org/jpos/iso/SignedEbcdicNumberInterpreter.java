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
 * Implements EBCDIC Interpreter for signed numerics.
 * (see http://publib.boulder.ibm.com/infocenter/wmbhelp/v6r0m0/index.jsp?topic=/com.ibm.etools.mft.doc/ad06900_.htm)
 * Strings are converted to and from EBCDIC bytes.
 * Negatives will be prepended with "-"
 * Unsigned numbers are interpreted as positive
 * 
 * @author nsmith@mxgroup.net
 * @version $Revision$ $Date$
 */
public class SignedEbcdicNumberInterpreter implements Interpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final SignedEbcdicNumberInterpreter INSTANCE = new SignedEbcdicNumberInterpreter();

    public void interpret(String data, byte[] targetArray, int offset) {
        boolean negative = data.startsWith("-");
        if (negative) {
            ISOUtil.asciiToEbcdic(data.substring(1), targetArray, offset);
            targetArray[offset + data.length() - 2] = (byte) (targetArray[offset + data.length() - 2] & 0xDF); 
        } else {
            ISOUtil.asciiToEbcdic(data, targetArray, offset);
        }
    }

    public String uninterpret(byte[] rawData, int offset, int length) {
        boolean negative = (byte) (rawData[offset + length - 1] & 0xF0) == (byte)0xD0;
        rawData[offset + length - 1] = (byte) (rawData[offset + length - 1] | 0xF0);
        return (negative ? "-" : "") + ISOUtil.ebcdicToAscii(rawData, offset, length);
    }

    public int getPackedLength(int nDataUnits)
    {
        return nDataUnits;
    }
}