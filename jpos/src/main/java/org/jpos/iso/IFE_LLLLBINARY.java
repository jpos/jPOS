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
 *
 * @author edwin < edwinkun at gmail dot com >
 */
public class IFE_LLLLBINARY extends ISOBinaryFieldPackager 
{
    public IFE_LLLLBINARY()
    {
        super(LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLLL);
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLLLBINARY(int len, String description) 
    {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLLL);
        checkLength(len, 9999);
    }

    public void setLength(int len)
    {
        checkLength(len, 9999);
        super.setLength(len);
    }
}

