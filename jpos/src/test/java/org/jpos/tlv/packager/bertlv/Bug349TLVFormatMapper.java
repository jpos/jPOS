/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

package org.jpos.tlv.packager.bertlv;

import org.jpos.emv.EMVTagType;
import org.jpos.emv.UnknownTagNumberException;
import org.jpos.iso.ISOException;
import org.jpos.tlv.TLVDataFormat;

public class Bug349TLVFormatMapper extends DefaultICCBERTLVFormatMapper {
    public static Bug349TLVFormatMapper INSTANCE = new Bug349TLVFormatMapper();

    private Bug349TLVFormatMapper() {
        super();
    }

    private EMVTagType getTagType(final Integer tagNumber) throws UnknownTagNumberException {
        if (Bug349TagType.isProprietaryTag(tagNumber)) {
            return getProprietaryTagType(tagNumber);
        } else {
            return Bug349TagType.forCode(tagNumber);
        }
    }

    @Override
    public TLVDataFormat getFormat(Integer tagNumber) throws ISOException {

        try {
            return super.getFormat(tagNumber);
        } catch (ISOException e) {
            EMVTagType tagType;
            try {
                tagType = getTagType(tagNumber);
            } catch (UnknownTagNumberException e1) {
                throw new ISOException(e1);
            }
            return tagType.getFormat();
        }
    }
}
