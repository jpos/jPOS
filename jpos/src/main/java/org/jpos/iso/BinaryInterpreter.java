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
 * This interface supports the encoding and decoding of binary data. Common
 * implementations are literal or no conversion, ASCII Hex, EBCDIC Hex and BCD.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public interface BinaryInterpreter
{
    /**
	 * Converts the binary data into a different interpretation or coding. Standard
	 * interpretations are ASCII Hex, EBCDIC Hex, BCD and LITERAL.
	 * 
	 * @param data
	 *            The data to be interpreted.
	 * @param b The byte array to write the interpreted data to.
     * @param offset The starting position in b.
	 */
    void interpret(byte[] data, byte[] b, int offset);

    /**
	 * Converts the raw byte array into a uninterpreted byte array. This reverses the
     * interpret method.
	 * 
	 * @param rawData
	 *            The interpreted data.
	 * @param offset
	 *            The index in rawData to start uninterpreting at.
	 * @param length
	 *            The number of uninterpreted bytes to uninterpret. This number may be
     *            different from the number of raw bytes that are uninterpreted.
	 * @return The uninterpreted data.
	 */
    byte[] uninterpret(byte[] rawData, int offset, int length);

    /**
	 * Returns the number of bytes required to interpret a byte array of length
	 * nBytes.
	 */
    int getPackedLength(int nBytes);
}