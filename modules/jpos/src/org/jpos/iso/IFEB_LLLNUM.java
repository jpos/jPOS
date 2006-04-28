/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may
 *    appear in the software itself, if and wherever such third-party
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse
 *    or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.iso;

/**
 * EBCDIC version of IF_LLCHAR
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
            "invalid len "+len +" packing LLEBCHAR field " + c.getKey());
        
        
        //System.out.println("String s = (String) c.getValue(); "+s);
        
        
        
        // if odd length
        if ( (len%2)==1 ) {
            odd = true;
            len = (len/2)+1;
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
        
        
        int len = ((b[offset] & 0x0f) * 100)  + ((b[offset+1] & 0x0f) * 10) + (b[offset+2] & 0x0f);
        
        int tempLen = len*2;

        //System.out.println("len "+ len +"len*2 "+tempLen);
        
        
        // odd handling
        byte testByte = b[ (offset+3+len-1) ];
        
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
