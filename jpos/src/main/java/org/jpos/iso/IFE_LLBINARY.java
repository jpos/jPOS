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
 * BINARY version of IFE_LLLCHAR
 * Uses a 3 EBCDIC byte length field, and the binary data is stored as is.
 * 
 * @author Alejandro Revila
 * @author Jonathan O'Connor
 * @version $Id: IFE_LLLBINARY.java 1830 2003-11-18 01:18:48Z ninki $
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LLBINARY extends ISOBinaryFieldPackager 
{
    public IFE_LLBINARY()
    {
        super(LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLBINARY(int len, String description) 
    {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL);
        checkLength(len, 99);
    }

    public void setLength(int len)
    {
        checkLength(len, 99);
        super.setLength(len);
    }
}

