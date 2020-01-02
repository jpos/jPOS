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
 * EBCDIC version of IFA_LCHAR
 * Uses a 1 EBCDIC byte length field
 *
 * @author eoin.flood@orbiscom.com
 * @author mouslih.abdelhakim@gmail.com
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LCHAR extends ISOStringFieldPackager
{
    public IFE_LCHAR() {
        super(NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.L);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_LCHAR(int len, String description) {
        super(len, description, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.L);
        checkLength(len, 9);
    }

    public void setLength(int len)
    {
        checkLength(len, 9);
        super.setLength(len);
    }
}
