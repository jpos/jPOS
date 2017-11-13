/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
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

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * various functions needed to pack/unpack ISO-8583 fields
 *
 * @author apr@jpos.org
 * @author Hani S. Kirollos
 * @author Alwyn Schoeman
 * @version $Id$
 * @see ISOComponent
 */
@SuppressWarnings("unused")
public class ISOUtil {
    /**
     * All methods in this class are static, so there's usually no need to instantiate it
     * We provide this public constructor in order to deal with some legacy script integration
     * that needs an instance of this class in a rendering context.
     */
    public ISOUtil() {
        super();
    }
    public static final String[] hexStrings;

    static {
        hexStrings = new String[256];
        for (int i = 0; i < 256; i++ ) {
            StringBuilder d = new StringBuilder(2);
            char ch = Character.forDigit((byte)i >> 4 & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            ch = Character.forDigit((byte)i & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            hexStrings[i] = d.toString();
        }

    }

    /**
     * Default encoding (charset) for bytes transmissions over network
     * @deprecated use {@link #CHARSET} instead
     */
    public static final String ENCODING  = "ISO8859_1";
    public static final Pattern unicodePattern = Pattern.compile("u00([0-9a-fA-F]{2})+");

    /**
     * Default charset for bytes transmissions over network
     */
    public static final Charset CHARSET  = StandardCharsets.ISO_8859_1;
    public static final Charset EBCDIC   = Charset.forName("IBM1047");

    public static final byte STX = 0x02;
    public static final byte FS  = 0x1C;
    public static final byte US  = 0x1F;
    public static final byte RS  = 0x1D;
    public static final byte GS  = 0x1E;
    public static final byte ETX = 0x03;

    public static String ebcdicToAscii(byte[] e) {
        return EBCDIC.decode(ByteBuffer.wrap(e)).toString();
    }
    public static String ebcdicToAscii(byte[] e, int offset, int len) {
        return EBCDIC.decode(ByteBuffer.wrap(e, offset, len)).toString();
    }
    public static byte[] ebcdicToAsciiBytes (byte[] e) {
        return ebcdicToAsciiBytes (e, 0, e.length);
    }
    public static byte[] ebcdicToAsciiBytes (byte[] e, int offset, int len) {
        return ebcdicToAscii(e, offset, len).getBytes(CHARSET);
    }
    public static byte[] asciiToEbcdic(String s) {
        return EBCDIC.encode(s).array();
    }
    public static byte[] asciiToEbcdic(byte[] a) {
        return EBCDIC.encode(new String(a, CHARSET)).array();
    }
    public static void asciiToEbcdic(String s, byte[] e, int offset) {
        System.arraycopy (asciiToEbcdic(s), 0, e, offset, s.length());
    }
    public static void asciiToEbcdic(byte[] s, byte[] e, int offset) {
        asciiToEbcdic(new String(s, CHARSET), e, offset);
    }

    /**
     * pad to the left
     * @param s - original string
     * @param len - desired len
     * @param c - padding char
     * @return padded string
     * @throws ISOException on error
     */
    public static String padleft(String s, int len, char c)
        throws ISOException 
    {
        s = s.trim();
        if (s.length() > len)
            throw new ISOException("invalid len " +s.length() + "/" +len);
        StringBuilder d = new StringBuilder (len);
        int fill = len - s.length();
        while (fill-- > 0)
            d.append (c);
        d.append(s);
        return d.toString();
    }
    
    /**
     * pad to the right
     * 
     * @param s -
     *            original string
     * @param len -
     *            desired len
     * @param c -
     *            padding char
     * @return padded string
     * @throws ISOException if String's length greater than pad length
     */
    public static String padright(String s, int len, char c) throws ISOException {
        s = s.trim();
        if (s.length() > len)
            throw new ISOException("invalid len " + s.length() + "/" + len);
        StringBuilder d = new StringBuilder(len);
        int fill = len - s.length();
        d.append(s);
        while (fill-- > 0)
            d.append(c);
        return d.toString();
    }

   /**
    * trim String (if not null)
    * @param s String to trim
    * @return String (may be null)
    */
    public static String trim (String s) {
        return s != null ? s.trim() : null;
    }

    /**
     * left pad with '0'
     * @param s - original string
     * @param len - desired len
     * @return zero padded string
     * @throws ISOException if string's length greater than len
     */
    public static String zeropad(String s, int len) throws ISOException {
        return padleft(s, len, '0');
    }

    /**
     * zeropads a long without throwing an ISOException (performs modulus operation)
     *
     * @param l the long
     * @param len the length
     * @return zeropadded value
     */
    public static String zeropad(long l, int len) {
        try {
            return padleft (Long.toString ((long) (l % Math.pow (10, len))), len, '0');
        } catch (ISOException ignored) { }
        return null; // should never happen
    }

    /**
     * pads to the right
     * @param s - original string
     * @param len - desired len
     * @return space padded string
     */
    public static String strpad(String s, int len) {
        StringBuilder d = new StringBuilder(s);
        while (d.length() < len)
            d.append(' ');
        return d.toString();
    }
    public static String zeropadRight (String s, int len) {
        StringBuilder d = new StringBuilder(s);
        while (d.length() < len)
            d.append('0');
        return d.toString();
    }
    /**
     * converts to BCD
     * @param s - the number
     * @param padLeft - flag indicating left/right padding
     * @param d The byte array to copy into.
     * @param offset Where to start copying into.
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft, byte[] d, int offset) {
        int len = s.length();
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i=start; i < len+start; i++) 
            d [offset + (i >> 1)] |= s.charAt(i-start)-'0' << ((i & 1) == 1 ? 0 : 4);
        return d;
    }

    /**
     * converts to BCD
     * @param s - the number
     * @param padLeft - flag indicating left/right padding
     * @param d The byte array to copy into.
     * @param offset Where to start copying into.
     * @return BCD representation of the number
     */
    public static byte[] str2hex(String s, boolean padLeft, byte[] d, int offset) {
        int len = s.length();
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i=start; i < len+start; i++)
            d [offset + (i >> 1)] |= Character.digit(s.charAt(i-start),16) << ((i & 1) == 1 ? 0 : 4);
        return d;
    }

    /**
     * converts to BCD
     * @param s - the number
     * @param padLeft - flag indicating left/right padding
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft) {
        int len = s.length();
        byte[] d = new byte[ len+1 >> 1 ];
        return str2bcd(s, padLeft, d, 0);
    }
    /**
     * converts to BCD
     * @param s - the number
     * @param padLeft - flag indicating left/right padding
     * @param fill - fill value
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft, byte fill) {
        int len = s.length();
        byte[] d = new byte[ len+1 >> 1 ];
        if (d.length > 0) {
            if (padLeft)
                d[0] = (byte) ((fill & 0xF) << 4);
            int start = (len & 1) == 1 && padLeft ? 1 : 0;
            int i;
            for (i=start; i < len+start; i++)
                d [i >> 1] |= s.charAt(i-start)-'0' << ((i & 1) == 1 ? 0 : 4);
            if ((i & 1) == 1)
                d [i >> 1] |= fill & 0xF;
        }
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
        StringBuilder d = new StringBuilder(len);
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i=start; i < len+start; i++) {
            int shift = (i & 1) == 1 ? 0 : 4;
            char c = Character.forDigit (
                    b[offset+ (i>>1)] >> shift & 0x0F, 16);
            if (c == 'd')
                c = '=';
            d.append (Character.toUpperCase (c));
        }
        return d.toString();
    }
    /**
     * converts a a byte array to a String with padding support
     * @param b - HEX representation
     * @param offset - starting offset
     * @param len - BCD field len
     * @param padLeft - was padLeft packed?
     * @return the String representation of the number
     */
    public static String hex2str(byte[] b, int offset,
                                 int len, boolean padLeft)
    {
        StringBuilder d = new StringBuilder(len);
        int start = (len & 1) == 1 && padLeft ? 1 : 0;

        for (int i=start; i < len+start; i++) {
            int shift = (i & 1) == 1 ? 0 : 4;
            char c = Character.forDigit (
                    b[offset+ (i>>1)] >> shift & 0x0F, 16);
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
        StringBuilder d = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            d.append(hexStrings[(int) aB & 0xFF]);
        }
        return d.toString();
    }
    /**
     * converts a byte array to printable characters
     * @param b - byte array
     * @return String representation
     */
    public static String dumpString(byte[] b) {
        StringBuilder d = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            char c = (char) aB;
            if (Character.isISOControl(c)) {
                // TODO: complete list of control characters,
                // use a String[] instead of this weird switch
                switch (c) {
                    case '\r':
                        d.append("{CR}");
                        break;
                    case '\n':
                        d.append("{LF}");
                        break;
                    case '\000':
                        d.append("{NULL}");
                        break;
                    case '\001':
                        d.append("{SOH}");
                        break;
                    case '\002':
                        d.append("{STX}");
                        break;
                    case '\003':
                        d.append("{ETX}");
                        break;
                    case '\004':
                        d.append("{EOT}");
                        break;
                    case '\005':
                        d.append("{ENQ}");
                        break;
                    case '\006':
                        d.append("{ACK}");
                        break;
                    case '\007':
                        d.append("{BEL}");
                        break;
                    case '\020':
                        d.append("{DLE}");
                        break;
                    case '\025':
                        d.append("{NAK}");
                        break;
                    case '\026':
                        d.append("{SYN}");
                        break;
                    case '\034':
                        d.append("{FS}");
                        break;
                    case '\036':
                        d.append("{RS}");
                        break;
                    default:
                        d.append('[');
                        d.append(hexStrings[(int) aB & 0xFF]);
                        d.append(']');
                        break;
                }
            } else
                d.append(c);

        }
        return d.toString();
    }
    /**
     * converts a byte array to hex string 
     * (suitable for dumps and ASCII packaging of Binary fields
     * @param b - byte array
     * @param offset  - starting position
     * @param len the length
     * @return String representation
     */
    public static String hexString(byte[] b, int offset, int len) {
        StringBuilder d = new StringBuilder(len * 2);
        len += offset;
        for (int i=offset; i<len; i++) {
            d.append(hexStrings[(int)b[i] & 0xFF]);
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
        int len = b.size();                             // BBB Should be length()?
        len = len > 128 ? 128: len;                     // BBB existence of 3rd bitmap not considered here
        StringBuilder d = new StringBuilder(len);
        for (int i=0; i<len; i++)
            d.append (b.get(i) ? '1' : '0');
        return d.toString();
    }
    /**
     * converts a BitSet into a binary field
     * used in pack routines
     *
     * This method will set bits 0 (and 65) if there's a secondary (and tertiary) bitmap
     * (i.e., if the bitmap length is > 64 (and > 128))
     *
     * @param b - the BitSet
     * @return binary representation
     */
    public static byte[] bitSet2byte (BitSet b)
    {
        int len = b.length()+62 >>6 <<6;        // +62 because we don't use bit 0 in the BitSet
        byte[] d = new byte[len >> 3];
        for (int i=0; i<len; i++) 
            if (b.get(i+1))                     // +1 because we don't use bit 0 of the BitSet
                d[i >> 3] |= 0x80 >> i % 8;
        if (len>64)
            d[0] |= 0x80;
        if (len>128)
            d[8] |= 0x80;
        return d;
    }
    
    /**
     * converts a BitSet into a binary field
     * used in pack routines
     *
     * This method will set bits 0 (and 65) if there's a secondary (and tertiary) bitmap
     * (i.e., if the bitmap length is > 64 (and > 128))
     *
     * @param b - the BitSet
     * @param bytes - number of bytes to return
     * @return binary representation
     */    
    public static byte[] bitSet2byte (BitSet b, int bytes)
    {
        int len = bytes * 8;
        
        byte[] d = new byte[bytes];
        for (int i=0; i<len; i++) 
            if (b.get(i+1))                     // +1 because we don't use bit 0 of the BitSet
                d[i >> 3] |= 0x80 >> i % 8;
        //TODO: review why 2nd & 3rd bit map flags are set here??? 
        if (len>64)
            d[0] |= 0x80;
        if (len>128)
            d[8] |= 0x80;
        return d;
    }
    
    /*
     * Convert BitSet to int value.
     */
    public static int bitSet2Int(BitSet bs) {
        int total = 0;
        int b = bs.length() - 1;
        if (b > 0) {
            int value = (int) Math.pow(2,b);
            for (int i = 0; i <= b; i++) {
                if (bs.get(i))
                    total += value;

                value = value >> 1;
            }
        }
        
        return total;
    }
    
    /*
     * Convert int value to BitSet.
     */
    public static BitSet int2BitSet(int value) {
        
        return int2BitSet(value,0);
    }
    
    /*
     * Convert int value to BitSet.
     */
    public static BitSet int2BitSet(int value, int offset) {
        
        BitSet bs = new BitSet();
        
        String hex = Integer.toHexString(value);
        hex2BitSet(bs, hex.getBytes(), offset);
        
        return bs;
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
                (b[offset] & 0x80) == 0x80 ? 128 : 64 : 64;
        BitSet bmap = new BitSet (len);
        for (int i=0; i<len; i++) 
            if ((b[offset + (i >> 3)] & 0x80 >> i % 8) > 0)
                bmap.set(i+1);
        return bmap;
    }
    /**
     * Converts a binary representation of a Bitmap field
     * into a Java BitSet
     * @param b - binary representation
     * @param offset - staring offset
     * @param maxBits - max number of bits (supports 64,128 or 192)
     * @return java BitSet object
     */
    public static BitSet byte2BitSet (byte[] b, int offset, int maxBits) {
        boolean  b1= (b[offset] & 0x80) == 0x80;
        boolean b65= (b.length > offset+8) && ((b[offset+8] & 0x80) == 0x80);

        int len=  (maxBits > 128 && b1 && b65) ?     192 :
                  (maxBits >  64 && b1)        ?     128 :
                  (maxBits <  64)              ? maxBits : 64;

        BitSet bmap = new BitSet (len);
        for (int i=0; i<len; i++)
            if ((b[offset + (i >> 3)] & 0x80 >> i % 8) > 0)
                bmap.set(i+1);
        return bmap;
    }

    /**
     * Converts a binary representation of a Bitmap field
     * into a Java BitSet.
     *
     * The byte[] will be fully consumed, and fed into the given BitSet starting at bitOffset+1
     *
     * @param bmap - BitSet
     * @param b - hex representation
     * @param bitOffset - (i.e. 0 for primary bitmap, 64 for secondary)
     * @return the same java BitSet object given as first argument
     */
    public static BitSet byte2BitSet (BitSet bmap, byte[] b, int bitOffset)
    {
        int len = b.length << 3;
        for (int i=0; i<len; i++) 
            if ((b[i >> 3] & 0x80 >> i % 8) > 0)
                bmap.set(bitOffset + i + 1);
        return bmap;
    }

    /**
     * Converts an ASCII representation of a Bitmap field
     * into a Java BitSet
     * @param b - hex representation
     * @param offset - starting offset
     * @param bitZeroMeansExtended - true for ISO-8583
     * @return java BitSet object
     */
    public static BitSet hex2BitSet 
        (byte[] b, int offset, boolean bitZeroMeansExtended)
    {
        int len = bitZeroMeansExtended ?
                    (Character.digit((char)b[offset],16) & 0x08) == 8 ? 128 : 64 :
                  64;
        BitSet bmap = new BitSet (len);
        for (int i=0; i<len; i++) {
            int digit = Character.digit((char)b[offset + (i >> 2)], 16);
            if ((digit & 0x08 >> i%4) > 0)
                bmap.set(i+1);
        }
        return bmap;
    }

    /**
     * Converts an ASCII representation of a Bitmap field
     * into a Java BitSet
     * @param b - hex representation
     * @param offset - starting offset
     * @param maxBits - max number of bits (supports 8, 16, 24, 32, 48, 52, 64,.. 128 or 192)
     * @return java BitSet object
     */
    public static BitSet hex2BitSet (byte[] b, int offset, int maxBits) {
        int len = maxBits > 64 ?
                    (Character.digit((char)b[offset],16) & 0x08) == 8 ? 128 : 64 :
                  maxBits;
        if (len > 64 && maxBits > 128 &&
            b.length > offset+16 &&
            (Character.digit((char)b[offset+16],16) & 0x08) == 8)
        {
            len = 192;
        }
        BitSet bmap = new BitSet (len);
        for (int i=0; i<len; i++) {
            int digit = Character.digit((char)b[offset + (i >> 2)], 16);
            if ((digit & 0x08 >> i%4) > 0) {
                bmap.set(i+1);
                if (i==65 && maxBits > 128)     // BBB this is redundant (check already done outside
                    len = 192;                  // BBB of the loop), but I'll leave it for now..
            }
        }
        return bmap;
    }

    /**
     * Converts an ASCII representation of a Bitmap field
     * into a Java BitSet
     * @param bmap - BitSet
     * @param b - hex representation
     * @param bitOffset - (i.e. 0 for primary bitmap, 64 for secondary)
     * @return java BitSet object
     */
    public static BitSet hex2BitSet (BitSet bmap, byte[] b, int bitOffset)
    {
        int len = b.length << 2;
        for (int i=0; i<len; i++) {
            int digit = Character.digit((char)b[i >> 2], 16);
            if ((digit & 0x08 >> i%4) > 0)
                bmap.set (bitOffset + i + 1);
        }
        return bmap;
    }

    /**
     * @param   b       source byte array
     * @param   offset  starting offset
     * @param   len     number of bytes in destination (processes len*2)
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
     * Converts a hex string into a byte array
     * @param s source string (with Hex representation)
     * @return byte array
     */
    public static byte[] hex2byte (String s) {
        if (s.length() % 2 == 0) {
            return hex2byte (s.getBytes(), 0, s.length() >> 1);
        } else {
        	// Padding left zero to make it even size #Bug raised by tommy
        	return hex2byte("0"+s);
        }
    }

    /**
     * Converts a byte array into a hex string
     * @param bs source byte array
     * @return hexadecimal representation of bytes
     */
    public static String byte2hex(byte[] bs) {
        return byte2hex(bs, 0, bs.length);
    }

    /**
     * Converts an integer into a byte array of hex
     *
     * @param value
     * @return bytes representation of integer
     */
    public static byte[] int2byte(int value) {
        if (value < 0) {
            return new byte[]{(byte) (value >>> 24 & 0xFF), (byte) (value >>> 16 & 0xFF),
                    (byte) (value >>> 8 & 0xFF), (byte) (value & 0xFF)};
        } else if (value <= 0xFF) {
            return new byte[]{(byte) (value & 0xFF)};
        } else if (value <= 0xFFFF) {
            return new byte[]{(byte) (value >>> 8 & 0xFF), (byte) (value & 0xFF)};
        } else if (value <= 0xFFFFFF) {
            return new byte[]{(byte) (value >>> 16 & 0xFF), (byte) (value >>> 8 & 0xFF),
                    (byte) (value & 0xFF)};
        } else {
            return new byte[]{(byte) (value >>> 24 & 0xFF), (byte) (value >>> 16 & 0xFF),
                    (byte) (value >>> 8 & 0xFF), (byte) (value & 0xFF)};
        }
    }

    /**
     * Converts a byte array of hex into an integer
     *
     * @param bytes
     * @return integer representation of bytes
     */
    public static int byte2int(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        for (int i = 0; i < 4 - bytes.length; i++) {
            byteBuffer.put((byte) 0);
        }
        for (int i = 0; i < bytes.length; i++) {
            byteBuffer.put(bytes[i]);
        }
        byteBuffer.position(0);
        return byteBuffer.getInt();
    }

    /**
     * Converts a byte array into a string of lower case hex chars.
     *
     * @param bs     A byte array
     * @param off    The index of the first byte to read
     * @param length The number of bytes to read.
     * @return the string of hex chars.
     */
    public static String byte2hex(byte[] bs, int off, int length) {
        if (bs.length <= off || bs.length < off + length)
            throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(length * 2);
        byte2hexAppend(bs, off, length, sb);
        return sb.toString();
    }

    private static void byte2hexAppend(byte[] bs, int off, int length, StringBuilder sb) {
        if (bs.length <= off || bs.length < off + length)
            throw new IllegalArgumentException();
        sb.ensureCapacity(sb.length() + length * 2);
        for (int i = off; i < off + length; i++) {
            sb.append(Character.forDigit(bs[i] >>> 4 & 0xf, 16));
            sb.append(Character.forDigit(bs[i] & 0xf, 16));
        }
    }

    /**
     * format double value
     * @param d    the amount
     * @param len  the field len
     * @return a String of fieldLen characters (right justified)
     */
    public static String formatDouble(double d, int len) {
        String prefix = Long.toString((long) d);
        String suffix = Integer.toString (
            (int) (Math.round(d * 100f) % 100) );
        try {
            if (len > 3)
                prefix = ISOUtil.padleft(prefix,len-3,' ');
            suffix = ISOUtil.zeropad(suffix, 2);
        } catch (ISOException e) {
            // should not happen
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
        StringBuilder s = new StringBuilder(padleft (buf, len-1, ' ') );
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
        StringBuilder str = new StringBuilder();

        int len = s != null ? s.length() : 0;
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
                case '\'':
                    str.append("&apos;");
                    break;
                case '\r':
                case '\n':
                    if (!canonical) {
                        str.append("&#");
                        str.append(Integer.toString(ch & 0xFF));
                        str.append(';');
                        break;
                    }
                    // else, default append char
                default: 
                    if (ch < 0x20) {
                        str.append(String.format("\\u%04x", (int) (ch & 0xFF)));
                    } else {
                        str.append(ch);
                    }
            }
        }
        return str.toString();
    }

