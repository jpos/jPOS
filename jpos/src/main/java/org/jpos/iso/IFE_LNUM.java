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
 * Uses a 1 EBCDIC byte length field
 *
 * based on Eoin's IFE_LLCHAR
 * @author apr@cs.com.uy
 * @author Jonathan.O'Connor@xcom.de
 * @author mouslih.abdelhakim@gmail.com
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LNUM extends ISOStringFieldPackager
{
    public IFE_LNUM() {
        super(NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.L);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_LNUM(int len, String description) {
        super(len, description, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.L);
        checkLength(len, 9);
    }

    public void setLength(int len)
    {
        checkLength(len, 9);
        super.setLength(len);
    }
}
