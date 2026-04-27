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


import org.jpos.tlv.TLVDataFormat;
import org.jpos.iso.ISOException;


/**
 * Maps EMV/BER-TLV tag numbers to their corresponding {@link org.jpos.tlv.TLVDataFormat} descriptors.
 * @author Vishnu Pillai
 */
/** Maps BER-TLV tag numbers to their data format (binary, ASCII, etc.). */
/** Maps BER-TLV tag values to their wire format (ASCII hex, binary, EBCDIC hex, etc.). */
public interface BERTLVFormatMapper {

    /**
     * Returns the data format for the given BER-TLV tag.
     * @param tagNumber the numeric tag identifier
     * @return the data format for the tag
     * @throws ISOException if the tag is unknown or invalid
     */
    TLVDataFormat getFormat(Integer tagNumber) throws ISOException;

}
