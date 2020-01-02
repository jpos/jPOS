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
 * EBCDIC version of IFB_LLLNUM
 * Uses a 2 EBCDIC byte length field
 *
 * @author julien.moebs@paybox.net
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */


public class IFEB_LLLNUM extends ISOFieldPackager {
    public IFEB_LLLNUM () {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFEB_LLLNUM (int len, String description) {
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
     * @exception ISOException
     */
    public int unpack(ISOComponent c, byte[] b, int offset)
    throws ISOException {
        boolean pad = false;
        
        
        int len = (b[offset] & 0x0f) * 100 + (b[offset+1] & 0x0f) * 10 + (b[offset+2] & 0x0f);
        
        int tempLen = len*2;

        //System.out.println("len "+ len +"len*2 "+tempLen);
        
        
        // odd handling
        byte testByte = b[offset+3+len-1];
        
        if( (testByte | 0xf0)== 0xff) {
            // odd length
            tempLen--;
        }
        
        
        // bcd line
        //System.out.println("ISOUtil.bcd2str(b, offset+2, len, pad) "+ISOUtil.bcd2str(b, offset+2, tempLen, pad));
        
        c.setValue(ISOUtil.bcd2str(b, offset+3, tempLen, pad));
        
        //c.setValue(ISOUtil.ebcdicToAscii(b, offset+2, len));
        
        return len+3;
    }
    
    public int getMaxPackedLength() {
        return getLength()+3;
    }
}
