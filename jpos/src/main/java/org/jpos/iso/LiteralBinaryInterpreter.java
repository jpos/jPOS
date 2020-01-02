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
 * This interpreter does no conversion and leaves the input the same as the output.
 * @author jonathan.oconnor@xcom.de
 */
public class LiteralBinaryInterpreter implements BinaryInterpreter
{
    /**
     * The only instance of this interpreter.
     */
    public static final LiteralBinaryInterpreter INSTANCE = new LiteralBinaryInterpreter();

    /**
     * Private constructor so we don't allow multiple instances.
     */
    private LiteralBinaryInterpreter()
    {
    }

    /**
     * Copies the input to the output.
     */
    public void interpret(byte[] data, byte[] b, int offset)
    {
        System.arraycopy(data, 0, b, offset, data.length);
    }

    /**
     * Copies the data out of the byte array.
     */
    public byte[] uninterpret(byte[] rawData, int offset, int length)
    {
        byte[] ret = new byte[length];
        System.arraycopy(rawData, offset, ret, 0, length);
        return ret;
    }

    /**
     * Returns nBytes because we are not doing any conversion.
     */
    public int getPackedLength(int nBytes)
    {
        // TODO Auto-generated method stub
        return nBytes;
    }
}
