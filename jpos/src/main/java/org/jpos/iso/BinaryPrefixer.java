/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
 * BinaryPrefixer constructs a prefix storing the length in binary.
 * 
 * @author joconnor
 * @version $Revision$ $Date$
 */
public class BinaryPrefixer implements Prefixer
{
    /**
	 * A length prefixer for upto 255 chars. The length is encoded with 1 unsigned byte.
	 */
    public static final BinaryPrefixer B = new BinaryPrefixer(1);

    /**
     * A length prefixer for upto 65535 chars. The length is encoded with 2 unsigned bytes.
     */
    public static final BinaryPrefixer BB = new BinaryPrefixer(2);

    /** The number of digits allowed to express the length */
    private int nBytes;

    public BinaryPrefixer(int nBytes)
    {
        this.nBytes = nBytes;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Prefixer#encodeLength(int, byte[])
	 */
    public void encodeLength(int length, byte[] b)
    {
        for (int i = nBytes - 1; i >= 0; i--) {
            b[i] = (byte)(length & 0xFF);
            length >>= 8;
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Prefixer#decodeLength(byte[], int)
	 */
    public int decodeLength(byte[] b, int offset)
    {
        int len = 0;
        for (int i = 0; i < nBytes; i++)
        {
            len = 256 * len + (b[offset + i] & 0xFF);
        }
        return len;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.Prefixer#getLengthInBytes()
	 */
    public int getPackedLength()
    {
        return nBytes;
    }
}