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
 * Right. The difference between this and RightPadder is that this truncates the data
 * during packing, instead of throwing an exception.
 * 
 * @author jonathan.oconnor@xcom.de
 * @version $Revision$ $Date$
 */
public class RightTPadder extends RightPadder
{
    /**
	 * A padder for padding spaces on the right. This is very common in
	 * alphabetic fields.
	 */
    public static final RightTPadder SPACE_PADDER = new RightTPadder(' ');

    /**
	 * Creates a Right Truncating Padder with a specific pad character.
	 * 
	 * @param pad
	 *            The padding character. For binary padders, the pad character
	 *            is truncated to lower order byte.
	 */
    public RightTPadder(char pad)
    {
        super(pad);
    }

    /**
	 * @see org.jpos.iso.Padder#pad(java.lang.String, int)
	 */
    public String pad(String data, int maxLength) throws ISOException
    {
        if (data.length() > maxLength)
        {
            return super.pad(data.substring(0,maxLength), maxLength);
        } else
        {
            return super.pad(data, maxLength);
        }
    }
}