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

import java.util.*;

/**
 * varios functions needed to pack/unpack ISO-8583 fields
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.20  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.19  2000/04/06 12:31:03  apr
 * XML normalize
 *
 * Revision 1.18  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.17  1999/12/15 13:04:13  apr
 * BugFix: bad FS definition
 *
 * Revision 1.16  1999/12/11 17:23:35  apr
 * BugFix: pan/exp separator = in bcd2str()
 *
 * Revision 1.15  1999/11/24 18:15:49  apr
 * Added dumpString used in VISA1 logs
 *
 * Revision 1.14  1999/11/18 23:36:38  apr
 * Added dumpString
 *
 * Revision 1.13  1999/09/20 12:43:14  apr
 * @return in strpad fixed (reported by georgem@tvinet.com)
 *
 */
public class ISOUtil {
    private static byte[] EBCDIC2ASCII = new byte[] {
        (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, 
        (byte)0xDC, (byte)0x09, (byte)0xC3, (byte)0x7F, 
        (byte)0xCA, (byte)0xB2, (byte)0xD5, (byte)0x0B, 
        (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F, 
        (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, 
        (byte)0xDB, (byte)0xDA, (byte)0x08, (byte)0xC1, 
        (byte)0x18, (byte)0x19, (byte)0xC8, (byte)0xF2, 
        (byte)0x1C, (byte)0x1D, (byte)0x1E, (byte)0x1F, 
        (byte)0xC4, (byte)0xB3, (byte)0xC0, (byte)0xD9, 
        (byte)0xBF, (byte)0x0A, (byte)0x17, (byte)0x1B, 
        (byte)0xB4, (byte)0xC2, (byte)0xC5, (byte)0xB0, 
        (byte)0xB1, (byte)0x05, (byte)0x06, (byte)0x07, 
        (byte)0xCD, (byte)0xBA, (byte)0x16, (byte)0xBC, 
        (byte)0xBB, (byte)0xC9, (byte)0xCC, (byte)0x04, 
        (byte)0xB9, (byte)0xCB, (byte)0xCE, (byte)0xDF, 
        (byte)0x14, (byte)0x15, (byte)0xFE, (byte)0x1A, 
        (byte)0x20, (byte)0xA0, (byte)0xE2, (byte)0xE4, 
        (byte)0xE0, (byte)0xE1, (byte)0xE3, (byte)0xE5, 
        (byte)0xE7, (byte)0xF1, (byte)0xA2, (byte)0x2E, 
        (byte)0x3C, (byte)0x28, (byte)0x2B, (byte)0x7C, 
        (byte)0x26, (byte)0xE9, (byte)0xEA, (byte)0xEB, 
        (byte)0xE8, (byte)0xED, (byte)0xEE, (byte)0xEF, 
        (byte)0xEC, (byte)0xDF, (byte)0x21, (byte)0x24, 
        (byte)0x2A, (byte)0x29, (byte)0x3B, (byte)0xAC, 
        (byte)0x2D, (byte)0x2F, (byte)0xC2, (byte)0xC4, 
        (byte)0xC0, (byte)0xC1, (byte)0xC3, (byte)0xC5, 
        (byte)0xC7, (byte)0xD1, (byte)0xA6, (byte)0x2C, 
        (byte)0x25, (byte)0x5F, (byte)0x3E, (byte)0x3F, 
        (byte)0xF8, (byte)0xC9, (byte)0xCA, (byte)0xCB, 
        (byte)0xC8, (byte)0xCD, (byte)0xCE, (byte)0xCF, 
        (byte)0xCC, (byte)0x60, (byte)0x3A, (byte)0x23, 
        (byte)0x40, (byte)0x27, (byte)0x3D, (byte)0x22, 
        (byte)0xD8, (byte)0x61, (byte)0x62, (byte)0x63, 
        (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, 
        (byte)0x68, (byte)0x69, (byte)0xAB, (byte)0xBB, 
        (byte)0xF0, (byte)0xFD, (byte)0xFE, (byte)0xB1, 
        (byte)0xB0, (byte)0x6A, (byte)0x6B, (byte)0x6C, 
        (byte)0x6D, (byte)0x6E, (byte)0x6F, (byte)0x70, 
        (byte)0x71, (byte)0x72, (byte)0xAA, (byte)0xBA, 
        (byte)0xE6, (byte)0xB8, (byte)0xC6, (byte)0xA4, 
        (byte)0xB5, (byte)0x7E, (byte)0x73, (byte)0x74, 
        (byte)0x75, (byte)0x76, (byte)0x77, (byte)0x78, 
        (byte)0x79, (byte)0x7A, (byte)0xA1, (byte)0xBF, 
        (byte)0xD0, (byte)0xDD, (byte)0xDE, (byte)0xAE, 
        (byte)0x5E, (byte)0xA3, (byte)0xA5, (byte)0xB7, 
        (byte)0xA9, (byte)0xA7, (byte)0xB6, (byte)0xBC, 
        (byte)0xBD, (byte)0xBE, (byte)0x5B, (byte)0x5D, 
        (byte)0xAF, (byte)0xA8, (byte)0xB4, (byte)0xD7, 
        (byte)0x7B, (byte)0x41, (byte)0x42, (byte)0x43, 
        (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, 
        (byte)0x48, (byte)0x49, (byte)0xAD, (byte)0xF4, 
        (byte)0xF6, (byte)0xF2, (byte)0xF3, (byte)0xF5, 
        (byte)0x7D, (byte)0x4A, (byte)0x4B, (byte)0x4C, 
        (byte)0x4D, (byte)0x4E, (byte)0x4F, (byte)0x50, 
        (byte)0x51, (byte)0x52, (byte)0xB9, (byte)0xFB, 
        (byte)0xFC, (byte)0xF9, (byte)0xFA, (byte)0xFF, 
        (byte)0x5C, (byte)0xF7, (byte)0x53, (byte)0x54, 
        (byte)0x55, (byte)0x56, (byte)0x57, (byte)0x58, 
        (byte)0x59, (byte)0x5A, (byte)0xB2, (byte)0xD4, 
        (byte)0xD6, (byte)0xD2, (byte)0xD3, (byte)0xD5, 
        (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, 
        (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, 
        (byte)0x38, (byte)0x39, (byte)0xB3, (byte)0xDB, 
        (byte)0xDC, (byte)0xD9, (byte)0xDA, (byte)0x1A
    };
    private static byte[] ASCII2EBCDIC = new byte[] {
        (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, 
        (byte)0x37, (byte)0x2D, (byte)0x2E, (byte)0x2F, 
        (byte)0x16, (byte)0x05, (byte)0x25, (byte)0x0B, 
        (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F, 
        (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, 
        (byte)0x3C, (byte)0x3D, (byte)0x32, (byte)0x26, 
        (byte)0x18, (byte)0x19, (byte)0x3F, (byte)0x27, 
        (byte)0x1C, (byte)0x1D, (byte)0x1E, (byte)0x1F, 
        (byte)0x40, (byte)0x5A, (byte)0x7F, (byte)0x7B, 
        (byte)0x5B, (byte)0x6C, (byte)0x50, (byte)0x7D, 
        (byte)0x4D, (byte)0x5D, (byte)0x5C, (byte)0x4E, 
        (byte)0x6B, (byte)0x60, (byte)0x4B, (byte)0x61, 
        (byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, 
        (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7, 
        (byte)0xF8, (byte)0xF9, (byte)0x7A, (byte)0x5E, 
        (byte)0x4C, (byte)0x7E, (byte)0x6E, (byte)0x6F, 
        (byte)0x7C, (byte)0xC1, (byte)0xC2, (byte)0xC3, 
        (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7, 
        (byte)0xC8, (byte)0xC9, (byte)0xD1, (byte)0xD2, 
        (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6, 
        (byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xE2, 
        (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6, 
        (byte)0xE7, (byte)0xE8, (byte)0xE9, (byte)0xBA, 
        (byte)0xE0, (byte)0xBB, (byte)0xB0, (byte)0x6D, 
        (byte)0x79, (byte)0x81, (byte)0x82, (byte)0x83, 
        (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87, 
        (byte)0x88, (byte)0x89, (byte)0x91, (byte)0x92, 
        (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96, 
        (byte)0x97, (byte)0x98, (byte)0x99, (byte)0xA2, 
        (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6, 
        (byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xC0, 
        (byte)0x4F, (byte)0xD0, (byte)0xA1, (byte)0x07, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x3F, (byte)0x3F, (byte)0x3F, (byte)0x3F, 
        (byte)0x41, (byte)0xAA, (byte)0x4A, (byte)0xB1, 
        (byte)0x9F, (byte)0xB2, (byte)0x6A, (byte)0xB5, 
        (byte)0xBD, (byte)0xB4, (byte)0x9A, (byte)0x8A, 
        (byte)0x5F, (byte)0xCA, (byte)0xAF, (byte)0xBC, 
        (byte)0x90, (byte)0x8F, (byte)0xEA, (byte)0xFA, 
        (byte)0xBE, (byte)0xA0, (byte)0xB6, (byte)0xB3, 
        (byte)0x9D, (byte)0xDA, (byte)0x9B, (byte)0x8B, 
        (byte)0xB7, (byte)0xB8, (byte)0xB9, (byte)0xAB, 
        (byte)0x64, (byte)0x65, (byte)0x62, (byte)0x66, 
        (byte)0x63, (byte)0x67, (byte)0x9E, (byte)0x68, 
        (byte)0x74, (byte)0x71, (byte)0x72, (byte)0x73, 
        (byte)0x78, (byte)0x75, (byte)0x76, (byte)0x77, 
        (byte)0xAC, (byte)0x69, (byte)0xED, (byte)0xEE, 
        (byte)0xEB, (byte)0xEF, (byte)0xEC, (byte)0xBF, 
        (byte)0x80, (byte)0xFD, (byte)0xFE, (byte)0xFB, 
        (byte)0xFC, (byte)0xAD, (byte)0xAE, (byte)0x59, 
        (byte)0x44, (byte)0x45, (byte)0x42, (byte)0x46, 
        (byte)0x43, (byte)0x47, (byte)0x9C, (byte)0x48, 
        (byte)0x54, (byte)0x51, (byte)0x52, (byte)0x53, 
        (byte)0x58, (byte)0x55, (byte)0x56, (byte)0x57, 
        (byte)0x8C, (byte)0x49, (byte)0xCD, (byte)0xCE, 
        (byte)0xCB, (byte)0xCF, (byte)0xCC, (byte)0xE1, 
        (byte)0x70, (byte)0xDD, (byte)0xDE, (byte)0xDB, 
        (byte)0xDC, (byte)0x8D, (byte)0x8E, (byte)0xDF 
    };
    public static String ebcdicToAscii(byte[] e) {
        byte[] a = new byte[e.length];
        for (int i=0; i<e.length; i++)
            a[i] = EBCDIC2ASCII[((int)e[i])&0xFF];
        return new String(a);
    }
    public static String ebcdicToAscii(byte[] e, int offset, int len) {
        byte[] a = new byte[len];
        for (int i=0; i<len; i++)
            a[i] = EBCDIC2ASCII[((int)e[offset+i])&0xFF];
        return new String(a);
    }
    public static byte[] asciiToEbcdic(String s) {
        byte[] a = s.getBytes();
        byte[] e = new byte[a.length];
        for (int i=0; i<a.length; i++) 
            e[i] = ASCII2EBCDIC[((int)a[i])&0xFF];
        return e;
    }

    /**
     * pad to the left
     * @param s - original string
     * @param len - desired len
     * @param c - padding char
     * @return padded string
     */
    public static String padleft(String s, int len, char c)
        throws ISOException 
    {
        s = s.trim();
        if (s.length() > len)
            throw new ISOException("invalid len " +s.length() + "/" +len);
        StringBuffer d = new StringBuffer (len);
        int fill = len - s.length();
        while (fill-- > 0)
            d.append (c);
        d.append(s);
        return d.toString();
    }

    /**
     * left pad with '0'
     * @param s - original string
     * @param len - desired len
     * @return zero padded string
     */
    public static String zeropad(String s, int len) throws ISOException {
        return padleft(s, len, '0');
    }

    /**
     * pads to the right
     * @param s - original string
     * @param len - desired len
     * @return space padded string
     */
    public static String strpad(String s, int len) {
        StringBuffer d = new StringBuffer(s);
        while (d.length() < len)
            d.append(' ');
        return d.toString();
    }
    public static String zeropadRight (String s, int len) {
        StringBuffer d = new StringBuffer(s);
        while (d.length() < len)
            d.append('0');
        return d.toString();
    }
    /**
     * converts to BCD
     * @param s - the number
     * @param padLeft - flag indicating left/right padding
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft) {
        int len = s.length();
        byte[] d = new byte[ (len+1) >> 1 ];
        int start = (((len & 1) == 1) && padLeft) ? 1 : 0;
        for (int i=start; i < len+start; i++) 
            d [i >> 1] |= (s.charAt(i-start)-'0') << ((i & 1) == 1 ? 0 : 4);
        return d;
    }
    /**
     * converts a BCD representation of a number to a String
     * @param b - BCD representation
     * @param offset - starting offset
     * @param len - BCD field len
     * @param padLeft - was padLeft packed?
     * @return the String representation of the number
     */
    public static String bcd2str(byte[] b, int offset,
                        int len, boolean padLeft)
    {
        StringBuffer d = new StringBuffer(len);
        int start = (((len & 1) == 1) && padLeft) ? 1 : 0;
        for (int i=start; i < len+start; i++) {
            int shift = ((i & 1) == 1 ? 0 : 4);
            char c = Character.forDigit (
                ((b[offset+(i>>1)] >> shift) & 0x0F), 16);
	    if (c == 'd')
		c = '=';
            d.append (Character.toUpperCase (c));
        }
        return d.toString();
    }
    /**
     * converts a byte array to hex string 
     * (suitable for dumps and ASCII packaging of Binary fields
     * @param b - byte array
     * @return String representation
     */
    public static String hexString(byte[] b) {
        StringBuffer d = new StringBuffer(b.length * 2);
        for (int i=0; i<b.length; i++) {
            char hi = Character.forDigit ((b[i] >> 4) & 0x0F, 16);
            char lo = Character.forDigit (b[i] & 0x0F, 16);
            d.append(Character.toUpperCase(hi));
            d.append(Character.toUpperCase(lo));
        }
        return d.toString();
    }
    /**
     * converts a byte array to printable characters
     * @param b - byte array
     * @return String representation
     */
    public static String dumpString(byte[] b) {
        StringBuffer d = new StringBuffer(b.length * 2);
        for (int i=0; i<b.length; i++) {
	    char c = (char) b[i];
	    if (Character.isISOControl (c)) {
		// TODO: complete list of control characters,
		// use a String[] instead of this weird switch
		switch (c) {
		    case '\r'  : d.append ("{CR}");   break;
		    case '\n'  : d.append ("{LF}");   break;
		    case '\000': d.append ("{NULL}"); break;
		    case '\001': d.append ("{SOH}");  break;
		    case '\002': d.append ("{STX}");  break;
		    case '\003': d.append ("{ETX}");  break;
		    case '\004': d.append ("{EOT}");  break;
		    case '\005': d.append ("{ENQ}");  break;
		    case '\006': d.append ("{ACK}");  break;
		    case '\007': d.append ("{BEL}");  break;
		    case '\020': d.append ("{DLE}");  break;
		    case '\025': d.append ("{NAK}");  break;
		    case '\026': d.append ("{SYN}");  break;
		    case '\034': d.append ("{FS}");  break;
		    default:
			char hi = Character.forDigit ((b[i] >> 4) & 0x0F, 16);
			char lo = Character.forDigit (b[i] & 0x0F, 16);
			d.append('[');
			d.append(Character.toUpperCase(hi));
			d.append(Character.toUpperCase(lo));
			d.append(']');
			break;
		}
	    }
	    else
		d.append (c);

        }
        return d.toString();
    }
    /**
     * converts a byte array to hex string 
     * (suitable for dumps and ASCII packaging of Binary fields
     * @param b - byte array
     * @param offset  - starting position
     * @param len
     * @return String representation
     */
    public static String hexString(byte[] b, int offset, int len) {
        StringBuffer d = new StringBuffer(len * 2);
        len += offset;
        for (int i=offset; i<len; i++) {
            char hi = Character.forDigit ((b[i] >> 4) & 0x0F, 16);
            char lo = Character.forDigit (b[i] & 0x0F, 16);
            d.append(Character.toUpperCase(hi));
            d.append(Character.toUpperCase(lo));
        }
        return d.toString();
    }

    /**
     * bit representation of a BitSet
     * suitable for dumps and debugging
     * @param b - the BitSet
     * @return string representing the bits (i.e. 011010010...)
     */
    public static String bitSet2String (BitSet b) {
        int len = b.size();
        len = (len > 128) ? 128: len;
        StringBuffer d = new StringBuffer(len);
        for (int i=0; i<len; i++)
            d.append (b.get(i) ? '1' : '0');
        return d.toString();
    }
    /**
     * converts a BitSet into a binary field
     * used in pack routines
     * @param b - the BitSet
     * @return binary representation
     */
    public static byte[] bitSet2byte (BitSet b)
    {
        int len = (b.size() >> 3) << 3;
        len = (len > 128) ? 128: len;
        byte[] d = new byte[len >> 3];
        for (int i=0; i<len; i++) 
            if (b.get(i+1)) 
                d[i >> 3] |= (0x80 >> (i % 8));
        if (len>64)
            d[0] |= 0x80;
        return d;
    }

    /**
     * Converts a binary representation of a Bitmap field
     * into a Java BitSet
     * @param b - binary representation
     * @param offset - staring offset
     * @param bitZeroMeansExtended - true for ISO-8583
     * @return java BitSet object
     */
    public static BitSet byte2BitSet 
        (byte[] b, int offset, boolean bitZeroMeansExtended)
    {
        int len = bitZeroMeansExtended ?
            ((b[offset] & 0x80) == 0x80 ? 128 : 64) : 64;

        BitSet bmap = new BitSet (len);
        for (int i=0; i<len; i++) 
            if (((b[offset + (i >> 3)]) & (0x80 >> (i % 8))) > 0)
                bmap.set(i+1);
        return bmap;
    }

    /**
     * Converts an ASCII representation of a Bitmap field
     * into a Java BitSet
     * @param b - binary representation
     * @param offset - staring offset
     * @param bitZeroMeansExtended - true for ISO-8583
     * @return java BitSet object
     */
    public static BitSet hex2BitSet 
        (byte[] b, int offset, boolean bitZeroMeansExtended)
    {
        int len = bitZeroMeansExtended ?
          ((Character.digit((char)b[offset],16) & 0x08) == 8 ? 128 : 64) :
          64;
        BitSet bmap = new BitSet (len);
        for (int i=0; i<len; i++) {
            int digit = Character.digit((char)b[offset + (i >> 2)], 16);
            if ((digit & (0x08 >> (i%4))) > 0)
                bmap.set(i+1);
        }
        return bmap;
    }

    /**
     * @param   b       source byte array
     * @param   offset  starting offset
     * @param   len     number of bytes in destination (procesa len*2 de source)
     * @return  byte[len]
     */
    public static byte[] hex2byte (byte[] b, int offset, int len) {
        byte[] d = new byte[len];
        for (int i=0; i<len*2; i++) {
            int shift = i%2 == 1 ? 0 : 4;
            d[i>>1] |= Character.digit((char) b[offset+i], 16) << shift;
        }
        return d;
    }

    /**
     * format double value
     * @param amount    the amount
     * @param fieldLen  the field len
     * @return a String of fieldLen characters (right justified)
     */
    public static String formatDouble(double d, int len) {
        String prefix = Long.toString((long) d);
        String suffix = Integer.toString (
            (int) ((Math.round(d * 100f)) % 100) );
        try {
            prefix = ISOUtil.padleft(prefix,len-3,' ');
            suffix = ISOUtil.zeropad(suffix, 2);
        } catch (ISOException e) {
            e.printStackTrace();
        }
        return prefix + "." + suffix;
    }
    /**
     * prepare long value used as amount for display
     * (implicit 2 decimals)
     * @param l value
     * @param len display len
     * @return formated field
     * @exception ISOException
     */
    public static String formatAmount(long l, int len) throws ISOException {
        String buf = Long.toString(l);
        if (l < 100)
            buf = zeropad(buf, 3);
        StringBuffer s = new StringBuffer(padleft (buf, len-1, ' ') );
        s.insert(len-3, '.');
        return s.toString();
    }

    /**
     * XML normalizer
     * @param s source String
     * @param canonical true if we want to normalize \r and \n as well
     * @return normalized string suitable for XML Output
     */
    public static String normalize (String s, boolean canonical) {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': 
                    str.append("&lt;");
                    break;
                case '>': 
                    str.append("&gt;");
                    break;
                case '&': 
                    str.append("&amp;");
                    break;
                case '"': 
                    str.append("&quot;");
                    break;
                case '\r':
                case '\n': 
                    if (canonical) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                    // else, default append char
                default: 
                    str.append(ch);
            }
        }
        return (str.toString());
    }
    /**
     * XML normalizer (default canonical)
     * @param s source String
     * @return normalized string suitable for XML Output
     */
    public static String normalize (String s) {
	return normalize (s, true);
    }
}
