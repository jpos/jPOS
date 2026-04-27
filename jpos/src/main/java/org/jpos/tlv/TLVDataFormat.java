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

package org.jpos.tlv;


/**
 * Describes the data format (encoding, length type) of a TLV field.
 * @author Vishnu Pillai
 */
public enum TLVDataFormat {
    /** ASCII digits {@code 0-9}. */
    ASCII_NUMERIC,
    /** ASCII alphabetic characters. */
    ASCII_ALPHA,
    /** ASCII alphanumeric characters. */
    ASCII_ALPHA_NUMERIC,
    /** ASCII alphanumeric characters and spaces. */
    ASCII_ALPHA_NUMERIC_SPACE,
    /** ASCII alphanumeric and special characters. */
    ASCII_ALPHA_NUMERIC_SPECIAL,
    /** Raw binary bytes. */
    BINARY,
    /** Compressed numeric (digits packed into nibbles, with padding). */
    COMPRESSED_NUMERIC,
    /** Constructed data object containing nested TLVs. */
    CONSTRUCTED,
    /** Packed BCD numeric (two digits per byte). */
    PACKED_NUMERIC,
    /** Packed BCD date in {@code YYMMDD} form. */
    PACKED_NUMERIC_DATE_YYMMDD,
    /** Packed BCD time in {@code HHMMSS} form. */
    PACKED_NUMERIC_TIME_HHMMSS,
    /** Proprietary/unspecified format. */
    PROPRIETARY
}
