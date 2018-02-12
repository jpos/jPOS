/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
 * EBCDIC version of IFB_LLNUM
 * Uses a 2 EBCDIC byte length field
 *
 * based on Eoin's IFE_LLCHAR
 * @author apr@cs.com.uy
 * @author Jonathan.O'Connor@xcom.de
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LLNUM extends ISOStringFieldPackager 
{
    public IFE_LLNUM() {
        super(NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_LLNUM(int len, String description) {
        super(len, description, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LL);
        checkLength(len, 99);
    }

    public void setLength(int len)
    {
        checkLength(len, 99);
        super.setLength(len);
    }
}