    public static String stripUnicode (String s) {
        StringBuilder sb = new StringBuilder();
        int len = s != null ? s.length() : 0;
        boolean escape = false;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i < len-5 && isInternalUnicodeSequence(s.substring(i+1, i+6))) {
                sb.append((char) (Character.digit(s.charAt(i + 4), 16) << 4 | (Character.digit(s.charAt(i + 5), 16))));
                i += 5;
            } else
                sb.append(ch);
        }
        return sb.toString();
    }

    private static boolean isInternalUnicodeSequence(String s) {
        return unicodePattern.matcher(s).matches();
    }
    /**
     * XML normalizer (default canonical)
     * @param s source String
     * @return normalized string suitable for XML Output
     */
    public static String normalize (String s) {
        return normalize(s, true);
    }
    /**
     * Protects PAN, Track2, CVC (suitable for logs).
     *
     * <pre>
     * "40000101010001" is converted to "400001____0001"
     * "40000101010001=020128375" is converted to "400001____0001=0201_____"
     * "40000101010001D020128375" is converted to "400001____0001D0201_____"
     * "123" is converted to "___"
     * </pre>
     * @param s string to be protected 
     * @return 'protected' String
     */
    public static String protect (String s) {
        StringBuilder sb = new StringBuilder();
        int len   = s.length();
        int clear = len > 6 ? 6 : 0;
        int lastFourIndex = -1;
        if (clear > 0) {
            lastFourIndex = s.indexOf ('=') - 4;
            if (lastFourIndex < 0)
                lastFourIndex = s.indexOf ('^') - 4;
            if (lastFourIndex < 0 && s.indexOf('^')<0)
                lastFourIndex = s.indexOf('D') - 4;
            if (lastFourIndex < 0)
                lastFourIndex = len - 4;
        }
        for (int i=0; i<len; i++) {
            if (s.charAt(i) == '=' || s.charAt(i) == 'D' && s.indexOf('^')<0)
                clear = 1;  // use clear=5 to keep the expiration date
            else if (s.charAt(i) == '^') {
                lastFourIndex = 0;
                clear = len - i;
            }
            else if (i == lastFourIndex)
                clear = 4;
            sb.append (clear-- > 0 ? s.charAt(i) : '_');
        }
        s = sb.toString();
        try {
            //Addresses Track1 Truncation
            int charCount = s.replaceAll("[^\\^]", "").length();
            if (charCount == 2 ) {
                s = s.substring(0, s.lastIndexOf("^")+1);
                s = ISOUtil.padright(s, len, '_');
            }
        } catch (ISOException e){
            //cannot PAD - should never get here
        }
        return s;
    }
    public static int[] toIntArray(String s) {
        StringTokenizer st = new StringTokenizer (s);
        int[] array = new int [st.countTokens()];
        for (int i=0; st.hasMoreTokens(); i++) 
            array[i] = Integer.parseInt (st.nextToken());
        return array;
    }
    public static String[] toStringArray(String s) {
        StringTokenizer st = new StringTokenizer (s);
        String[] array = new String [st.countTokens()];
        for (int i=0; st.hasMoreTokens(); i++) 
            array[i] = st.nextToken();
        return array;
    }
    /**
     * Bitwise XOR between corresponding bytes
     * @param op1 byteArray1
     * @param op2 byteArray2
     * @return an array of length = the smallest between op1 and op2
     */
    public static byte[] xor (byte[] op1, byte[] op2) {
        byte[] result;
        // Use the smallest array
        if (op2.length > op1.length) {
            result = new byte[op1.length];
        }
        else {
            result = new byte[op2.length];
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte)(op1[i] ^ op2[i]);
        }
        return  result;
    }
    /**
     * Bitwise XOR between corresponding byte arrays represented in hex
     * @param op1 hexstring 1
     * @param op2 hexstring 2
     * @return an array of length = the smallest between op1 and op2
     */
    public static String hexor (String op1, String op2) {
        byte[] xor = xor (hex2byte (op1), hex2byte (op2));
        return hexString (xor);
    }

    /**
     * Trims a byte[] to a certain length
     * @param array the byte[] to be trimmed
     * @param length the wanted length
     * @return the trimmed byte[]
     */
    public static byte[] trim (byte[] array, int length) {
        byte[] trimmedArray = new byte[length];
        System.arraycopy(array, 0, trimmedArray, 0, length);
        return  trimmedArray;
    }

    /**
     * Concatenates two byte arrays (array1 and array2)
     * @param array1 first part
     * @param array2 last part
     * @return the concatenated array
     */
    public static byte[] concat (byte[] array1, byte[] array2) {
        byte[] concatArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, concatArray, 0, array1.length);
        System.arraycopy(array2, 0, concatArray, array1.length, array2.length);
        return  concatArray;
    }

    /**
     * Concatenates two byte arrays (array1 and array2)
     * @param array1 first part
     * @param beginIndex1 initial index
     * @param length1  length
     * @param array2 last part
     * @param beginIndex2 last part index
     * @param length2 last part length
     * @return the concatenated array
     */
    public static byte[] concat (byte[] array1, int beginIndex1, int length1, byte[] array2,
            int beginIndex2, int length2) {
        byte[] concatArray = new byte[length1 + length2];
        System.arraycopy(array1, beginIndex1, concatArray, 0, length1);
        System.arraycopy(array2, beginIndex2, concatArray, length1, length2);
        return  concatArray;
    }
    /** 
     * Causes the currently executing thread to sleep (temporarily cease 
     * execution) for the specified number of milliseconds. The thread 
     * does not lose ownership of any monitors.
     *
     * This is the same as Thread.sleep () without throwing InterruptedException
     *
     * @param      millis   the length of time to sleep in milliseconds.
     */
    public static void sleep (long millis) {
        try {
            Thread.sleep (millis);
        } catch (InterruptedException ignored) { }
    }

    /**
     * Left unPad with '0'
     * @param s - original string
     * @return zero unPadded string
     */
    public static String zeroUnPad( String s ) {
        return unPadLeft(s, '0');
    }
    /**
     * Right unPad with ' '
     * @param s - original string
     * @return blank unPadded string
     */
    public static String blankUnPad( String s ) {
        return unPadRight(s, ' ');
    }

    /**
     * Unpad from right. 
     * @param s - original string
     * @param c - padding char
     * @return unPadded string.
     */
    public static String unPadRight(String s, char c) {
        int end = s.length();
        if (end == 0)
            return s;
        while ( 0 < end && s.charAt(end-1) == c) end --;
        return 0 < end ? s.substring( 0, end ): s.substring( 0, 1 );
    }

    /**
     * Unpad from left. 
     * @param s - original string
     * @param c - padding char
     * @return unPadded string.
     */
    public static String unPadLeft(String s, char c) {
        int fill = 0, end = s.length();
        if (end == 0)
            return s;
        while ( fill < end && s.charAt(fill) == c) fill ++;
        return fill < end ? s.substring( fill, end ): s.substring( fill-1, end );
    }

    /**
     * @return true if the string is zero-filled ( 0 char filled )
     **/
    public static boolean isZero( String s ) {
        int i = 0, len = s.length();
        while ( i < len && s.charAt( i ) == '0'){
            i++;
        }
        return i >= len;
    }

    /**
     * @return true if the string is blank filled (space char filled)
     */
    public static boolean isBlank( String s ){
        return s.trim().length() == 0;
    }

    /**
     * Return true if the string is alphanum.
     * <code>{letter digit (.) (_) (-) ( ) (?) }</code>
     * 
     **/
    public static boolean isAlphaNumeric ( String s ) {
        int i = 0, len = s.length();
        while ( i < len && ( Character.isLetterOrDigit( s.charAt( i ) ) ||
                             s.charAt( i ) == ' ' || s.charAt( i ) == '.' ||
                             s.charAt( i ) == '-' || s.charAt( i ) == '_' )
                             || s.charAt(i) == '?' ){
            i++;
        }
        return i >= len;
    }

    /**
     * Return true if the string represent a number
     * in the specified radix.
     * <br><br>
     **/
    public static boolean isNumeric ( String s, int radix ) {
        int i = 0, len = s.length();
        while ( i < len && Character.digit(s.charAt(i), radix) > -1  ){
            i++;
        }
        return i >= len && len > 0;
    }

    /**
     * Converts a BitSet into an extended binary field
     * used in pack routines. The result is always in the
     * extended format: (16 bytes of length)
     * <br><br>
     * @param b the BitSet
     * @return binary representation
     */
    public static byte[] bitSet2extendedByte ( BitSet b ){
        int len = 128;
        byte[] d = new byte[len >> 3];
        for ( int i=0; i<len; i++ )
            if (b.get(i+1))
                d[i >> 3] |= 0x80 >> i % 8;
        d[0] |= 0x80;
        return d;
    }

    /**
     * Converts a String to an integer of base radix.
     * <br><br>
     * String constraints are:
     * <li>Number must be less than 10 digits</li>
     * <li>Number must be positive</li>
     * @param s String representation of number
     * @param radix Number base to use
     * @return integer value of number
     * @throws NumberFormatException
     */
    public static int parseInt (String s, int radix) throws NumberFormatException {
        int length = s.length();
        if (length > 9)
            throw new NumberFormatException ("Number can have maximum 9 digits");
        int result;
        int index = 0;
        int digit = Character.digit (s.charAt(index++), radix);
        if (digit == -1)
            throw new NumberFormatException ("String contains non-digit");
        result = digit;
        while (index < length) {
            result *= radix;
            digit = Character.digit (s.charAt(index++), radix);
            if (digit == -1)
                throw new NumberFormatException ("String contains non-digit");
            result += digit;
        }
        return result;
    }

    /**
     * Converts a String to an integer of radix 10.
     * <br><br>
     * String constraints are:
     * <li>Number must be less than 10 digits</li>
     * <li>Number must be positive</li>
     * @param s String representation of number
     * @return integer value of number
     * @throws NumberFormatException
     */
    public static int parseInt (String s) throws NumberFormatException {
        return parseInt (s, 10);
    }

    /**
     * Converts a character array to an integer of base radix.
     * <br><br>
     * Array constraints are:
     * <li>Number must be less than 10 digits</li>
     * <li>Number must be positive</li>
     * @param cArray Character Array representation of number
     * @param radix Number base to use
     * @return integer value of number
     * @throws NumberFormatException
     */
    public static int parseInt (char[] cArray, int radix) throws NumberFormatException {
        int length = cArray.length;
        if (length > 9)
            throw new NumberFormatException ("Number can have maximum 9 digits");
        int result;
        int index = 0;
        int digit = Character.digit(cArray[index++], radix);
        if (digit == -1)
            throw new NumberFormatException ("Char array contains non-digit");
        result = digit;
        while (index < length) {
            result *= radix;
            digit = Character.digit(cArray[index++],radix);
            if (digit == -1)
                throw new NumberFormatException ("Char array contains non-digit");
            result += digit;
        }
        return result;
    }

    /**
     * Converts a character array to an integer of radix 10.
     * <br><br>
     * Array constraints are:
     * <li>Number must be less than 10 digits</li>
     * <li>Number must be positive</li>
     * @param cArray Character Array representation of number
     * @return integer value of number
     * @throws NumberFormatException
     */
    public static int parseInt (char[] cArray) throws NumberFormatException {
        return parseInt (cArray,10);
    }

    /**
     * Converts a byte array to an integer of base radix.
     * <br><br>
     * Array constraints are:
     * <li>Number must be less than 10 digits</li>
     * <li>Number must be positive</li>
     * @param bArray Byte Array representation of number
     * @param radix Number base to use
     * @return integer value of number
     * @throws NumberFormatException
     */
    public static int parseInt (byte[] bArray, int radix) throws NumberFormatException {
        int length = bArray.length;
        if (length > 9)
            throw new NumberFormatException ("Number can have maximum 9 digits");
        int result;
        int index = 0;
        int digit = Character.digit((char)bArray[index++], radix);
        if (digit == -1)
            throw new NumberFormatException ("Byte array contains non-digit");
        result = digit;
        while (index < length) {
            result *= radix;
            digit = Character.digit((char)bArray[index++],radix);
            if (digit == -1)
                throw new NumberFormatException ("Byte array contains non-digit");
            result += digit;
        }
        return result;
    }

    /**
     * Converts a byte array to an integer of radix 10.
     * <br><br>
     * Array constraints are:
     * <li>Number must be less than 10 digits</li>
     * <li>Number must be positive</li>
     * @param bArray Byte Array representation of number
     * @return integer value of number
     * @throws NumberFormatException
     */
    public static int parseInt (byte[] bArray) throws NumberFormatException {
        return parseInt (bArray,10);
    }
    private static String hexOffset (int i) {
        i = i>>4 << 4;
        int w = i > 0xFFFF ? 8 : 4;
        try {
            return zeropad (Integer.toString (i, 16), w);
        } catch (ISOException e) {
            // should not happen
            return e.getMessage();
        }
    }

    /**
     * @param b a byte[] buffer
     * @return hexdump
     */
    public static String hexdump (byte[] b) {
        return hexdump (b, 0, b.length);
    }

    /**
     * @param b a byte[] buffer
     * @param offset starting offset
     */
    public static String hexdump (byte[] b, int offset) {
        return hexdump (b, offset, b.length-offset);
    }

    /**
     * @param b a byte[] buffer
     * @param offset starting offset
     * @param len the Length
     * @return hexdump
     */
    public static String hexdump (byte[] b, int offset, int len) {
        StringBuilder sb    = new StringBuilder ();
        StringBuilder hex   = new StringBuilder ();
        StringBuilder ascii = new StringBuilder ();
        String sep         = "  ";
        String lineSep     = System.getProperty ("line.separator");
        len = offset + len;

        for (int i=offset; i<len; i++) {
            hex.append(hexStrings[(int)b[i] & 0xFF]);
            hex.append (' ');
            char c = (char) b[i];
            ascii.append (c >= 32 && c < 127 ? c : '.');

            int j = i % 16;
            switch (j) {
                case 7 :
                    hex.append (' ');
                    break;
                case 15 :
                    sb.append (hexOffset (i));
                    sb.append (sep);
                    sb.append (hex.toString());
                    sb.append (' ');
                    sb.append (ascii.toString());
                    sb.append (lineSep);
                    hex   = new StringBuilder ();
                    ascii = new StringBuilder ();
                    break;
            }
        }
        if (hex.length() > 0) {
            while (hex.length () < 49)
                hex.append (' ');

            sb.append (hexOffset (len));
            sb.append (sep);
            sb.append (hex.toString());
            sb.append (' ');
            sb.append (ascii.toString());
            sb.append (lineSep);
        }
        return sb.toString();
    }
    /**
     * pads a string with 'F's (useful for pinoffset management)
     * @param s an [hex]string
     * @param len desired length
     * @return string right padded with 'F's
     */
    public static String strpadf (String s, int len) {
        StringBuilder d = new StringBuilder(s);
        while (d.length() < len)
            d.append('F');
        return d.toString();
    }
    /**
     * reverse the effect of strpadf
     * @param s F padded string
     * @return trimmed string
     */
    public static String trimf (String s) {
        if (s != null) {
            int l = s.length();
            if (l > 0) {
                while (--l >= 0) {
                    if (s.charAt (l) != 'F')
                        break;
                }
                s = l == 0 ? "" : s.substring (0, l+1);
            }
        }
        return s;
    }
    
    /**
     * return the last n characters of the passed String, left padding where required with 0
     * 
     * @param s
     *            String to take from
     * @param n nuber of characters to take
     * 
     * @return String (may be null)
     */
    public static String takeLastN(String s,int n) throws ISOException {
        if (s.length()>n) {
            return s.substring(s.length()-n);
        }
        else {
            if (s.length()<n){
                return zeropad(s,n);                
            }
            else {
                return s;
            }
        }
    }
    
    /**
     * return the first n characters of the passed String, left padding where required with 0
     * 
     * @param s
     *            String to take from
     * @param n nuber of characters to take
     * 
     * @return String (may be null)
     */
    public static String takeFirstN(String s,int n) throws ISOException {
        if (s.length()>n) {
            return s.substring(0,n);
        }
        else {
            if (s.length()<n){
                return zeropad(s,n);                
            }
            else {
                return s;
            }
        }
    }
    public static String millisToString (long millis) {
        StringBuilder sb = new StringBuilder();
        if (millis < 0) {
            millis = -millis;
            sb.append('-');
        }
        int ms = (int) (millis % 1000);
        millis /= 1000;
        int dd = (int) (millis/86400);
        millis -= dd * 86400;
        int hh = (int) (millis/3600);
        millis -= hh * 3600;
        int mm = (int) (millis/60);
        millis -= mm * 60;
        int ss = (int) millis;
        if (dd > 0) {
            sb.append (Long.toString(dd));
            sb.append ("d ");
        }
        sb.append (zeropad (hh, 2));
        sb.append (':');
        sb.append (zeropad (mm, 2));
        sb.append (':');
        sb.append (zeropad (ss, 2));
        sb.append ('.');
        sb.append (zeropad (ms, 3));
        return sb.toString();
    }

    /**
     * Format a string containing a amount conversion rate in the proper format
     * <p/>
     * Format:
     * The leftmost digit (i.e., position 1) of this data element denotes the number of
     * positions the decimal separator must be moved from the right. Positions 2–8 of
     * this data element specify the rate. For example, a conversion rate value of
     * 91234567 in this data element would equate to 0.001234567.
     *
     * @param convRate - amount conversion rate
     * @return a string containing a amount conversion rate in the proper format,
     *         witch is suitable for create fields 10 and 11
     * @throws ISOException
     */
    public static String formatAmountConversionRate(double convRate) throws ISOException {
        if (convRate == 0)
            return null;
        BigDecimal cr = new BigDecimal(convRate);
        int x = 7 - cr.precision() + cr.scale();
        String bds = cr.movePointRight(cr.scale()).toString();
        if (x > 9)
            bds = ISOUtil.zeropad(bds, bds.length() + x - 9);
        String ret = ISOUtil.zeropadRight(bds, 7);
        return Math.min(9, x) + ISOUtil.takeFirstN(ret, 7);
    }

    /**
     * Parse currency amount conversion rate string
     * <p/>
     * Suitble for parse fields 10 and 11
     *
     * @param convRate amount conversation rate
     * @return parsed currency amount conversation rate
     * @throws IllegalArgumentException
     */
    public static double parseAmountConversionRate(String convRate) {
        if (convRate == null || convRate.length() != 8)
            throw new IllegalArgumentException("Invalid amount converion rate argument: '" +
                    convRate + "'");
        BigDecimal bd = new BigDecimal(convRate);
        int pow = bd.movePointLeft(7).intValue();
        bd = new BigDecimal(convRate.substring(1));
        return bd.movePointLeft(pow).doubleValue();
    }

    /**
     * Converts a string[] into a comma-delimited String.
     *
     * Takes care of escaping commas using a backlash
     * @see org.jpos.iso.ISOUtil#commaDecode(String)
     * @param ss string array to be comma encoded
     * @return comma encoded string
     */
    public static String commaEncode (String[] ss) {
        StringBuilder sb = new StringBuilder();
        for (String s : ss) {
            if (sb.length() > 0)
                sb.append(',');
            if (s != null) {
                for (int i = 0; i<s.length(); i++) {
                    char c = s.charAt(i);
                    switch (c) {
                        case '\\':
                        case ',' :
                            sb.append('\\');
                            break;
                    }
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Decodes a comma encoded String as encoded by commaEncode
     * @see org.jpos.iso.ISOUtil#commaEncode(String[])
     * @param s the command encoded String
     * @return String[]
     */
    public static String[] commaDecode (String s) {
        List<String> l = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (!escaped) {
                switch (c) {
                    case '\\':
                        escaped = true;
                        continue;
                    case ',':
                        l.add(sb.toString());
                        sb = new StringBuilder();
                        continue;
                }
            }
            sb.append(c);
            escaped=false;
        }
        if (sb.length() > 0)
            l.add(sb.toString());
        return l.toArray(new String[l.size()]);
    }

    /**
     * Decodes a comma encoded String returning element in position i
     * @param s comma encoded string
     * @param i position (starts at 0)
     * @return element in position i of comma encoded string, or null
     */
    public static String commaDecode (String s, int i) {
        String[] ss = commaDecode(s);
        int l = ss.length;
        return i >= 0 && i < l ? ss[i] : null;
    }

    /**
     * Compute card's check digit (LUHN)
     * @param p PAN (without checkdigit)
     * @return the checkdigit
     */
    public static char calcLUHN (String p) {
        int i, crc;
        int odd = p.length() % 2;

        for (i=crc=0; i<p.length(); i++) {
            char c = p.charAt(i);
            if (!Character.isDigit (c)) {
                throw new IllegalArgumentException("Invalid PAN " + p);
            }
            c = (char) (c - '0');
            if (i % 2 != odd)
                crc+= c*2 >= 10 ? c*2 -9 : c*2;
            else
                crc+=c;
        }

        return (char) ((crc % 10 == 0 ? 0 : 10 - crc % 10) + '0');
    }

    public static String getRandomDigits(Random r, int l, int radix) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<l; i++) {
            sb.append (r.nextInt(radix));
        }
        return sb.toString();
    }

    // See http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
    // and http://physics.nist.gov/cuu/Units/binary.html
    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "Bi", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    /**
     * At times when the charset is not the default usual one the dump will have more unprintable characters than printable.
     * The charset will allow printing of more printable character. Usually when your data is in EBCDIC format you will run into this.
     * 
     * The standard hexdump that exists would print a byte array of F0F1F2 as 
     * F0 F1 F2        ...
     * 
     * This hexdump, if the Charset.forName("IBM1047") is passedin as charset will print
     * F0 F1 F2       | 123      
     * 
     * 
     * @param array
     *            the array that needs to be dumped.
     * @param offset
     *            From where the data needs to be dumped.
     * @param length
     *            The number of byte that ned to be dumped.
     * @param charSet
     *            The Charset encoding the array is i.
     * @return The hexdump string.
     */
    public static String hexDump(byte[] array, int offset, int length, Charset charSet) {
        // https://gist.github.com/jen20/906db194bd97c14d91df
        final int width = 16;

        StringBuilder builder = new StringBuilder();

        for (int rowOffset = offset; rowOffset < offset + length; rowOffset += width) {
            builder.append(String.format("%06d:  ", rowOffset));

            for (int index = 0; index < width; index++) {
                if (rowOffset + index < array.length) {
                    builder.append(String.format("%02x ", array[rowOffset + index]).toUpperCase());
                }
                else {
                    builder.append("   ");
                }
            }

            if (rowOffset < array.length) {
                int asciiWidth = Math.min(width, array.length - rowOffset);
                builder.append("  |  ");
                builder.append(new String(array, rowOffset, asciiWidth, charSet).replaceAll("\r\n", " ")
                        .replaceAll("\n", " "));
            }

            builder.append(String.format("%n"));
        }

        return builder.toString();
    }

}
