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
 * EBCDIC version of IFA_NUMERIC
 * Right Justify, zero fill (0xf0) fields
 *
 * @author eoin.flood@orbiscom.com
 * @version $Id$
 * @see IFA_NUMERIC
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_NUMERIC extends ISOStringFieldPackager 
{
    public IFE_NUMERIC() {
        super(LeftPadder.ZERO_PADDER, EbcdicInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_NUMERIC(int len, String description) {
        super(len, description, LeftPadder.ZERO_PADDER, EbcdicInterpreter.INSTANCE, NullPrefixer.INSTANCE);
    }
}
