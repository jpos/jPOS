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

/**
 * EBCDIC version of IFB_LLLNUM
 * Uses a 2 EBCDIC byte length field
 *
 * @author julien.moebs@paybox.net
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */


public class IFEB_LLLNUM extends ISOFieldPackager {
    /** Default constructor. */
    public IFEB_LLLNUM () {
        super();
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFEB_LLLNUM (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException on ISO processing error
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        boolean odd = false;
        int len;
        String s = (String) c.getValue();
        
        
        
        if ((len=s.length()) > getLength() || len>99)   // paranoia settings
            throw new ISOException(
            "invalid len "+len +" packing IFEB_LLLNUM field " + c.getKey());
        
        
        //System.out.println("String s = (String) c.getValue(); "+s);
        
        
        
        // if odd length
        if ( len%2 ==1 ) {
            odd = true;
            len = len/2 +1;
        } else {
            odd = false;
            len = len/2;
        }
        
        //System.out.println("len= "+ len +" s.length()= "+len);
        
        String fieldLength = ISOUtil.zeropad(Integer.toString(len), 3);
        
        byte [] EBCDIClength = ISOUtil.asciiToEbcdic(fieldLength);

        // bcd stuff
        byte[] bcd = ISOUtil.str2bcd(s, false);
        if(odd)
            bcd[len-1] = (byte) (bcd[len-1] | 0xf);
        

        //System.out.println("bcd2str "+ISOUtil.bcd2str(bcd, 0, bcd.length*2, false) );
        
        byte[] b   = new byte[bcd.length + 3];
        
        b[0] = EBCDIClength[0];
        b[1] = EBCDIClength[1];
        b[2] = EBCDIClength[2];
        System.arraycopy(bcd, 0, b, 3, bcd.length);
        
        return b;
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException on ISO processing error
     */
    public int unpack(ISOComponent c, byte[] b, int offset)
    throws ISOException {
        int prefixLength = EbcdicPrefixer.LLL.getPackedLength();
        checkAvailable(b, offset, prefixLength, "length prefix");
        int len = EbcdicPrefixer.LLL.decodeLength(b, offset);
        checkPackedLength(len);
        checkAvailable(b, offset + prefixLength, len, "field value");
        int digits = getDigitLength(b, offset + prefixLength, len);
        checkUnpackedLength(digits);
        c.setValue(ISOUtil.bcd2str(b, offset + prefixLength, digits, false));
        return len+3;
    }

    @Override
    public void unpack(ISOComponent c, InputStream in) throws IOException, ISOException {
        int len = EbcdicPrefixer.LLL.decodeLength(readBytes(in, 3), 0);
        checkPackedLength(len);
        byte[] value = readBytes(in, len);
        int digits = getDigitLength(value, 0, len);
        checkUnpackedLength(digits);
        c.setValue(ISOUtil.bcd2str(value, 0, digits, false));
    }

    private void checkPackedLength(int length) throws ISOException {
        if (getLength() < 0)
            throw new ISOException("Field maximum length is not configured");
        if (length > ((long) getLength() + 1) / 2)
            throw new ISOException("Packed field length " + length + " too long");
    }

    private static int getDigitLength(byte[] b, int offset, int length) throws ISOException {
        if (length == 0)
            return 0;
        for (int i = 0; i < length; i++) {
            int value = b[offset + i] & 0xFF;
            int high = value >>> 4;
            int low = value & 0x0F;
            if (high > 9 || (low > 9 && (i != length - 1 || low != 0x0F)))
                throw new ISOException("Invalid BCD digit in field value");
        }
        return length * 2 - ((b[offset + length - 1] & 0x0F) == 0x0F ? 1 : 0);
    }
    
    public int getMaxPackedLength() {
        return getLength()+3;
    }
}
