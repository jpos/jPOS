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
    public IFE_BITMAP() {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_BITMAP(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
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
     * @exception ISOException
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
//    	TODO: calculate bytes to read based on bits 1, 65 on/off in the actual data 
    	int bytes;
    	byte [] b1 = ISOUtil.ebcdicToAsciiBytes (b, offset, getLength()*2 );
    	BitSet bmap = ISOUtil.hex2BitSet (b1, 0, getLength() << 3);
        c.setValue(bmap);
        bytes = b1.length;
        // check for 2nd bit map indicator
        if (bytes > 16 && !bmap.get(1)) {
          bytes = 16; 
        // check for 3rd bit map indicator
        } else if (bytes > 32 && !bmap.get(65)) {
          bytes = 32; 
        } 
        return bytes;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
    	byte [] b1 = ISOUtil.ebcdicToAsciiBytes (readBytes (in, 16), 0, 16);
        BitSet bmap = ISOUtil.hex2BitSet (new BitSet (64), b1, 0);
        if (getLength() > 8 && bmap.get (1)) {
        	byte [] b2 = ISOUtil.ebcdicToAsciiBytes (readBytes (in, 16), 0, 16);        	
            ISOUtil.hex2BitSet (bmap, b2, 64);
        }
        c.setValue(bmap);
    }
}
