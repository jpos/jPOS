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
 * Implements EBCDIC Interpreter. Strings are converted to and from EBCDIC
 * bytes.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class EbcdicInterpreter implements Interpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final EbcdicInterpreter INSTANCE = new EbcdicInterpreter();

    /**
	 * (non-Javadoc)
	 *
     */
    public void interpret(String data, byte[] b, int offset)
    {
        ISOUtil.asciiToEbcdic(data, b, offset);
    }

    /**
	 * (non-Javadoc)
	 *
     */
    public String uninterpret(byte[] rawData, int offset, int length)
    {
        return ISOUtil.ebcdicToAscii(rawData, offset, length);
    }

    /**
	 * (non-Javadoc)
	 *
     */
    public int getPackedLength(int nDataUnits)
    {
        return nDataUnits;
    }
}
