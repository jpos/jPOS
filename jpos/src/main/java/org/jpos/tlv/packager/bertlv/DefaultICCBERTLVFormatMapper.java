/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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


import org.jpos.emv.EMVProprietaryTagType;
import org.jpos.emv.EMVStandardTagType;
import org.jpos.emv.EMVTagType;
import org.jpos.tlv.TLVDataFormat;
import org.jpos.emv.UnknownTagNumberException;
import org.jpos.iso.ISOException;

/**
 * Default BER-TLV format mapper for ICC (Integrated Circuit Card) data elements, based on the EMV specification.
 * @author Vishnu Pillai
 */
public class DefaultICCBERTLVFormatMapper implements BERTLVFormatMapper {

    /** Singleton instance of this format mapper. */
    public static DefaultICCBERTLVFormatMapper INSTANCE = new DefaultICCBERTLVFormatMapper();

    /** Default constructor. */
    public DefaultICCBERTLVFormatMapper() {
        super();
    }

    private EMVTagType getTagType(final Integer tagNumber) throws UnknownTagNumberException {
        if (EMVStandardTagType.isProprietaryTag(tagNumber)) {
            return getProprietaryTagType(tagNumber);
        } else {
            return EMVStandardTagType.forCode(tagNumber);
        }
    }

    @Override
    public TLVDataFormat getFormat(Integer tagNumber) throws ISOException {
        EMVTagType tagType;
        try {
            tagType = getTagType(tagNumber);
        } catch (UnknownTagNumberException e) {
            throw new ISOException(e);
        }
        return tagType.getFormat();
    }

    /**
     * Subclasses should override this method to provide an implementation of org.jpos.emv.EMVProprietaryTagType.
     * @param tagNumber the proprietary tag number
     * @return the corresponding {@link EMVProprietaryTagType}
     * @throws UnknownTagNumberException if the tag number is not recognised
     */
    protected EMVProprietaryTagType getProprietaryTagType(Integer tagNumber) throws UnknownTagNumberException {
        throw new UnknownTagNumberException(Integer.toHexString(tagNumber));
    }

}
