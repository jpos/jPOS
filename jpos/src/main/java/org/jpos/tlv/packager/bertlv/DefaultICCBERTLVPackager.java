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

package org.jpos.tlv.packager.bertlv;


import org.jpos.iso.ISOException;


/**
 * Packager for ICC Tags in BER TLV format
 *
 * @author Vishnu Pillai
 */

public abstract class DefaultICCBERTLVPackager extends BERTLVPackager {

    private static BERTLVFormatMapper DEFAULT_TAG_FORMAT_MAPPER = DefaultICCBERTLVFormatMapper.INSTANCE;

    /**
     * Use this method to globally set the BERTLVFormatMapper
     *
     * @param tagFormatMapper
     */
    public static void setTagFormatMapper(BERTLVFormatMapper tagFormatMapper) {
        DefaultICCBERTLVPackager.DEFAULT_TAG_FORMAT_MAPPER = tagFormatMapper;
    }

    @Override
    protected BERTLVFormatMapper getTagFormatMapper() {
        return DefaultICCBERTLVPackager.DEFAULT_TAG_FORMAT_MAPPER;
    }

    public DefaultICCBERTLVPackager() throws ISOException {
        super();
    }

}
