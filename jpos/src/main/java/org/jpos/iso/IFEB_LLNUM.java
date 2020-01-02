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

/**
 * Uses a 2 EBCDIC byte length field.
 *
 * right-justified with leading 0
 * and packed data as BCD. 2 BCD digits 
 * per byte and adding the value hex(0xF) 
 * for padding if length is odd.
 *
 * @author julien.moebs@paybox.net
 * @author doronf@xor-t.com
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFEB_LLNUM extends ISOFieldPackager {
    public IFEB_LLNUM () {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFEB_LLNUM (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack(ISOComponent c) throws ISOException {
        boolean odd = false;
        int len;
        String s = (String) c.getValue();
 
        if ((len=s.length()) > getLength() || len>99)   // paranoia settings
            throw new ISOException(
            "invalid len "+len +" packing IFEB_LLNUM field "+ c.getKey());
        
        // if odd length
        if ( len%2 ==1 ) {
            odd = true;
            len = len/2 +1;
        } else {
            odd = false;
            len = len/2;
        }

        String fieldLength = ISOUtil.zeropad(Integer.toString(len), 2);
        
        byte [] EBCDIClength = ISOUtil.asciiToEbcdic(fieldLength);

        // bcd stuff
        byte[] bcd = ISOUtil.str2bcd(s, false);
        
        if(odd)
            bcd[len-1] = (byte) (bcd[len-1] | 0xf);

        byte[] b   = new byte[bcd.length + 2];
        
        b[0] = EBCDIClength[0];
        b[1] = EBCDIClength[1];
        System.arraycopy(bcd, 0, b, 2, bcd.length);

        return b;
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack(ISOComponent c, byte[] b, int offset)
    throws ISOException {
        boolean pad = false;
        int len = (b[offset] & 0x0f) * 10 + (b[offset+1] & 0x0f);
        int tempLen = len*2;

        // odd handling
        byte lastByte = b[offset+2+len-1];

        if((lastByte & 0x0f) == 0x0f)
            tempLen--;
        
        c.setValue(ISOUtil.bcd2str(b, offset+2, tempLen, pad));

        return len+2;
    }

    /*
     * code contributed by doronf@xor-t.com
     */
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        byte[] b = readBytes (in, 2);
        int len =
                100 * (((b[0] >> 4 & 0x0F) > 0x09 ? 0 :
                        b[0] >> 4 & 0x0F) * 10 + (b[0] & 0x0F))
               + ((b[1] >> 4 & 0x0F) > 0x09 ? 0 :
                        b[1] >> 4 & 0x0F) * 10 + (b[1] & 0x0F);
        c.setValue (ISOUtil.bcd2str (readBytes (in, len), 0, 2*len, pad));
    }
    
    public int getMaxPackedLength() {
        return getLength()+2;
    }
}
