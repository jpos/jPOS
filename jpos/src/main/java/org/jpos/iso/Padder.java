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
 * An interface for padding and unpadding strings and byte arrays.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public interface Padder
{
    /**
	 * Returns a padded string upto a maximum length. If the data is longer
	 * than maxLength, then the data is truncated.
	 * 
	 * @param data
	 *            The string to pad.
	 * @param maxLength
	 *            The maximum length of the padded string.
	 * @return A padded string.
     * @throws ISOException on error
	 */
    String pad(String data, int maxLength) throws ISOException;

    /**
	 * Removes the padding from a padded string.
	 * 
	 * @param paddedData
	 *            The string to unpad.
	 * @return The unpadded string.
     * @throws ISOException on error
	 */
    String unpad(String paddedData) throws ISOException;
}