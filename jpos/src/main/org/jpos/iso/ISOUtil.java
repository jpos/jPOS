/**
 * ISOUtil (kinda csstr)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */

/*
 * $Log$
 * Revision 1.2  1998/12/01 12:48:12  apr
 * Added padleft
 *
 * Revision 1.1  1998/11/09 23:40:33  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.util.*;

public class ISOUtil {
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

	public static String zeropad(String s, int len) throws ISOException {
		return padleft(s, len, '0');
	}

	public static String strpad(String s, int len) {
		StringBuffer d = new StringBuffer(s);
		while (d.length() < len)
			d.append(' ');
		return d.toString();
	}

	public static byte[] str2bcd(String s, boolean padLeft) {
		int len = s.length();
		byte[] d = new byte[ (len+1) >> 1 ];
		int start = (((len & 1) == 1) && padLeft) ? 1 : 0;
		for (int i=start; i < len+start; i++) 
			d [i >> 1] |= (s.charAt(i-start)-'0') << ((i & 1) == 1 ? 0 : 4);
		return d;
	}
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

	public static String bitSet2String (BitSet b) {
		int len = b.size();
		StringBuffer d = new StringBuffer(len);
		for (int i=0; i<len; i++)
			d.append (b.get(i) ? '1' : '0');
		return d.toString();
	}

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

	public static BitSet byte2BitSet (byte[] b, int offset) {
		int len = (b[offset] & 0x80) == 0x80 ? 128 : 64;
		BitSet bmap = new BitSet (len);
		for (int i=0; i<len; i++) 
			if (((b[offset + (i >> 3)]) & (0x80 >> (i % 8))) > 0)
				bmap.set(i+1);
		return bmap;
	}

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

    public static void main (String args[]) {
        String s = "123456789";
        try {
            s = zeropad (s, 9);
			byte[] b;

			// System.out.println ("---[ testing hexString ]---");
			// b = new byte[4];
			// b[0] = 9;
			// b[1] = 10;
			// b[2] = 11;
			// b[3] = 12;
			// System.out.println ("hexString()="+ hexString(b));

			// System.out.println ("---[ padLeft false ]---");
            // b = str2bcd (s, false);
            // System.out.println ("hex=" +hexString(b));
            // System.out.println ("bcd=" +bcd2str(b, 0, 9, false));

			// System.out.println ("---[ padLeft true ]---");
            // b = str2bcd (s, true);
            // System.out.println ("hex=" +hexString(b));
            // System.out.println ("bcd=" +bcd2str(b, 0, 9, true));

			BitSet bitset = new BitSet (64);
			bitset.set(3);
			bitset.set(7);
			System.out.println (bitSet2String (bitset));
			b = bitSet2byte (bitset);
            System.out.println ("hex=" +hexString(b));

			BitSet destbitset = byte2BitSet (b, 0);
			System.out.println ("destbitset=" +destbitset);

			// String hexbit = "0123456789ABCEF0";
			// byte[] binbit = hex2byte(hexbit.getBytes(), 0, 8);
			// System.out.println ("binbit=" +hexString(binbit));

        } catch (ISOException e) {
            e.printStackTrace();
        }
    }
}
