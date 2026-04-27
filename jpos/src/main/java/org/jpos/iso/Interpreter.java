/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
 * Implementations convert Strings into byte arrays and vice versa.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public interface Interpreter
{
    /**
     * Converts the string data into a different interpretation. Standard
     * interpretations are ASCII, EBCDIC, BCD and LITERAL.
     * @param data the string data to interpret
     * @param b    the target byte array
     * @param offset the offset in {@code b} to start writing at
     * @throws ISOException on error
     */
    void interpret(String data, byte[] b, int offset) throws ISOException;

    /**
	 * Converts the byte array into a String. This reverses the interpret
	 * method.
	 * 
	 * @param rawData
	 *            The interpreted data.
	 * @param offset
	 *            The index in rawData to start interpreting at.
	 * @param length
	 *            The number of data units to interpret.
     * @return The uninterpreted data.
     * @throws ISOException on error
     */
    String uninterpret(byte[] rawData, int offset, int length) throws ISOException;

    /**
	 * Returns the number of bytes required to interpret a String of length
	 * nDataUnits.
	 */
    /**
     * Returns the number of bytes needed to store {@code nDataUnits} data units in this interpretation.
     * @param nDataUnits the number of logical data units
     * @return the packed byte length
     */
    int getPackedLength(int nDataUnits);
}
