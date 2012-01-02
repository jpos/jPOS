/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import java.io.UnsupportedEncodingException;


/**
 * Implements ASCII Interpreter. Strings are converted to and from ASCII bytes.
 * This uses the US-ASCII encoding which all JVMs must support.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class AsciiInterpreter implements Interpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final AsciiInterpreter INSTANCE = new AsciiInterpreter();

    /**
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Interpreter#interpret(java.lang.String)
	 */
    public void interpret(String data, byte[] b, int offset)
    {
        try {
            System.arraycopy(data.getBytes(ISOUtil.ENCODING), 0, b, offset, data.length());
        } catch (UnsupportedEncodingException ignored) {
            // encoding is supported
        }
    }

    /**
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Interpreter#uninterpret(byte[])
	 */
    public String uninterpret (byte[] rawData, int offset, int length) {
        byte[] ret = new byte[length];
        try {
            System.arraycopy(rawData, offset, ret, 0, length);
            return new String(ret, ISOUtil.ENCODING);
        } catch (UnsupportedEncodingException ignored) {
            // encoding is supported
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(
                String.format("Required %d but just got %d bytes", length, rawData.length-offset)
            );
        }
        return null;
    }

    /**
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Interpreter#getPackedLength(int)
	 */
    public int getPackedLength(int nDataUnits)
    {
        return nDataUnits;
    }
}
