package uy.com.cs.jpos.iso;

import java.util.*;

/**
 * varios functions needed to pack/unpack ISO-8583 fields
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class ISOUtil {
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
	 * @return zero padded string
	 */
	public static String strpad(String s, int len) {
		StringBuffer d = new StringBuffer(s);
		while (d.length() < len)
			d.append(' ');
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
			d.append ((b[offset+(i>>1)] >> shift) & 0x0F);
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
	 * bit representation of a BitSet
	 * suitable for dumps and debugging
	 * @param b - the BitSet
	 * @return string representing the bits (i.e. 011010010...)
	 */
	public static String bitSet2String (BitSet b) {
		int len = b.size();
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
	public static byte[] bitSet2byte (BitSet b) {
		int len = (b.size() >> 3) << 3;
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
	 * @return java BitSet object
	 */
	public static BitSet byte2BitSet (byte[] b, int offset) {
		int len = (b[offset] & 0x80) == 0x80 ? 128 : 64;
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
	 * @return java BitSet object
	 */
	public static BitSet hex2BitSet (byte[] b, int offset) {
		int len = (Character.digit((char)b[offset],16) & 0x08) == 8 ? 128 : 64;
		BitSet bmap = new BitSet (len);
		for (int i=0; i<len; i++) {
			int digit = Character.digit((char)b[offset + (i >> 2)], 16);
			if ((digit & (0x08 >> (i%4))) > 0)
				bmap.set(i+1);
		}
		return bmap;
	}

	/**
	 * @param	b		source byte array
	 * @param	offset	starting offset
	 * @param	len		number of bytes in destination (procesa len*2 de source)
	 * @return	byte[len]
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
	 * @param amount	the amount
	 * @param fieldLen	the field len
	 * @return a String of fieldLen characters (right justified)
	 */
	public static String formatDouble(double d, int len) {
		String prefix = Long.toString((long) d);
		String suffix = Integer.toString ((int) ((d * 100) % 100));
		try {
			prefix = ISOUtil.padleft(prefix,len-3,' ');
			suffix = ISOUtil.zeropad(suffix, 2);
		} catch (ISOException e) {
			e.printStackTrace();
		}
		return prefix + "." + suffix;
	}
}
