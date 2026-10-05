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
 * EBCDIC [unpacked] Bitmap
 *
 * @author apr
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class IFE_BITMAP extends ISOBitMapPackager {
    /** Default constructor. */
    public IFE_BITMAP() {
        super();
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_BITMAP(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException on ISO processing error
     */
    public byte[] pack (ISOComponent c) throws ISOException {
    	BitSet bitMapValue = (BitSet) c.getValue();
    	int maxBytesPossible = getLength();
    	int maxBitsAllowedPhysically = maxBytesPossible<<3;
    	int lastBitOn = bitMapValue.length()-1;
        int actualLastBit=lastBitOn; // takes into consideration 2nd and 3rd bit map flags
        if (lastBitOn > 128) {
        	if (bitMapValue.get(65)) {
        		actualLastBit = 192;
            } else {
                actualLastBit = 128;
            }
        } else if (lastBitOn > 64) {
            actualLastBit = 128;
        }
       	if (actualLastBit > maxBitsAllowedPhysically) {
            throw new ISOException ("Bitmap can only hold bits numbered up to " + maxBitsAllowedPhysically + " in the " +
    						getLength() + " bytes available.");
        }
        
       	int requiredLengthInBytes = (actualLastBit >> 3) + (actualLastBit % 8 > 0 ? 1 : 0);
       	
       	int requiredBitMapLengthInBytes;
       	if (requiredLengthInBytes>4 && requiredLengthInBytes<=8) {
            requiredBitMapLengthInBytes = 8;
        }
       	else if (requiredLengthInBytes>8 && requiredLengthInBytes<=16) {
            requiredBitMapLengthInBytes = 16;
        }
       	else if (requiredLengthInBytes>16 && requiredLengthInBytes<=24) {
            requiredBitMapLengthInBytes = 24;
        }
       	else {
            requiredBitMapLengthInBytes=maxBytesPossible;
        }
       		     	
        byte[] b = ISOUtil.bitSet2byte (bitMapValue, requiredBitMapLengthInBytes);
        return ISOUtil.asciiToEbcdic(ISOUtil.hexString(b).getBytes());
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
        checkBitmapLength();
        int bytes = Math.min(getLength(), 8) << 1;
        checkAvailable(b, offset, bytes, "primary bitmap");
        byte[] primary = toAsciiHex(b, offset, bytes);
        BitSet bmap = ISOUtil.hex2BitSet(new BitSet(64), primary, 0);
        if (getLength() > 8 && bmap.get(1)) {
            int secondary = Math.min(getLength() - 8, 8) << 1;
            checkAvailable(b, offset + bytes, secondary, "secondary bitmap");
            ISOUtil.hex2BitSet(bmap, toAsciiHex(b, offset + bytes, secondary), 64);
            bytes += secondary;
        }
        if (getLength() > 16 && bmap.get(1) && bmap.get(65)) {
            int tertiary = Math.min(getLength() - 16, 8) << 1;
            checkAvailable(b, offset + bytes, tertiary, "tertiary bitmap");
            ISOUtil.hex2BitSet(bmap, toAsciiHex(b, offset + bytes, tertiary), 128);
            bytes += tertiary;
        }
        c.setValue(bmap);
        return bytes;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        checkBitmapLength();
        int primaryLength = Math.min(getLength(), 8) << 1;
        byte[] b1 = toAsciiHex(readBytes(in, primaryLength), 0, primaryLength);
        BitSet bmap = ISOUtil.hex2BitSet(new BitSet(64), b1, 0);
        if (getLength() > 8 && bmap.get(1)) {
            int secondaryLength = Math.min(getLength() - 8, 8) << 1;
            byte[] b2 = toAsciiHex(readBytes(in, secondaryLength), 0, secondaryLength);
            ISOUtil.hex2BitSet(bmap, b2, 64);
        }
        if (getLength() > 16 && bmap.get(1) && bmap.get(65)) {
            int tertiaryLength = Math.min(getLength() - 16, 8) << 1;
            byte[] b3 = toAsciiHex(readBytes(in, tertiaryLength), 0, tertiaryLength);
            ISOUtil.hex2BitSet(bmap, b3, 128);
        }
        c.setValue(bmap);
    }

    private void checkBitmapLength() throws ISOException {
        if (getLength() <= 0)
            throw new ISOException("Invalid bitmap length " + getLength());
    }

    private static byte[] toAsciiHex(byte[] b, int offset, int length) throws ISOException {
        byte[] ascii = ISOUtil.ebcdicToAsciiBytes(b, offset, length);
        for (byte value : ascii) {
            if (Character.digit((char) (value & 0xFF), 16) < 0)
                throw new ISOException("Invalid hexadecimal digit in bitmap");
        }
        return ascii;
    }
}
