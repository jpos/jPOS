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

package org.jpos.iso;

import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;

/**
 * ISOFieldPackager Binary Bitmap
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class IFB_BITMAP extends ISOBitMapPackager {
    /** Default constructor. */
    public IFB_BITMAP() {
        super();
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_BITMAP(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException on ISO processing error
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        BitSet b = (BitSet) c.getValue();
        int len =                                           // bytes needed to encode BitSet (in 8-byte chunks)
            getLength() >= 8 ?
                    b.length()+62 >>6 <<3 : getLength();    // +62 because we don't use bit 0 in the BitSet
        return ISOUtil.bitSet2byte (b, len);
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException on ISO processing error
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        checkBitmapLength();
        int bytes = Math.min(getLength(), 8);
        checkAvailable(b, offset, bytes, "primary bitmap");
        BitSet bmap = new BitSet(64);
        addBitmap(bmap, b, offset, bytes, 0);
        if (getLength() > 8 && bmap.get(1)) {
            int secondary = Math.min(getLength() - 8, 8);
            checkAvailable(b, offset + bytes, secondary, "secondary bitmap");
            addBitmap(bmap, b, offset + bytes, secondary, 64);
            bytes += secondary;
        }
        if (getLength() > 16 && bmap.get(1) && bmap.get(65)) {
            int tertiary = Math.min(getLength() - 16, 8);
            checkAvailable(b, offset + bytes, tertiary, "tertiary bitmap");
            addBitmap(bmap, b, offset + bytes, tertiary, 128);
            bytes += tertiary;
        }
        c.setValue(bmap);
        return bytes;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        checkBitmapLength();
        BitSet bmap = ISOUtil.byte2BitSet(new BitSet(64), readBytes(in, Math.min(getLength(), 8)), 0);
        if (getLength() > 8 && bmap.get (1)) {
            ISOUtil.byte2BitSet(bmap, readBytes(in, Math.min(getLength() - 8, 8)), 64);
        }
        if (getLength() > 16 && bmap.get(1) && bmap.get(65)) {
            ISOUtil.byte2BitSet(bmap, readBytes(in, Math.min(getLength() - 16, 8)), 128);
        }
        c.setValue(bmap);
    }

    private void checkBitmapLength() throws ISOException {
        if (getLength() <= 0)
            throw new ISOException("Invalid bitmap length " + getLength());
    }

    private static void addBitmap(BitSet bmap, byte[] b, int offset, int length, int bitOffset) {
        for (int i = 0; i < length; i++) {
            for (int bit = 0; bit < 8; bit++) {
                if ((b[offset + i] & 0x80 >> bit) != 0)
                    bmap.set(bitOffset + (i << 3) + bit + 1);
            }
        }
    }
    public int getMaxPackedLength() {
        return getLength() >> 3;
    }
}
