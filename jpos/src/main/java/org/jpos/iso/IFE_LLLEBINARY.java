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
 * Uses a 3 EBCDIC byte length field, and EBCDIC content
 * 
 * @author Alejandro Revila
 * @author Jonathan O'Connor
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LLLEBINARY extends ISOBinaryFieldPackager 
{
    public IFE_LLLEBINARY()
    {
        super(EbcdicBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL);
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLLEBINARY(int len, String description) 
    {
        super(len, description, EbcdicBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL);
        checkLength(len, 999);
    }

    public void setLength(int len)
    {
        checkLength(len, 999);
        super.setLength(len);
    }
}
