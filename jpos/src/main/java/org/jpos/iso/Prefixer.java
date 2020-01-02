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
 * This interface is used to encode and decode length prefixes.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public interface Prefixer
{
    /**
	 * Fills a byte array with the field length data in raw form.
	 * 
	 * @param length
	 *            The length to be encoded.
	 * @param b
	 *            The byte array to fill with the encoded length.
	 */
    void encodeLength(int length, byte[] b) throws ISOException;

    /**
	 * Decodes an encoded length.
	 * 
	 * @param b
	 *            The byte array to scan for the length.
	 * @param offset
	 *            The offset to start scanning from.
	 * @return The length in chars of the field data to follow this
	 *         LengthPrefix.
	 */
    int decodeLength(byte[] b, int offset) throws ISOException;

    /**
	 * Returns the number of bytes taken up by the length encoding.
	 */
    int getPackedLength();
}