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
 * ASCII packaged Bitmap
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class IFA_BITMAP extends ISOBitMapPackager {
    /** Default constructor. */
    public IFA_BITMAP() {
        super();
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_BITMAP(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException on ISO processing error
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        BitSet b = (BitSet) c.getValue();
        int len =
            getLength() >= 8 ?
                    b.length()+62 >>6 <<3 : getLength();
        return ISOUtil.hexString(ISOUtil.bitSet2byte (b, len)).getBytes();
    }

    public int getMaxPackedLength() {
        return getLength() >> 2;
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
        int bytes = Math.min(getLength(), 8) << 1;
        checkBitmapLength();
        checkAvailable(b, offset, bytes, "primary bitmap");
        BitSet bmap = new BitSet(64);
        addHexBitmap(bmap, b, offset, bytes, 0);
        if (getLength() > 8 && bmap.get(1)) {
            int secondary = Math.min(getLength() - 8, 8) << 1;
            checkAvailable(b, offset + bytes, secondary, "secondary bitmap");
            addHexBitmap(bmap, b, offset + bytes, secondary, 64);
            bytes += secondary;
        }
        if (getLength() > 16 && bmap.get(1) && bmap.get(65)) {
            int tertiary = Math.min(getLength() - 16, 8) << 1;
            checkAvailable(b, offset + bytes, tertiary, "tertiary bitmap");
            addHexBitmap(bmap, b, offset + bytes, tertiary, 128);
            bytes += tertiary;
            bmap.clear(65);
        }
        c.setValue(bmap);
        return bytes;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        checkBitmapLength();
        byte[] primary = readBytes(in, Math.min(getLength(), 8) << 1);
        checkHexDigits(primary, 0, primary.length);
        BitSet bmap = ISOUtil.hex2BitSet(new BitSet(64), primary, 0);
        if (getLength() > 8 && bmap.get (1)) {
            byte[] secondary = readBytes(in, Math.min(getLength() - 8, 8) << 1);
            checkHexDigits(secondary, 0, secondary.length);
            ISOUtil.hex2BitSet(bmap, secondary, 64);
        }
        if (getLength() > 16 && bmap.get(1) && bmap.get(65)) {
            byte[] tertiary = readBytes(in, Math.min(getLength() - 16, 8) << 1);
            checkHexDigits(tertiary, 0, tertiary.length);
            ISOUtil.hex2BitSet(bmap, tertiary, 128);
            bmap.clear(65);
        }
        c.setValue(bmap);
    }

    private void checkBitmapLength() throws ISOException {
        if (getLength() <= 0)
            throw new ISOException("Invalid bitmap length " + getLength());
    }

    private static void addHexBitmap(BitSet bmap, byte[] b, int offset, int length, int bitOffset)
        throws ISOException
    {
        for (int i = 0; i < length; i++) {
            int digit = Character.digit((char) (b[offset + i] & 0xFF), 16);
            if (digit < 0)
                throw new ISOException("Invalid hexadecimal digit in bitmap");
            for (int bit = 0; bit < 4; bit++) {
                if ((digit & 0x08 >> bit) != 0)
                    bmap.set(bitOffset + (i << 2) + bit + 1);
            }
        }
    }

    private static void checkHexDigits(byte[] b, int offset, int length) throws ISOException {
        for (int i = 0; i < length; i++) {
            if (Character.digit((char) (b[offset + i] & 0xFF), 16) < 0)
                throw new ISOException("Invalid hexadecimal digit in bitmap");
        }
    }
}
