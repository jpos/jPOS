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
 * left.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class LeftPadder implements Padder
{
    /**
	 * A padder for padding zeros on the left. This is very common in numeric
	 * fields.
	 */
    public static final LeftPadder ZERO_PADDER = new LeftPadder('0');

    private char pad;

    /**
	 * Creates a Left Padder with a specific pad character.
	 * 
	 * @param pad
	 *            The padding character. For binary padders, the pad character
	 *            is truncated to lower order byte.
	 */
    public LeftPadder(char pad)
    {
        this.pad = pad;
    }

    /**
     */
    public String pad(String data, int maxLength) throws ISOException
    {
        StringBuilder padded = new StringBuilder(maxLength);
        int len = data.length();
        if (len > maxLength)
        {
            throw new ISOException("Data is too long. Max = " + maxLength);
        } else
        {
            for (int i = maxLength - len; i > 0; i--)
            {
                padded.append(pad);
            }
            padded.append(data);
        }
        return padded.toString();
    }

    /**
	 * (non-Javadoc)
	 *
     */
    public String unpad(String paddedData)
    {
        int i = 0;
        int len = paddedData.length();
        while (i < len)
        {
            if (paddedData.charAt(i) != pad)
            {
                return paddedData.substring(i);
            }
            i++;
        }
        return "";
    }
}
