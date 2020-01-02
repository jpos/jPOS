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
 * Implements a Literal Interpreter. No conversion is done.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class LiteralInterpreter implements Interpreter
{
    /** An instance of this Interpreter. Only one needed for the whole system */
    public static final LiteralInterpreter INSTANCE = new LiteralInterpreter();

    /**
	 * (non-Javadoc)
	 *
     */
    @Override
    public void interpret(String data, byte[] b, int offset)
    {
        byte[] raw = data.getBytes(ISOUtil.CHARSET);
        System.arraycopy(raw, 0, b, offset, raw.length);
    }

    /**
	 * (non-Javadoc)
	 *
     */
    @Override
    public String uninterpret(byte[] rawData, int offset, int length) {
        return new String(rawData, offset, length, ISOUtil.CHARSET);
    }

    /**
	 * (non-Javadoc)
	 *
     */
    @Override
    public int getPackedLength(int nDataUnits)
    {
        return nDataUnits;
    }
}
