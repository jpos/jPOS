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
 * Implements the Padder interface for padding strings and byte arrays on the
 * Right.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class RightPadder implements Padder
{
    /**
	 * A padder for padding spaces on the right. This is very common in
	 * alphabetic fields.
	 */
    public static final RightPadder SPACE_PADDER = new RightPadder(' ');

    private char pad;

    /**
	 * Creates a Right Padder with a specific pad character.
	 * 
	 * @param pad
	 *            The padding character. For binary padders, the pad character
	 *            is truncated to lower order byte.
	 */
    public RightPadder(char pad)
    {
        this.pad = pad;
    }

    public String pad(String data, int maxLength) throws ISOException
    {
        int len = data.length();

        if (len < maxLength) {
            StringBuilder padded = new StringBuilder(maxLength);
            padded.append(data);
            for (; len < maxLength; len++) {
                padded.append(pad);
            }
            data = padded.toString();
        }
        else if (len > maxLength) {
            throw new ISOException("Data is too long. Max = " + maxLength);
        }
        return data;
    }

    public String unpad(String paddedData)
    {
        int len = paddedData.length();
        for (int i = len; i > 0; i--)
        {
            if (paddedData.charAt(i - 1) != pad)
            {
                return paddedData.substring(0, i);
            }
        }
        return "";
    }
}
