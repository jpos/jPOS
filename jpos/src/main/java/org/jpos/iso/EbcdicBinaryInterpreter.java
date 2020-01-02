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
 * Implements EBCDIC Binary Interpreter. byte[] are converted to and from EBCDIC bytes.
 * 
 * @author joconnor
 * @author apr
 */
@SuppressWarnings("unused")
public class EbcdicBinaryInterpreter implements BinaryInterpreter {
    public static final EbcdicBinaryInterpreter INSTANCE = new EbcdicBinaryInterpreter();

    /**
	 * (non-Javadoc)
	 *
     */
    public void interpret(byte[] data, byte[] b, int offset)
    {
        ISOUtil.asciiToEbcdic(data, b, offset);
    }

   public byte[] uninterpret(byte[] rawData, int offset, int length)
    {
        return ISOUtil.ebcdicToAsciiBytes(rawData, offset, length);
    }

   /**
    * @see org.jpos.iso.Interpreter#getPackedLength(int)
    */
    public int getPackedLength(int nDataUnits)
    {
        return nDataUnits;
    }
}
