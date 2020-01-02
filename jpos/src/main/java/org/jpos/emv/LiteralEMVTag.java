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

package org.jpos.emv;


import org.jpos.tlv.TLVDataFormat;


/**
 * @author Vishnu Pillai
 */
public class LiteralEMVTag extends EMVTag<String> {

    public LiteralEMVTag(EMVStandardTagType tagType, String value)
            throws IllegalArgumentException {
        super(tagType, value);
    }

    public LiteralEMVTag(EMVProprietaryTagType tagType, Integer tagNumber, String value)
            throws IllegalArgumentException {
        super(tagType, tagNumber, value);
    }

    public LiteralEMVTag(EMVProprietaryTagType tagType, Integer tagNumber, TLVDataFormat dataFormat, String value)
            throws IllegalArgumentException {
        super(tagType, tagNumber, dataFormat, value);
    }
}
