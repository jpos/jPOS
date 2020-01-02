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
 * AsciiPrefixer constructs a prefix for ASCII messages.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class NullPrefixer implements Prefixer
{
    /** A handy instance of the null prefixer. */
    public static final NullPrefixer INSTANCE = new NullPrefixer();

    /** Hidden constructor */
    private NullPrefixer() {}

    @Override
    public void encodeLength(int length, byte[] b) {}

    /**
	 * Returns -1 meaning there is no length field.
	 *
     */
    @Override
    public int decodeLength(byte[] b, int offset)
    {
        return -1;
    }

    @Override
    public int getPackedLength()
    {
        return 0;
    }
}
