/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
import java.math.MathContext;

import java.util.BitSet;

import org.junit.Test;

import static org.junit.Assert.*;

public class ISOUtilTest {
    final String lineSep = System.getProperty("line.separator");

    @Test
    public void testAsciiToEbcdic() throws Throwable {
        byte[] a = new byte[0];
        byte[] result = ISOUtil.asciiToEbcdic(a);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testAsciiToEbcdic1() throws Throwable {
        byte[] a = new byte[1];
        byte[] result = ISOUtil.asciiToEbcdic(a);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testAsciiToEbcdic2() throws Throwable {
        byte[] e = new byte[13];
        ISOUtil.asciiToEbcdic("testISOUtils", e, 0);
        assertEquals("e[0]", (byte) -93, e[0]);
    }

    @Test
    public void testAsciiToEbcdic3() throws Throwable {
        byte[] e = new byte[3];
        ISOUtil.asciiToEbcdic("", e, 100);
        assertEquals("e.length", 3, e.length);
    }

    @Test
    public void testAsciiToEbcdic4() throws Throwable {
        byte[] result = ISOUtil.asciiToEbcdic("");
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testAsciiToEbcdic5() throws Throwable {
        byte[] result = ISOUtil.asciiToEbcdic("testISOUtils");
        assertEquals("result.length", 12, result.length);
        assertEquals("result[0]", (byte) -93, result[0]);
    }

    @Test
    public void testAsciiToEbcdicThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] e = new byte[0];
        try {
            ISOUtil.asciiToEbcdic("testISOUtils", e, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
            assertEquals("e.length", 0, e.length);
        }
    }

    @Test
    public void testAsciiToEbcdicThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] e = new byte[1];
        try {
            ISOUtil.asciiToEbcdic("testISOUtils", e, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("e[0]", (byte) -93, e[0]);
            assertEquals("ex.getMessage()", "1", ex.getMessage());
            assertEquals("e.length", 1, e.length);
        }
    }

    @Test
    public void testAsciiToEbcdicThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.asciiToEbcdic((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testAsciiToEbcdicThrowsNullPointerException1() throws Throwable {
        byte[] e = new byte[3];
        try {
            ISOUtil.asciiToEbcdic(null, e, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("e.length", 3, e.length);
        }
    }

    @Test
    public void testAsciiToEbcdicThrowsNullPointerException2() throws Throwable {
        try {
            ISOUtil.asciiToEbcdic((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBcd2str() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.bcd2str(b, 100, 0, true);
        assertEquals("result", "", result);
    }

    @Test
    public void testBcd2str1() throws Throwable {
        byte[] b = new byte[2];
        String result = ISOUtil.bcd2str(b, 0, 2, true);
        assertEquals("result", "00", result);
    }

    @Test
    public void testBcd2str2() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) -61;
        String result = ISOUtil.bcd2str(b, 0, 1, false);
        assertEquals("result", "C", result);
    }

    @Test
    public void testBcd2str3() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) -3;
        String result = ISOUtil.bcd2str(b, 0, 1, true);
        assertEquals("result", "=", result);
    }

    @Test
    public void testBcd2str4() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) -31;
        String result = ISOUtil.bcd2str(b, 0, 1, false);
        assertEquals("result", "E", result);
    }

    @Test
    public void testBcd2str5() throws Throwable {
        byte[] b = new byte[3];
        String result = ISOUtil.bcd2str(b, 0, 1, true);
        assertEquals("result", "0", result);
    }

    @Test
    public void testBcd2str6() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 14;
        String result = ISOUtil.bcd2str(b, 0, 3, true);
        assertEquals("result", "00E", result);
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[41];
        b[25] = (byte) -100;
        b[30] = (byte) 13;
        b[35] = (byte) -29;
        try {
            ISOUtil.bcd2str(b, 16, 61, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "41", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[41];
        b[25] = (byte) -100;
        b[30] = (byte) 13;
        b[35] = (byte) -29;
        try {
            ISOUtil.bcd2str(b, 16, 61, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "41", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[25];
        b[2] = (byte) -63;
        b[3] = (byte) 62;
        b[23] = (byte) 29;
        try {
            ISOUtil.bcd2str(b, 0, 100, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "25", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] b = new byte[41];
        b[25] = (byte) -100;
        b[35] = (byte) -29;
        try {
            ISOUtil.bcd2str(b, 16, 61, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "41", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 28;
        try {
            ISOUtil.bcd2str(b, 0, 27, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] b = new byte[25];
        b[2] = (byte) -63;
        b[3] = (byte) 62;
        try {
            ISOUtil.bcd2str(b, 0, 100, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "25", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsNegativeArraySizeException() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.bcd2str(b, 100, -1, true);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.bcd2str((byte[]) null, 100, 1, false);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.bcd2str((byte[]) null, 100, 1, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBcd2strThrowsNullPointerException2() throws Throwable {
        try {
            ISOUtil.bcd2str((byte[]) null, 100, 1000, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBitSet2byte() throws Throwable {
        byte[] b = new byte[1];
        BitSet bmap = new BitSet();
        ISOUtil.hex2BitSet(bmap, b, 0);
        byte[] b2 = new byte[0];
        ISOUtil.hex2BitSet(bmap, b2, -1);
        BitSet b3 = new BitSet(100);
        b3.or(bmap);
        byte[] result = ISOUtil.bitSet2byte(b3);
        assertEquals("result.length", 8, result.length);
        assertEquals("result[0]", (byte) -16, result[0]);
    }

    @Test
    public void testBitSet2byte3() throws Throwable {
        byte[] result = ISOUtil.bitSet2byte(new BitSet(100));
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testBitSet2byteThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.bitSet2byte(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBitSet2extendedByte() throws Throwable {
        byte[] result = ISOUtil.bitSet2extendedByte(new BitSet(100));
        assertEquals("result.length", 16, result.length);
        assertEquals("result[0]", (byte) -128, result[0]);
    }

    @Test
    public void testBitSet2extendedByteThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.bitSet2extendedByte(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBitSet2String() throws Throwable {
        BitSet bmap = new BitSet(100);
        byte[] b = new byte[1];
        ISOUtil.byte2BitSet(bmap, b, 100);
        bmap.set(100);
        bmap.flip(0, 100);
        String result = ISOUtil.bitSet2String(bmap);
        assertEquals(
                "result",
                "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111000000000000000000000000000",
                result);
    }

    @Test
    public void testBitSet2String1() throws Throwable {
        String result = ISOUtil.bitSet2String(new BitSet(100));
        assertEquals(
                "result",
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                result);
    }

    @Test
    public void testBitSet2String2() throws Throwable {
        String result = ISOUtil.bitSet2String(new BitSet(0));
        assertEquals("result", "", result);
    }

    @Test
    public void testBitSet2StringThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.bitSet2String(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testBlankUnPad() throws Throwable {
        String result = ISOUtil.blankUnPad("");
        assertEquals("result", "", result);
    }

    @Test
    public void testBlankUnPad1() throws Throwable {
        String result = ISOUtil.blankUnPad("1");
        assertEquals("result", "1", result);
    }

    @Test
    public void testBlankUnPadThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.blankUnPad(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSet() throws Throwable {
        byte[] b = new byte[9];
        BitSet result = ISOUtil.byte2BitSet(b, 0, true);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSet1() throws Throwable {
        byte[] b = new byte[9];
        b[4] = (byte) 1;
        BitSet result = ISOUtil.byte2BitSet(b, 0, true);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSet11() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 63);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSet2() throws Throwable {
        byte[] b = new byte[10];
        BitSet result = ISOUtil.byte2BitSet(b, 0, 1000);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSet3() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 127);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSet5() throws Throwable {
        byte[] b = new byte[1];
        BitSet result = ISOUtil.byte2BitSet((BitSet) null, b, 100);
        assertNull("result", result);
    }

    @Test
    public void testByte2BitSet6() throws Throwable {
        byte[] b = new byte[0];
        BitSet result = ISOUtil.byte2BitSet((BitSet) null, b, 100);
        assertNull("result", result);
    }

    @Test
    public void testByte2BitSet7() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 128);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSet8() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 1000);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSet9() throws Throwable {
        byte[] b = new byte[10];
        BitSet result = ISOUtil.byte2BitSet(b, 0, 100);
        assertEquals("result.size()", 64, result.size());
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[71];
        b[62] = (byte) -128;
        b[63] = (byte) 1;
        try {
            ISOUtil.byte2BitSet(b, 62, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "71", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 3;
        try {
            ISOUtil.byte2BitSet(b, 0, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException10() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.byte2BitSet(b, 100, 64);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException12() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) -29;
        try {
            ISOUtil.byte2BitSet(b, 0, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException13() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException14() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException15() throws Throwable {
        byte[] b = new byte[0];
        try {
            ISOUtil.byte2BitSet(b, 100, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException16() throws Throwable {
        byte[] b = new byte[0];
        try {
            ISOUtil.byte2BitSet(b, 100, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException17() throws Throwable {
        byte[] b = new byte[12];
        b[1] = (byte) 1;
        b[9] = (byte) -128;
        try {
            ISOUtil.byte2BitSet(b, 1, 129);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "12", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException18() throws Throwable {
        byte[] b = new byte[12];
        b[1] = (byte) -63;
        b[9] = (byte) -128;
        try {
            ISOUtil.byte2BitSet(b, 1, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "12", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException19() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 59;
        try {
            ISOUtil.byte2BitSet(b, 0, 128);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) -1;
        try {
            ISOUtil.byte2BitSet(b, 1, 129);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) -1;
        try {
            ISOUtil.byte2BitSet(b, 1, 127);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 9;
        try {
            ISOUtil.byte2BitSet(b, 0, 63);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 3;
        try {
            ISOUtil.byte2BitSet(b, 0, 129);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException6() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) -61;
        try {
            ISOUtil.byte2BitSet(b, 0, 64);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException7() throws Throwable {
        byte[] b = new byte[2];
        try {
            ISOUtil.byte2BitSet(b, 0, 128);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException8() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, 63);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException9() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, 129);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsIndexOutOfBoundsException() throws Throwable {
        BitSet bmap = new BitSet(100);
        byte[] b = new byte[0];
        ISOUtil.byte2BitSet(bmap, b, 100);
        byte[] b2 = new byte[4];
        b2[0] = (byte) 1;
        try {
            ISOUtil.byte2BitSet(bmap, b2, -30);
            fail("Expected IndexOutOfBoundsException to be thrown");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "bitIndex < 0: -22", ex.getMessage());
            assertEquals("bmap.size()", 128, bmap.size());
        }
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.byte2BitSet((byte[]) null, 100, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException1() throws Throwable {
        byte[] b = new byte[4];
        b[2] = (byte) 127;
        try {
            ISOUtil.byte2BitSet((BitSet) null, b, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException2() throws Throwable {
        BitSet bmap = new BitSet(100);
        try {
            ISOUtil.byte2BitSet(bmap, (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("bmap.size()", 128, bmap.size());
        }
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException3() throws Throwable {
        try {
            ISOUtil.byte2BitSet((byte[]) null, 100, 65);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConcat() throws Throwable {
        byte[] array1 = new byte[3];
        byte[] result = ISOUtil.concat(array1, 0, 1, ISOUtil.asciiToEbcdic("testISOUtils"), 10, 0);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testConcat1() throws Throwable {
        byte[] array2 = new byte[3];
        byte[] array1 = new byte[3];
        byte[] result = ISOUtil.concat(array1, 0, 0, array2, 1, 0);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testConcat2() throws Throwable {
        byte[] array1 = new byte[1];
        byte[] array2 = new byte[3];
        byte[] result = ISOUtil.concat(array1, array2);
        assertEquals("result.length", 4, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testConcat3() throws Throwable {
        byte[] array2 = new byte[0];
        byte[] array1 = new byte[0];
        byte[] result = ISOUtil.concat(array1, array2);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testConcatThrowsNegativeArraySizeException() throws Throwable {
        byte[] array1 = new byte[0];
        byte[] array2 = new byte[1];
        try {
            ISOUtil.concat(array1, 100, 0, array2, 1000, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConcatThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.concat((byte[]) null, 100, 1000, ISOUtil.asciiToEbcdic("testISOUtils"), 0, -1);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConcatThrowsNullPointerException1() throws Throwable {
        byte[] array2 = new byte[3];
        try {
            ISOUtil.concat((byte[]) null, array2);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new ISOUtil();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDumpString() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 22;
        b[2] = (byte) 7;
        b[3] = (byte) 29;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{SYN}{BEL}[1D]\uFFEF", result);
    }

    @Test
    public void testDumpString1() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 32;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK} {NULL}{DLE}{NULL}", result);
    }

    @Test
    public void testDumpString10() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 5;
        b[2] = (byte) -29;
        b[3] = (byte) -6;
        b[4] = (byte) 27;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}{ENQ}\uFFE3\uFFFA[1B]", result);
    }

    @Test
    public void testDumpString100() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 15;
        b[2] = (byte) -128;
        b[3] = (byte) -2;
        b[4] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}[0F]\uFF80\uFFFE{DLE}", result);
    }

    @Test
    public void testDumpString101() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 13;
        b[1] = (byte) 93;
        b[2] = (byte) 17;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{CR}][11]", result);
    }

    @Test
    public void testDumpString102() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 1;
        b[2] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}{NULL}[1D]{NULL}", result);
    }

    @Test
    public void testDumpString103() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[2] = (byte) -16;
        b[3] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{NULL}\uFFF0{FS}{NULL}", result);
    }

    @Test
    public void testDumpString104() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 5;
        b[1] = (byte) 32;
        b[2] = (byte) 127;
        b[3] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ} [7F]{NAK}{NULL}", result);
    }

    @Test
    public void testDumpString105() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 11;
        b[1] = (byte) -10;
        b[2] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "[0B]\uFFF6{NAK}", result);
    }

    @Test
    public void testDumpString106() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        b[1] = (byte) -17;
        b[2] = (byte) 20;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}\uFFEF[14]", result);
    }

    @Test
    public void testDumpString107() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 6;
        b[2] = (byte) 25;
        b[3] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{ACK}[19]\uFFE9", result);
    }

    @Test
    public void testDumpString108() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 10;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{LF}", result);
    }

    @Test
    public void testDumpString109() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 1;
        b[1] = (byte) -15;
        b[2] = (byte) -19;
        b[3] = (byte) -108;
        b[4] = (byte) -16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}\uFFF1\uFFED\uFF94\uFFF0", result);
    }

    @Test
    public void testDumpString11() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 24;
        b[1] = (byte) 6;
        b[3] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "[18]{ACK}{NULL}{SYN}", result);
    }

    @Test
    public void testDumpString110() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 16;
        b[1] = (byte) 9;
        b[2] = (byte) 93;
        b[3] = (byte) -3;
        b[4] = (byte) -1;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{DLE}[09]]\uFFFD\uFFFF", result);
    }

    @Test
    public void testDumpString111() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[1] = (byte) -13;
        b[2] = (byte) 26;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{BEL}\uFFF3[1A]", result);
    }

    @Test
    public void testDumpString112() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 14;
        b[2] = (byte) 94;
        b[3] = (byte) -5;
        b[4] = (byte) -28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}[0E]^\uFFFB\uFFE4", result);
    }

    @Test
    public void testDumpString113() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) 30;
        b[2] = (byte) -91;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}{RS}\uFFA5{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString114() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString115() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 15;
        b[2] = (byte) -93;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}[0F]\uFFA3{NULL}{FS}", result);
    }

    @Test
    public void testDumpString116() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -26;
        b[2] = (byte) -7;
        b[3] = (byte) -27;
        b[4] = (byte) 27;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{RS}\uFFE6\uFFF9\uFFE5[1B]", result);
    }

    @Test
    public void testDumpString117() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 1;
        b[1] = (byte) 96;
        b[2] = (byte) 29;
        b[3] = (byte) 12;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}`[1D][0C]", result);
    }

    @Test
    public void testDumpString118() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 5;
        b[1] = (byte) 32;
        b[3] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ} {NULL}{NAK}{NULL}", result);
    }

    @Test
    public void testDumpString119() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 16;
        b[1] = (byte) 9;
        b[2] = (byte) 93;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{DLE}[09]]{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString12() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[1] = (byte) 1;
        b[2] = (byte) 13;
        b[3] = (byte) -23;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}{SOH}{CR}\uFFE9[17]", result);
    }

    @Test
    public void testDumpString120() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 24;
        b[1] = (byte) 6;
        b[2] = (byte) 23;
        b[3] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "[18]{ACK}[17]{SYN}", result);
    }

    @Test
    public void testDumpString121() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 1;
        b[1] = (byte) 15;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}[0F]", result);
    }

    @Test
    public void testDumpString122() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 30;
        b[2] = (byte) 2;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{RS}{STX}\uFFFD", result);
    }

    @Test
    public void testDumpString123() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{BEL}", result);
    }

    @Test
    public void testDumpString124() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[1] = (byte) 1;
        b[2] = (byte) 13;
        b[3] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}{SOH}{CR}\uFFE9{NULL}", result);
    }

    @Test
    public void testDumpString125() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 22;
        b[2] = (byte) 7;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}{SYN}{BEL}{NULL}\uFFEF", result);
    }

    @Test
    public void testDumpString126() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[1] = (byte) 8;
        b[2] = (byte) -8;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ}[08]\uFFF8{DLE}", result);
    }

    @Test
    public void testDumpString127() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 1;
        b[3] = (byte) -9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{SOH}{NULL}\uFFF7", result);
    }

    @Test
    public void testDumpString128() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 21;
        b[1] = (byte) -18;
        b[2] = (byte) 91;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}\uFFEE[", result);
    }

    @Test
    public void testDumpString129() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[1] = (byte) -24;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{BEL}\uFFE8{NULL}", result);
    }

    @Test
    public void testDumpString13() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[1] = (byte) -8;
        b[3] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}\uFFF8{NULL}[1F]", result);
    }

    @Test
    public void testDumpString130() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[2] = (byte) 119;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ}{NULL}w{NULL}", result);
    }

    @Test
    public void testDumpString131() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{ENQ}", result);
    }

    @Test
    public void testDumpString132() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -6;
        b[4] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{LF}\uFFFA{NULL}{NULL}{SYN}", result);
    }

    @Test
    public void testDumpString133() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -6;
        b[2] = (byte) -29;
        b[3] = (byte) 11;
        b[4] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{LF}\uFFFA\uFFE3[0B]{SYN}", result);
    }

    @Test
    public void testDumpString134() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 30;
        b[2] = (byte) 2;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{RS}{STX}\uFFFD", result);
    }

    @Test
    public void testDumpString135() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 11;
        b[2] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "[0B]{NULL}{NAK}", result);
    }

    @Test
    public void testDumpString136() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[1] = (byte) 8;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ}[08]{NULL}{DLE}", result);
    }

    @Test
    public void testDumpString137() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 21;
        b[2] = (byte) -16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{NAK}\uFFF0{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString138() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        b[1] = (byte) -4;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}\uFFFC{NULL}", result);
    }

    @Test
    public void testDumpString139() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[1] = (byte) 23;
        b[2] = (byte) 119;
        b[3] = (byte) -105;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ}[17]w\uFF97", result);
    }

    @Test
    public void testDumpString14() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -12;
        b[2] = (byte) 31;
        b[3] = (byte) 15;
        b[4] = (byte) 26;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{LF}\uFFF4[1F][0F][1A]", result);
    }

    @Test
    public void testDumpString140() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 1;
        b[1] = (byte) 96;
        b[2] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}`[1D]{NULL}", result);
    }

    @Test
    public void testDumpString141() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 15;
        b[2] = (byte) -128;
        b[4] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}[0F]\uFF80{NULL}{DLE}", result);
    }

    @Test
    public void testDumpString142() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}{NULL}", result);
    }

    @Test
    public void testDumpString143() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 22;
        b[3] = (byte) 29;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}{SYN}{NULL}[1D]\uFFEF", result);
    }

    @Test
    public void testDumpString144() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 13;
        b[2] = (byte) 17;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{CR}{NULL}[11]", result);
    }

    @Test
    public void testDumpString145() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 32;
        b[2] = (byte) 127;
        b[3] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL} [7F]{NAK}{NULL}", result);
    }

    @Test
    public void testDumpString146() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[1] = (byte) 1;
        b[2] = (byte) 13;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}{SOH}{CR}{NULL}[17]", result);
    }

    @Test
    public void testDumpString147() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 22;
        b[2] = (byte) -26;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{NULL}\uFFE6{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString148() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -29;
        b[4] = (byte) 9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{RS}\uFFE3{NULL}{NULL}[09]", result);
    }

    @Test
    public void testDumpString149() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{NULL}", result);
    }

    @Test
    public void testDumpString15() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 23;
        b[2] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}[17]{ENQ}{NULL}", result);
    }

    @Test
    public void testDumpString150() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[2] = (byte) -91;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}{NULL}\uFFA5{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString151() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) -20;
        b[4] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}\uFFEC{NULL}{NULL}{EOT}", result);
    }

    @Test
    public void testDumpString16() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 1;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}{NULL}", result);
    }

    @Test
    public void testDumpString17() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 5;
        b[2] = (byte) -29;
        b[4] = (byte) 27;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}{ENQ}\uFFE3{NULL}[1B]", result);
    }

    @Test
    public void testDumpString18() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}[17]{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString19() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[2] = (byte) -8;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ}{NULL}\uFFF8{DLE}", result);
    }

    @Test
    public void testDumpString2() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 4;
        b[1] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}{ENQ}", result);
    }

    @Test
    public void testDumpString20() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 1;
        b[2] = (byte) 9;
        b[3] = (byte) -9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{SOH}[09]\uFFF7", result);
    }

    @Test
    public void testDumpString21() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -26;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{RS}\uFFE6{NULL}{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString22() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 8;
        b[2] = (byte) -31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}[08]\uFFE1{NULL}", result);
    }

    @Test
    public void testDumpString23() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) -21;
        b[2] = (byte) -6;
        b[3] = (byte) 7;
        b[4] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}\uFFEB\uFFFA{BEL}[1F]", result);
    }

    @Test
    public void testDumpString24() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 21;
        b[2] = (byte) -16;
        b[3] = (byte) 28;
        b[4] = (byte) 2;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{NAK}\uFFF0{FS}{STX}", result);
    }

    @Test
    public void testDumpString25() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 2;
        b[2] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{NULL}\uFFE9", result);
    }

    @Test
    public void testDumpString26() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -12;
        b[2] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{LF}\uFFF4[1F]{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString27() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[2] = (byte) 2;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{NULL}{STX}\uFFFD", result);
    }

    @Test
    public void testDumpString28() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 18;
        b[2] = (byte) -26;
        b[3] = (byte) 11;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}[12]\uFFE6[0B]", result);
    }

    @Test
    public void testDumpString29() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[2] = (byte) -93;
        b[3] = (byte) 4;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{NULL}\uFFA3{EOT}{FS}", result);
    }

    @Test
    public void testDumpString3() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 30;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{RS}", result);
    }

    @Test
    public void testDumpString30() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[1] = (byte) -8;
        b[2] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}\uFFF8{DLE}{NULL}", result);
    }

    @Test
    public void testDumpString31() throws Throwable {
        byte[] b = new byte[5];
        b[2] = (byte) 13;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{NULL}{CR}{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString32() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 49;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "1", result);
    }

    @Test
    public void testDumpString33() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 16;
        b[1] = (byte) -7;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{DLE}\uFFF9", result);
    }

    @Test
    public void testDumpString34() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[1] = (byte) -8;
        b[2] = (byte) 16;
        b[3] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}\uFFF8{DLE}[1F]", result);
    }

    @Test
    public void testDumpString35() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 26;
        b[1] = (byte) 8;
        b[2] = (byte) -24;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "[1A][08]\uFFE8", result);
    }

    @Test
    public void testDumpString36() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 16;
        b[2] = (byte) 93;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{DLE}{NULL}]{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString37() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 21;
        b[1] = (byte) -18;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}\uFFEE{NULL}", result);
    }

    @Test
    public void testDumpString38() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 21;
        b[2] = (byte) -16;
        b[3] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{NAK}\uFFF0{FS}{NULL}", result);
    }

    @Test
    public void testDumpString39() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 15;
        b[2] = (byte) -93;
        b[3] = (byte) 4;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}[0F]\uFFA3{EOT}{FS}", result);
    }

    @Test
    public void testDumpString4() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) -12;
        b[2] = (byte) 27;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}\uFFF4[1B]{NULL}", result);
    }

    @Test
    public void testDumpString40() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        b[2] = (byte) 20;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}{NULL}[14]", result);
    }

    @Test
    public void testDumpString41() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 6;
        b[1] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}{BEL}", result);
    }

    @Test
    public void testDumpString42() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 24;
        b[3] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "[18]{NULL}{NULL}{SYN}", result);
    }

    @Test
    public void testDumpString43() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 5;
        b[2] = (byte) -29;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}{ENQ}\uFFE3{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString44() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) -1;
        b[2] = (byte) 13;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "\uFFFF{NULL}{CR}{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString45() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 4;
        b[1] = (byte) -22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}\uFFEA", result);
    }

    @Test
    public void testDumpString46() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[1] = (byte) 1;
        b[3] = (byte) -23;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}{SOH}{NULL}\uFFE9[17]", result);
    }

    @Test
    public void testDumpString47() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 15;
        b[2] = (byte) -93;
        b[3] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}[0F]\uFFA3{EOT}{NULL}", result);
    }

    @Test
    public void testDumpString48() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 6;
        b[1] = (byte) 8;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}[08]", result);
    }

    @Test
    public void testDumpString49() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 2;
        b[1] = (byte) 16;
        b[2] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{DLE}\uFFE9", result);
    }

    @Test
    public void testDumpString5() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -6;
        b[3] = (byte) 11;
        b[4] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{LF}\uFFFA{NULL}[0B]{SYN}", result);
    }

    @Test
    public void testDumpString50() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 24;
        b[1] = (byte) 18;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "[18][12]", result);
    }

    @Test
    public void testDumpString51() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "", result);
    }

    @Test
    public void testDumpString52() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) -21;
        b[3] = (byte) 7;
        b[4] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}\uFFEB{NULL}{BEL}[1F]", result);
    }

    @Test
    public void testDumpString53() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[2] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{LF}{NULL}[1F]{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString54() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 5;
        b[1] = (byte) 32;
        b[2] = (byte) 127;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ} [7F]{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString55() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 32;
        b[2] = (byte) -93;
        b[3] = (byte) 16;
        b[4] = (byte) -28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK} \uFFA3{DLE}\uFFE4", result);
    }

    @Test
    public void testDumpString56() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 1;
        b[2] = (byte) 13;
        b[3] = (byte) -23;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{SOH}{CR}\uFFE9[17]", result);
    }

    @Test
    public void testDumpString57() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[2] = (byte) 26;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{BEL}{NULL}[1A]", result);
    }

    @Test
    public void testDumpString58() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 1;
        b[2] = (byte) 9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{SOH}[09]{NULL}", result);
    }

    @Test
    public void testDumpString59() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) -12;
        b[2] = (byte) 27;
        b[3] = (byte) 12;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}\uFFF4[1B][0C]", result);
    }

    @Test
    public void testDumpString6() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) 23;
        b[2] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}[17]{ENQ}{NULL}", result);
    }

    @Test
    public void testDumpString60() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 5;
        b[1] = (byte) 32;
        b[2] = (byte) 127;
        b[3] = (byte) 21;
        b[4] = (byte) -9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ENQ} [7F]{NAK}\uFFF7", result);
    }

    @Test
    public void testDumpString61() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) -20;
        b[2] = (byte) 13;
        b[4] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}\uFFEC{CR}{NULL}{EOT}", result);
    }

    @Test
    public void testDumpString62() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) 23;
        b[2] = (byte) 5;
        b[3] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}[17]{ENQ}[1D]", result);
    }

    @Test
    public void testDumpString63() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 1;
        b[1] = (byte) -15;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}\uFFF1{NULL}{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString64() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) -20;
        b[2] = (byte) 13;
        b[3] = (byte) -24;
        b[4] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}\uFFEC{CR}\uFFE8{EOT}", result);
    }

    @Test
    public void testDumpString65() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) -18;
        b[2] = (byte) 30;
        b[3] = (byte) -27;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}\uFFEE{RS}\uFFE5", result);
    }

    @Test
    public void testDumpString66() throws Throwable {
        byte[] b = new byte[3];
        b[2] = (byte) 6;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{NULL}{ACK}", result);
    }

    @Test
    public void testDumpString67() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 2;
        b[1] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{DLE}{NULL}", result);
    }

    @Test
    public void testDumpString68() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) -21;
        b[4] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}\uFFEB{NULL}{NULL}[1F]", result);
    }

    @Test
    public void testDumpString69() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) -15;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}\uFFF1", result);
    }

    @Test
    public void testDumpString7() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) -21;
        b[3] = (byte) 7;
        b[4] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}\uFFEB{NULL}{BEL}[1F]", result);
    }

    @Test
    public void testDumpString70() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 22;
        b[1] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}[1D]", result);
    }

    @Test
    public void testDumpString71() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 15;
        b[3] = (byte) 4;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}[0F]{NULL}{EOT}{FS}", result);
    }

    @Test
    public void testDumpString72() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -29;
        b[2] = (byte) -8;
        b[3] = (byte) 7;
        b[4] = (byte) 9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{RS}\uFFE3\uFFF8{BEL}[09]", result);
    }

    @Test
    public void testDumpString73() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 3;
        b[1] = (byte) 92;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}\\", result);
    }

    @Test
    public void testDumpString74() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[2] = (byte) -128;
        b[4] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}{NULL}\uFF80{NULL}{DLE}", result);
    }

    @Test
    public void testDumpString75() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) -19;
        b[1] = (byte) 30;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "\uFFED{RS}", result);
    }

    @Test
    public void testDumpString76() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 6;
        b[1] = (byte) -27;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}\uFFE5", result);
    }

    @Test
    public void testDumpString77() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 28;
        b[2] = (byte) 3;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{FS}{ETX}", result);
    }

    @Test
    public void testDumpString78() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) -20;
        b[2] = (byte) 13;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}\uFFEC{CR}{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString79() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[2] = (byte) 13;
        b[3] = (byte) -23;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{EOT}{NULL}{CR}\uFFE9[17]", result);
    }

    @Test
    public void testDumpString8() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 15;
        b[2] = (byte) -93;
        b[3] = (byte) 4;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}[0F]\uFFA3{EOT}{FS}", result);
    }

    @Test
    public void testDumpString80() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 30;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{RS}{NULL}\uFFFD", result);
    }

    @Test
    public void testDumpString81() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 21;
        b[3] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}{NAK}{NULL}{FS}{NULL}", result);
    }

    @Test
    public void testDumpString82() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 15;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}[0F]", result);
    }

    @Test
    public void testDumpString83() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) -20;
        b[2] = (byte) 13;
        b[4] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}\uFFEC{CR}{NULL}{EOT}", result);
    }

    @Test
    public void testDumpString84() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 3;
        b[2] = (byte) 6;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}{NULL}{ACK}", result);
    }

    @Test
    public void testDumpString85() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[2] = (byte) 9;
        b[3] = (byte) -9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{NULL}[09]\uFFF7", result);
    }

    @Test
    public void testDumpString86() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) 30;
        b[2] = (byte) -91;
        b[3] = (byte) 94;
        b[4] = (byte) -30;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}{RS}\uFFA5^\uFFE2", result);
    }

    @Test
    public void testDumpString87() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[1] = (byte) -24;
        b[2] = (byte) 90;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{BEL}\uFFE8Z", result);
    }

    @Test
    public void testDumpString88() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) -21;
        b[3] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{STX}\uFFEB{NULL}{BEL}{NULL}", result);
    }

    @Test
    public void testDumpString89() throws Throwable {
        byte[] b = new byte[1];
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}", result);
    }

    @Test
    public void testDumpString9() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[2] = (byte) 94;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ACK}{NULL}^{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString90() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 1;
        b[1] = (byte) 10;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SOH}{LF}", result);
    }

    @Test
    public void testDumpString91() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 30;
        b[2] = (byte) 2;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{SYN}{RS}{STX}{NULL}", result);
    }

    @Test
    public void testDumpString92() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 22;
        b[2] = (byte) 7;
        b[3] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}{SYN}{BEL}[1D]{NULL}", result);
    }

    @Test
    public void testDumpString93() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 22;
        b[2] = (byte) 7;
        b[3] = (byte) 29;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}{SYN}{BEL}[1D]\uFFEF", result);
    }

    @Test
    public void testDumpString94() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 3;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{ETX}{NULL}{NULL}", result);
    }

    @Test
    public void testDumpString95() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 6;
        b[2] = (byte) 25;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NULL}{ACK}[19]{NULL}", result);
    }

    @Test
    public void testDumpString96() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[2] = (byte) 7;
        b[3] = (byte) 29;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{NAK}{NULL}{BEL}[1D]\uFFEF", result);
    }

    @Test
    public void testDumpString97() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -29;
        b[3] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{RS}\uFFE3{NULL}{BEL}{NULL}", result);
    }

    @Test
    public void testDumpString98() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[2] = (byte) 16;
        b[3] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{FS}{NULL}{DLE}[1F]", result);
    }

    @Test
    public void testDumpString99() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -29;
        b[3] = (byte) 7;
        b[4] = (byte) 9;
        String result = ISOUtil.dumpString(b);
        assertEquals("result", "{RS}\uFFE3{NULL}{BEL}[09]", result);
    }

    @Test
    public void testDumpStringThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.dumpString((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAscii() throws Throwable {
        byte[] e = new byte[2];
        String result = ISOUtil.ebcdicToAscii(e);
        assertEquals("result", "\u0000\u0000", result);
    }

    @Test
    public void testEbcdicToAscii1() throws Throwable {
        byte[] e = new byte[3];
        String result = ISOUtil.ebcdicToAscii(e, 0, 1);
        assertEquals("result", "\u0000", result);
    }

    @Test
    public void testEbcdicToAsciiBytes() throws Throwable {
        byte[] e = new byte[2];
        byte[] result = ISOUtil.ebcdicToAsciiBytes(e, 0, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testEbcdicToAsciiBytes1() throws Throwable {
        byte[] e = new byte[3];
        byte[] result = ISOUtil.ebcdicToAsciiBytes(e, 100, 0);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testEbcdicToAsciiBytes2() throws Throwable {
        byte[] e = new byte[0];
        byte[] result = ISOUtil.ebcdicToAsciiBytes(e);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testEbcdicToAsciiBytes3() throws Throwable {
        byte[] e = new byte[2];
        byte[] result = ISOUtil.ebcdicToAsciiBytes(e);
        assertEquals("result.length", 2, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] e = new byte[1];
        try {
            ISOUtil.ebcdicToAsciiBytes(e, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] e = new byte[1];
        try {
            ISOUtil.ebcdicToAsciiBytes(e, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsNegativeArraySizeException() throws Throwable {
        byte[] e = new byte[1];
        try {
            ISOUtil.ebcdicToAsciiBytes(e, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.ebcdicToAsciiBytes((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.ebcdicToAsciiBytes((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAsciiThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] e = new byte[1];
        try {
            ISOUtil.ebcdicToAscii(e, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAsciiThrowsNegativeArraySizeException() throws Throwable {
        byte[] e = new byte[1];
        try {
            ISOUtil.ebcdicToAscii(e, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEbcdicToAsciiThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.ebcdicToAscii((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFormatAmount() throws Throwable {
        String result = ISOUtil.formatAmount(100000L, 7);
        assertEquals("result", "1000.00", result);
    }

    @Test
    public void testFormatAmount1() throws Throwable {
        String result = ISOUtil.formatAmount(1000L, 100);
        assertEquals("result",
                "                                                                                               10.00", result);
    }

    @Test
    public void testFormatAmount2() throws Throwable {
        String result = ISOUtil.formatAmount(100L, 100);
        assertEquals("result",
                "                                                                                                1.00", result);
    }

    @Test
    public void testFormatAmount3() throws Throwable {
        String result = ISOUtil.formatAmount(99L, 100);
        assertEquals("result",
                "                                                                                                0.99", result);
    }

    @Test
    public void testFormatAmount4() throws Throwable {
        String result = ISOUtil.formatAmount(101L, 4);
        assertEquals("result", "1.01", result);
    }

    @Test
    public void testFormatAmountThrowsISOException() throws Throwable {
        try {
            ISOUtil.formatAmount(99L, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len 3/-1", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testFormatAmountThrowsISOException1() throws Throwable {
        try {
            ISOUtil.formatAmount(100L, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len 3/-1", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testFormatAmountThrowsISOException2() throws Throwable {
        try {
            ISOUtil.formatAmount(Long.MIN_VALUE, 100);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len 20/3", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testFormatDouble() throws Throwable {
        String result = ISOUtil.formatDouble(100.0, 100);
        assertEquals("result",
                "                                                                                              100.00", result);
    }

    @Test
    public void testFormatDouble1() throws Throwable {
        String result = ISOUtil.formatDouble(100.0, 0);
        assertEquals("result", "100.00", result);
    }

    @Test
    public void testHex2BitSet() throws Throwable {
        byte[] b = new byte[82];
        BitSet result = ISOUtil.hex2BitSet(b, 0, false);
        assertEquals("result.size()", 128, result.size());
    }

    @Test
    public void testHex2BitSet1() throws Throwable {
        byte[] b = new byte[82];
        BitSet result = ISOUtil.hex2BitSet(b, 0, true);
        assertEquals("result.size()", 256, result.size());
    }

    @Test
    public void testHex2BitSet10() throws Throwable {
        byte[] b = new byte[80];
        BitSet result = ISOUtil.hex2BitSet(b, 0, 1000);
        assertEquals("result.size()", 256, result.size());
    }

    @Test
    public void testHex2BitSet3() throws Throwable {
        byte[] b = new byte[0];
        BitSet result = ISOUtil.hex2BitSet((BitSet) null, b, 100);
        assertNull("result", result);
    }

    @Test
    public void testHex2BitSet4() throws Throwable {
        byte[] b = new byte[20];
        b[11] = (byte) 65;
        BitSet result = ISOUtil.hex2BitSet(b, 0, false);
        assertEquals("result.size()", 128, result.size());
    }

    @Test
    public void testHex2BitSet7() throws Throwable {
        byte[] b = new byte[46];
        BitSet result = ISOUtil.hex2BitSet(b, 0, 64);
        assertEquals("result.size()", 128, result.size());
    }

    @Test
    public void testHex2BitSet9() throws Throwable {
        byte[] b = new byte[80];
        BitSet result = ISOUtil.hex2BitSet(b, 0, 100);
        assertEquals("result.size()", 256, result.size());
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[19];
        b[14] = (byte) 65;
        try {
            ISOUtil.hex2BitSet(b, 0, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "19", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 48;
        try {
            ISOUtil.hex2BitSet(b, 0, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException10() throws Throwable {
        byte[] b = new byte[19];
        b[4] = (byte) 65;
        try {
            ISOUtil.hex2BitSet(b, 0, 65);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "19", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException11() throws Throwable {
        byte[] b = new byte[83];
        b[68] = (byte) 65;
        try {
            ISOUtil.hex2BitSet(b, 66, 127);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "83", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException12() throws Throwable {
        byte[] b = new byte[19];
        try {
            ISOUtil.hex2BitSet(b, 0, 65);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "19", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException13() throws Throwable {
        byte[] b = new byte[20];
        b[16] = (byte) 66;
        try {
            ISOUtil.hex2BitSet(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "20", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[18];
        try {
            ISOUtil.hex2BitSet(b, 0, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "18", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 66;
        try {
            ISOUtil.hex2BitSet(b, 0, 64);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2BitSet(b, 0, 65);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2BitSet(b, 0, 63);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException6() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2BitSet(b, 0, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException7() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.hex2BitSet(b, 0, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException8() throws Throwable {
        byte[] b = new byte[2];
        try {
            ISOUtil.hex2BitSet(b, 100, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException9() throws Throwable {
        byte[] b = new byte[0];
        try {
            ISOUtil.hex2BitSet(b, 100, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsIndexOutOfBoundsException() throws Throwable {
        BitSet bmap = new BitSet(100);
        byte[] b = new byte[2];
        ISOUtil.byte2BitSet(bmap, b, 100);
        byte[] b2 = new byte[4];
        try {
            ISOUtil.hex2BitSet(bmap, b2, -29);
            fail("Expected IndexOutOfBoundsException to be thrown");
        } catch (IndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "bitIndex < 0: -28", ex.getMessage());
            assertEquals("bmap.size()", 128, bmap.size());
        }
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.hex2BitSet((byte[]) null, 100, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.hex2BitSet((byte[]) null, 100, 63);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException2() throws Throwable {
        try {
            ISOUtil.hex2BitSet((byte[]) null, 100, 65);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException3() throws Throwable {
        byte[] b = new byte[2];
        try {
            ISOUtil.hex2BitSet((BitSet) null, b, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException4() throws Throwable {
        BitSet bmap = new BitSet();
        try {
            ISOUtil.hex2BitSet(bmap, (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("bmap.size()", 64, bmap.size());
        }
    }

    @Test
    public void testHex2byte() throws Throwable {
        byte[] b = new byte[3];
        byte[] result = ISOUtil.hex2byte(b, 0, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) -1, result[0]);
    }

    @Test
    public void testHex2byte1() throws Throwable {
        byte[] b = new byte[3];
        byte[] result = ISOUtil.hex2byte(b, 100, 0);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testHex2byte2() throws Throwable {
        byte[] result = ISOUtil.hex2byte("");
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testHex2byte3() throws Throwable {
        byte[] result = ISOUtil.hex2byte("testISOUtils");
        assertEquals("result.length", 6, result.length);
        assertEquals("result[0]", (byte) -2, result[0]);
    }

    @Test
    public void testHex2byteThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2byte(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHex2byteThrowsNegativeArraySizeException() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.hex2byte(b, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHex2byteThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.hex2byte((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHex2byteThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.hex2byte(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHex2byteThrowsRuntimeException() throws Throwable {
        byte[] result = ISOUtil.hex2byte("testISOUtils1");
        assertNotNull(result);
    }

    @Test
    public void testHexdump() throws Throwable {
        byte[] b = new byte[34];
        b[16] = (byte) 127;
        String result = ISOUtil.hexdump(b, 0, 17);
        assertEquals("result", "0000  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep
                + "0010  7F                                                ." + lineSep, result);
    }

    @Test
    public void testHexdump1() throws Throwable {
        byte[] b = new byte[34];
        b[17] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 32);
        assertEquals("result", "0000  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep
                + "0010  00 1F 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep, result);
    }

    @Test
    public void testHexdump10() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 32;
        String result = ISOUtil.hexdump(b);
        assertEquals("result", "0000  00 20                                             . " + lineSep, result);
    }

    @Test
    public void testHexdump11() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 127;
        String result = ISOUtil.hexdump(b);
        assertEquals("result", "0000  00 7F 00                                          ..." + lineSep, result);
    }

    @Test
    public void testHexdump12() throws Throwable {
        byte[] b = new byte[12];
        b[8] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("result", "0000  00 00 00 00 00 00 00 00  20 00                    ........ ." + lineSep, result);
    }

    @Test
    public void testHexdump13() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.hexdump(b);
        assertEquals("result", "", result);
    }

    @Test
    public void testHexdump14() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 126;
        String result = ISOUtil.hexdump(b);
        assertEquals("result", "0000  7E 00                                             ~." + lineSep, result);
    }

    @Test
    public void testHexdump15() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 33;
        String result = ISOUtil.hexdump(b);
        assertEquals("result", "0000  21                                                !" + lineSep, result);
    }

    @Test
    public void testHexdump16() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 31;
        String result = ISOUtil.hexdump(b);
        assertEquals("result", "0000  1F 00                                             .." + lineSep, result);
    }

    @Test
    public void testHexdump17() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 3);
        assertEquals("result", "0000  00 1F 00                                          ..." + lineSep, result);
    }

    @Test
    public void testHexdump18() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        b[5] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 17);
        assertEquals("result", "0000  7F 20 00 00 00 1F 00 00  00 00 00 00 00 00 00 00  . .............." + lineSep
                + "0010  00                                                ." + lineSep, result);
    }

    @Test
    public void testHexdump19() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 2);
        assertEquals("result", "0000  7F 20                                             . " + lineSep, result);
    }

    @Test
    public void testHexdump2() throws Throwable {
        byte[] b = new byte[19];
        b[1] = (byte) 32;
        b[5] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 17);
        assertEquals("result", "0000  00 20 00 00 00 1F 00 00  00 00 00 00 00 00 00 00  . .............." + lineSep
                + "0010  00                                                ." + lineSep, result);
    }

    @Test
    public void testHexdump20() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 1);
        assertEquals("result", "0000  20                                                 " + lineSep, result);
    }

    @Test
    public void testHexdump21() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 3);
        assertEquals("result", "0000  7F 20 00                                          . ." + lineSep, result);
    }

    @Test
    public void testHexdump22() throws Throwable {
        byte[] b = new byte[18];
        b[14] = (byte) 46;
        String result = ISOUtil.hexdump(b, 10, 15);
        assertEquals("result", "0000  00 00 00 00 2E                                    ....." + lineSep, result);
    }

    @Test
    public void testHexdump23() throws Throwable {
        byte[] b = new byte[12];
        b[6] = (byte) -48;
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("result", "0000  00 00 00 00 00 00 D0 00  00 00                    .........." + lineSep, result);
    }

    @Test
    public void testHexdump3() throws Throwable {
        byte[] b = new byte[2];
        String result = ISOUtil.hexdump(b, 100, 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testHexdump4() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) -4;
        String result = ISOUtil.hexdump(b, 1, 2);
        assertEquals("result", "0000  FC                                                ." + lineSep, result);
    }

    @Test
    public void testHexdump5() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        b[5] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("result", "0000  7F 20 00 00 00 1F 00 00  00 00                    . ........" + lineSep, result);
    }

    @Test
    public void testHexdump6() throws Throwable {
        byte[] b = new byte[34];
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("result", "0000  00 00 00 00 00 00 00 00  00 00                    .........." + lineSep, result);
    }

    @Test
    public void testHexdump7() throws Throwable {
        byte[] b = new byte[10];
        b[7] = (byte) -2;
        String result = ISOUtil.hexdump(b, 7, 8);
        assertEquals("result", "0000  FE                                                ." + lineSep, result);
    }

    @Test
    public void testHexdump8() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 31;
        b[2] = (byte) -48;
        String result = ISOUtil.hexdump(b, 0, 3);
        assertEquals("result", "0000  00 1F D0                                          ..." + lineSep, result);
    }

    @Test
    public void testHexdump9() throws Throwable {
        byte[] b = new byte[34];
        b[16] = (byte) 127;
        String result = ISOUtil.hexdump(b, 0, 32);
        assertEquals("result", "0000  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep
                + "0010  7F 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep, result);
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        b[5] = (byte) 31;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "19", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[34];
        b[16] = (byte) 127;
        b[17] = (byte) 31;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "34", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException10() throws Throwable {
        byte[] b = new byte[2];
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException11() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) -49;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException12() throws Throwable {
        byte[] b = new byte[10];
        b[7] = (byte) -2;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "10", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 49;
        b[2] = (byte) -2;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] b = new byte[9];
        b[7] = (byte) 46;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "9", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 49;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] b = new byte[18];
        b[14] = (byte) 46;
        try {
            ISOUtil.hexdump(b, 10, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "18", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException6() throws Throwable {
        byte[] b = new byte[12];
        b[6] = (byte) -48;
        b[8] = (byte) 32;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "12", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException7() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 31;
        b[2] = (byte) -48;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException8() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 32;
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException9() throws Throwable {
        byte[] b = new byte[9];
        b[7] = (byte) 46;
        b[8] = (byte) -3;
        try {
            ISOUtil.hexdump(b, 7, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "9", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.hexdump((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHexdumpThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.hexdump((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHexor() throws Throwable {
        String result = ISOUtil.hexor("", "");
        assertEquals("result", "", result);
    }

    @Test
    public void testHexor1() throws Throwable {
        String result = ISOUtil.hexor("testISOUtilOp1", "testISOUtilOp2");
        assertEquals("result", "00000000000003", result);
    }

    @Test
    public void testHexString() throws Throwable {
        byte[] b = new byte[2];
        String result = ISOUtil.hexString(b, 0, 1);
        assertEquals("result", "00", result);
    }

    @Test
    public void testHexString1() throws Throwable {
        byte[] b = new byte[3];
        String result = ISOUtil.hexString(b, 100, 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testHexString2() throws Throwable {
        byte[] b = new byte[1];
        String result = ISOUtil.hexString(b);
        assertEquals("result", "00", result);
    }

    @Test
    public void testHexString3() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.hexString(b);
        assertEquals("result", "", result);
    }

    @Test
    public void testHexStringThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hexString(b, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testHexStringThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.hexString(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testHexStringThrowsNegativeArraySizeException() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.hexString(b, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHexStringThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.hexString((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testHexStringThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.hexString((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testIsAlphaNumeric() throws Throwable {
        boolean result = ISOUtil.isAlphaNumeric("testISOUtil\rs");
        assertFalse("result", result);
    }

    @Test
    public void testIsAlphaNumeric1() throws Throwable {
        boolean result = ISOUtil.isAlphaNumeric(")\r3\u001F,\u0011-\u0005\u000FU\u000E5M2)\u0017h\u0011&ln" + lineSep
                + "G4@\u0015\u000272A\u001D9&qW\u001An|YP");
        assertFalse("result", result);
    }

    @Test
    public void testIsAlphaNumericThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.isAlphaNumeric(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testIsAlphaNumericThrowsStringIndexOutOfBoundsException() throws Throwable {
        try {
            ISOUtil.isAlphaNumeric(" ");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 1", ex.getMessage());
        }
    }

    @Test
    public void testIsAlphaNumericThrowsStringIndexOutOfBoundsException1() throws Throwable {
        try {
            ISOUtil.isAlphaNumeric("");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 0", ex.getMessage());
        }
    }

    @Test
    public void testIsAlphaNumericThrowsStringIndexOutOfBoundsException2() throws Throwable {
        try {
            ISOUtil.isAlphaNumeric("testISOUtils");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 12", ex.getMessage());
        }
    }

    @Test
    public void testIsBlank() throws Throwable {
        boolean result = ISOUtil.isBlank("");
        assertTrue("result", result);
    }

    @Test
    public void testIsBlank1() throws Throwable {
        boolean result = ISOUtil.isBlank("1");
        assertFalse("result", result);
    }

    @Test
    public void testIsBlankThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.isBlank(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testIsNumeric() throws Throwable {
        boolean result = ISOUtil.isNumeric("10Characte", 32);
        assertTrue("result", result);
    }

    @Test
    public void testIsNumeric1() throws Throwable {
        boolean result = ISOUtil.isNumeric("testISOUtils", 100);
        assertFalse("result", result);
    }

    @Test
    public void testIsNumeric2() throws Throwable {
        boolean result = ISOUtil.isNumeric("testISOUtil\rs", 32);
        assertFalse("result", result);
    }

    @Test
    public void testIsNumeric3() throws Throwable {
        boolean result = ISOUtil.isNumeric("", 100);
        assertFalse("result", result);
    }

    @Test
    public void testIsNumericThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.isNumeric(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testIsZero() throws Throwable {
        boolean result = ISOUtil.isZero("0 q");
        assertFalse("result", result);
    }

    @Test
    public void testIsZero1() throws Throwable {
        boolean result = ISOUtil.isZero("000");
        assertTrue("result", result);
    }

    @Test
    public void testIsZero2() throws Throwable {
        boolean result = ISOUtil.isZero("testISOUtils");
        assertFalse("result", result);
    }

    @Test
    public void testIsZero3() throws Throwable {
        boolean result = ISOUtil.isZero("");
        assertTrue("result", result);
    }

    @Test
    public void testIsZeroThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.isZero(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testNormalize() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "S\u0002\"m\u0011Ap}q\\!\u0005\u0010$:\"X\u0008FM\u00028\u000E&CZ#\"\"]'\u0008k\"\u000B$jT>L\u0017-<3z\u0000%E<\u0011\"j\u001F\u0014\u001C\fR\u00138\r\">\u0016<T7l\u0002]oM|w\"$4*ks{~Cqqx7fW^^zi}gV;cY<A=ylA%DR-9",
                        true);
        assertEquals(
                "result",
                "S&#2;&quot;m&#17;Ap}q\\!&#5;&#16;$:&quot;X&#8;FM&#2;8&#14;&amp;CZ#&quot;&quot;]'&#8;k&quot;&#11;$jT&gt;L&#23;-&lt;3z&#0;%E&lt;&#17;&quot;j&#31;&#20;&#28;&#12;R&#19;8&#13;&quot;&gt;&#22;&lt;T7l&#2;]oM|w&quot;$4*ks{~Cqqx7fW^^zi}gV;cY&lt;A=ylA%DR-9",
                result);
    }

    @Test
    public void testNormalize1() throws Throwable {
        String result = ISOUtil.normalize("testISOUtil\rs", true);
        assertEquals("result", "testISOUtil&#13;s", result);
    }

    @Test
    public void testNormalize10() throws Throwable {
        String result = ISOUtil.normalize(null, true);
        assertEquals("result", "", result);
    }

    @Test
    public void testNormalize11() throws Throwable {
        String result = ISOUtil.normalize("", true);
        assertEquals("result", "", result);
    }

    @Test
    public void testNormalize12() throws Throwable {
        String result = ISOUtil.normalize("\u5BC7<t2e76HTO- 63;TwFix", true);
        assertEquals("result", "\u5BC7&lt;t2e76HTO- 63;TwFix", result);
    }

    @Test
    public void testNormalize13() throws Throwable {
        String result = ISOUtil.normalize("\u0014", true);
        assertEquals("result", "&#20;", result);
    }

    @Test
    public void testNormalize14() throws Throwable {
        String result = ISOUtil.normalize("\u7D79\u0001#h<Fuo|s)C<D&\\J:'{ul'p\\Nz@^dt`.", true);
        assertEquals("result", "\u7D79&#1;#h&lt;Fuo|s)C&lt;D&amp;\\J:'{ul'p\\Nz@^dt`.", result);
    }

    @Test
    public void testNormalize15() throws Throwable {
        String result = ISOUtil.normalize("&quot;", true);
        assertEquals("result", "&amp;quot;", result);
    }

    @Test
    public void testNormalize16() throws Throwable {
        String result = ISOUtil.normalize(" ");
        assertEquals("result", " ", result);
    }

    @Test
    public void testNormalize17() throws Throwable {
        String result = ISOUtil.normalize("testISOUtil\rs");
        assertEquals("result", "testISOUtil&#13;s", result);
    }

    @Test
    public void testNormalize18() throws Throwable {
        String result = ISOUtil.normalize("\rI\u0004\"e", true);
        assertEquals("result", "&#13;I&#4;&quot;e", result);
    }

    @Test
    public void testNormalize19() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "\u0014iSa4\r@\u0005/\u001Ai>iK>&%EA]\u001E\u001D:B\u000F-\u0016\u0012K=9\u001D\u0019t:&.2\"F<}Nf\u0011\u0003\u0008,\u0015=\u001D\n?\u0012\u000Eo<E\u001F\u0007\u001AC\u001C3\u0012d7:rf^l,`",
                        false);
        assertEquals(
                "result",
                "&#20;iSa4&#13;@&#5;/&#26;i&gt;iK&gt;&amp;%EA]&#30;&#29;:B&#15;-&#22;&#18;K=9&#29;&#25;t:&amp;.2&quot;F&lt;}Nf&#17;&#3;&#8;,&#21;=&#29;&#10;?&#18;&#14;o&lt;E&#31;&#7;&#26;C&#28;3&#18;d7:rf^l,`",
                result);
    }

    @Test
    public void testNormalize2() throws Throwable {
        String result = ISOUtil.normalize(" ", true);
        assertEquals("result", " ", result);
    }

    @Test
    public void testNormalize20() throws Throwable {
        String result = ISOUtil.normalize("\rX XX", false);
        assertEquals("result", "&#13;X XX", result);
    }

    @Test
    public void testNormalize21() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "\u0004r2\u0013&mX&\u0014\u0017hT\u000E}Y!{\u0004&\u0016\u000Fb\"D\u001B\u0014Qz\u001E-&fe<\u0012]<.5\\\u0001\u0000\u001D%v\u001FraS\"mMlc\u0014\u001F\u0008Q\"\u0019&\u000E\\\u0004\u000F\t\u000F&:\u4BF6",
                        true);
        assertEquals(
                "result",
                "&#4;r2&#19;&amp;mX&amp;&#20;&#23;hT&#14;}Y!{&#4;&amp;&#22;&#15;b&quot;D&#27;&#20;Qz&#30;-&amp;fe&lt;&#18;]&lt;.5\\&#1;&#0;&#29;%v&#31;raS&quot;mMlc&#20;&#31;&#8;Q&quot;&#25;&amp;&#14;\\&#4;&#15;&#9;&#15;&amp;:\u4BF6",
                result);
    }

    @Test
    public void testNormalize22() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "@|k7\u0014[\"7w\u0002'>\u0005\u001D[\u001D\fu0,\"9'K\u0007u\u0017[@\">V><uNo>\u001Eyj8>, pS5V&</sCZ \u0008dBz\"M\"%Wv)lQ)<u7>c]VKWRExFkiXlc1#'>?QU |d45\\eZR",
                        true);
        assertEquals(
                "result",
                "@|k7&#20;[&quot;7w&#2;'&gt;&#5;&#29;[&#29;&#12;u0,&quot;9'K&#7;u&#23;[@&quot;&gt;V&gt;&lt;uNo&gt;&#30;yj8&gt;, pS5V&amp;&lt;/sCZ &#8;dBz&quot;M&quot;%Wv)lQ)&lt;u7&gt;c]VKWRExFkiXlc1#'&gt;?QU |d45\\eZR",
                result);
    }

    @Test
    public void testNormalize23() throws Throwable {
        String result = ISOUtil.normalize(">&Y/G%za1ZHz$O^bPBeB nUlTf{n9,", true);
        assertEquals("result", "&gt;&amp;Y/G%za1ZHz$O^bPBeB nUlTf{n9,", result);
    }

    @Test
    public void testNormalize24() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "iC\u0012Vi<\t< A\\`>|&\rw\u0018l\u0000d\u000F_`>\\ N8\u0016%Up\rf\u0005\u0019G>%>1Wnx;Ul0Rz}%[wn\u000E\u001C*\t>DJ,<\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                        false);
        assertEquals(
                "result",
                "iC&#18;Vi&lt;&#9;&lt; A\\`&gt;|&amp;&#13;w&#24;l&#0;d&#15;_`&gt;\\ N8&#22;%Up&#13;f&#5;&#25;G&gt;%&gt;1Wnx;Ul0Rz}%[wn&#14;&#28;*&#9;&gt;DJ,&lt;\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                result);
    }

    @Test
    public void testNormalize3() throws Throwable {
        String result = ISOUtil.normalize("#g];unko,nsk 3<yhj>\\-)fw47&3,@p~rh[2cGi[", true);
        assertEquals("result", "#g];unko,nsk 3&lt;yhj&gt;\\-)fw47&amp;3,@p~rh[2cGi[", result);
    }

    @Test
    public void testNormalize4() throws Throwable {
        String result = ISOUtil.normalize(" XX XXXX  XX XXXX X  XXX  XX XXXXX XXXXX   XX XXXXXXXX   XX X X  \t XXX", true);
        assertEquals("result", " XX XXXX  XX XXXX X  XXX  XX XXXXX XXXXX   XX XXXXXXXX   XX X X  &#9; XXX", result);
    }

    @Test
    public void testNormalize5() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "iC\u0012Vi<\t< A\\`>|&\rw\u0018l\u0000d\u000F_`>\\ N8\u0016%Up\rf\u0005\u0019G>%>1Wnx;Ul0Rz}%[wn\u000E\u001C*\t>DJ,<\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                        true);
        assertEquals(
                "result",
                "iC&#18;Vi&lt;&#9;&lt; A\\`&gt;|&amp;&#13;w&#24;l&#0;d&#15;_`&gt;\\ N8&#22;%Up&#13;f&#5;&#25;G&gt;%&gt;1Wnx;Ul0Rz}%[wn&#14;&#28;*&#9;&gt;DJ,&lt;\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                result);
    }

    @Test
    public void testNormalize6() throws Throwable {
        String result = ISOUtil.normalize("\rI\u0004\"e", false);
        assertEquals("result", "&#13;I&#4;&quot;e", result);
    }

    @Test
    public void testNormalize7() throws Throwable {
        String result = ISOUtil.normalize("J\u0006YTuVP>F}R+Js:(aD", true);
        assertEquals("result", "J&#6;YTuVP&gt;F}R+Js:(aD", result);
    }

    @Test
    public void testNormalize8() throws Throwable {
        String result = ISOUtil.normalize(">=\u0011[\u0011_f\u0019<&[", true);
        assertEquals("result", "&gt;=&#17;[&#17;_f&#25;&lt;&amp;[", result);
    }

    @Test
    public void testNormalize9() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "<\u0010}\"\uCD88\uD16A\u1384\uFE1A\u44B2\u2712\u3BD7\u3AE5\uFCC4\uD19B\u16F2\uD45A\u45F8\u65FF\uA224\u3930\u946E\u8FB0\u3550\u061D\u4741\u8084\u8606\u6B1A\u8F81\uC634\u0190\u2053\uFA5A\u4C3F\uD365\uF7A7\uF8D4",
                        true);
        assertEquals(
                "result",
                "&lt;&#16;}&quot;\uCD88\uD16A\u1384\uFE1A\u44B2\u2712\u3BD7\u3AE5\uFCC4\uD19B\u16F2\uD45A\u45F8\u65FF\uA224\u3930\u946E\u8FB0\u3550\u061D\u4741\u8084\u8606\u6B1A\u8F81\uC634\u0190\u2053\uFA5A\u4C3F\uD365\uF7A7\uF8D4",
                result);
    }

    @Test
    public void testPadleft() throws Throwable {
        String result = ISOUtil.padleft("testString", 11, '2');
        assertEquals("result", "2testString", result);
    }

    @Test
    public void testPadleft1() throws Throwable {
        String result = ISOUtil.padleft("2C", 2, ' ');
        assertEquals("result", "2C", result);
    }

    @Test
    public void testPadleftThrowsISOException() throws Throwable {
        try {
            ISOUtil.padleft("testString", 0, '\u0002');
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len 10/0", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testPadleftThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.padleft(null, 0, '\u0002');
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseInt() throws Throwable {
        char[] cArray = new char[2];
        cArray[0] = 'S';
        cArray[1] = 'C';
        int result = ISOUtil.parseInt(cArray, 35);
        assertEquals("result", 992, result);
    }

    @Test
    public void testParseInt1() throws Throwable {
        char[] cArray = new char[1];
        cArray[0] = '1';
        int result = ISOUtil.parseInt(cArray);
        assertEquals("result", 1, result);
    }

    @Test
    public void testParseInt2() throws Throwable {
        int result = ISOUtil.parseInt("1", 10);
        assertEquals("result", 1, result);
    }

    @Test
    public void testParseInt3() throws Throwable {
        int result = ISOUtil.parseInt("2C", 31);
        assertEquals("result", 74, result);
    }

    @Test
    public void testParseInt4() throws Throwable {
        int result = ISOUtil.parseInt("1");
        assertEquals("result", 1, result);
    }

    @Test
    public void testParseInt5() throws Throwable {
        byte[] bArray = new byte[1];
        bArray[0] = (byte) 49;
        int result = ISOUtil.parseInt(bArray);
        assertEquals("result", 1, result);
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException() throws Throwable {
        char[] cArray = new char[0];
        try {
            ISOUtil.parseInt(cArray, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        char[] cArray = new char[0];
        try {
            ISOUtil.parseInt(cArray);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] bArray = new byte[0];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] bArray = new byte[0];
        try {
            ISOUtil.parseInt(bArray);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.parseInt((char[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.parseInt((char[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNullPointerException2() throws Throwable {
        try {
            ISOUtil.parseInt((String) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNullPointerException3() throws Throwable {
        try {
            ISOUtil.parseInt((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNullPointerException4() throws Throwable {
        try {
            ISOUtil.parseInt((byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNullPointerException5() throws Throwable {
        try {
            ISOUtil.parseInt((byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException() throws Throwable {
        char[] cArray = new char[9];
        cArray[0] = 'c';
        cArray[1] = '1';
        try {
            ISOUtil.parseInt(cArray, 35);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Char array contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException1() throws Throwable {
        char[] cArray = new char[8];
        cArray[0] = '1';
        try {
            ISOUtil.parseInt(cArray, 10);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Char array contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException10() throws Throwable {
        try {
            ISOUtil.parseInt("0\"/", 10);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "String contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException11() throws Throwable {
        try {
            ISOUtil.parseInt("9Characte", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "String contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException12() throws Throwable {
        try {
            ISOUtil.parseInt("8Charact", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "String contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException13() throws Throwable {
        try {
            ISOUtil.parseInt("10Characte", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Number can have maximum 9 digits", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException2() throws Throwable {
        byte[] bArray = new byte[8];
        bArray[0] = (byte) 55;
        try {
            ISOUtil.parseInt(bArray, 10);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Byte array contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException3() throws Throwable {
        byte[] bArray = new byte[8];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Byte array contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException4() throws Throwable {
        byte[] bArray = new byte[9];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Byte array contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException5() throws Throwable {
        byte[] bArray = new byte[10];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Number can have maximum 9 digits", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException6() throws Throwable {
        char[] cArray = new char[9];
        try {
            ISOUtil.parseInt(cArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Char array contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException7() throws Throwable {
        char[] cArray = new char[10];
        try {
            ISOUtil.parseInt(cArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "Number can have maximum 9 digits", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException8() throws Throwable {
        try {
            ISOUtil.parseInt("o`2L\\*@@#", 28);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "String contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException9() throws Throwable {
        try {
            ISOUtil.parseInt("9Characte", 28);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "String contains non-digit", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsStringIndexOutOfBoundsException() throws Throwable {
        try {
            ISOUtil.parseInt("", 100);
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 0", ex.getMessage());
        }
    }

    @Test
    public void testParseIntThrowsStringIndexOutOfBoundsException1() throws Throwable {
        try {
            ISOUtil.parseInt("");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 0", ex.getMessage());
        }
    }

    @Test
    public void testProtect() throws Throwable {
        String result = ISOUtil.protect("10Characte");
        assertEquals("result", "10Characte", result);
    }

    @Test
    public void testProtect1() throws Throwable {
        String result = ISOUtil.protect("=WaW=4V0");
        assertEquals("result", "=___=___", result);
    }

    @Test
    public void testProtect10() throws Throwable {
        String result = ISOUtil.protect("=====^===========^====^===");
        assertEquals("result", "=====^===========^====^===", result);
    }

    @Test
    public void testProtect11() throws Throwable {
        String result = ISOUtil.protect("testISOUtils");
        assertEquals("result", "testIS__tils", result);
    }

    @Test
    public void testProtect12() throws Throwable {
        String result = ISOUtil.protect("=HNb^D4uZfz0@|\")61b:~dSS`[.2!!qlL4Z0");
        assertEquals("result", "=___^D4uZfz0@|\")61b:~dSS`[.2!!qlL4Z0", result);
    }

    @Test
    public void testProtect13() throws Throwable {
        String result = ISOUtil.protect("^58*(=@");
        assertEquals("result", "^58*(=_", result);
    }

    @Test
    public void testProtect14() throws Throwable {
        String result = ISOUtil.protect("===\u3455w");
        assertEquals("result", "===__", result);
    }

    @Test
    public void testProtect15() throws Throwable {
        String result = ISOUtil.protect("=\u0AC4\uC024\uF29B=~2A)~5aCgl\"lLU*lm_cJ1M/!KFnA");
        assertEquals("result", "=___=________________________KFnA", result);
    }

    @Test
    public void testProtect16() throws Throwable {
        String result = ISOUtil.protect("\u30C5\uE09B\u6028\uB54E\u2094\uFA25\uAD56\u3A1F\uE55C\u31AA\u5FE0=$");
        assertEquals("result", "\u30C5\uE09B\u6028\uB54E\u2094\uFA25_\u3A1F\uE55C\u31AA\u5FE0=_", result);
    }

    @Test
    public void testProtect17() throws Throwable {
        String result = ISOUtil.protect("+6+[=I?");
        assertEquals("result", "+6+[=__", result);
    }

    @Test
    public void testProtect18() throws Throwable {
        String result = ISOUtil.protect("=======");
        assertEquals("result", "=======", result);
    }

    @Test
    public void testProtect19() throws Throwable {
        String result = ISOUtil.protect("===^^+^=");
        assertEquals("result", "===^^+^=", result);
    }

    @Test
    public void testProtect2() throws Throwable {
        String result = ISOUtil.protect("===^===");
        assertEquals("result", "===^===", result);
    }

    @Test
    public void testProtect20() throws Throwable {
        String result = ISOUtil.protect("\u6D1D^KI");
        assertEquals("result", "_^KI", result);
    }

    @Test
    public void testProtect21() throws Throwable {
        String result = ISOUtil.protect("=7G^=^");
        assertEquals("result", "=__^=^", result);
    }

    @Test
    public void testProtect3() throws Throwable {
        String result = ISOUtil.protect("^D==N^=r=\u0002^g)==");
        assertEquals("result", "^D==_^=_=_^g)==", result);
    }

    @Test
    public void testProtect4() throws Throwable {
        String result = ISOUtil.protect("=");
        assertEquals("result", "=", result);
    }

    @Test
    public void testProtect5() throws Throwable {
        String result = ISOUtil.protect("");
        assertEquals("result", "", result);
    }

    @Test
    public void testProtect6() throws Throwable {
        String result = ISOUtil.protect("VqM_'");
        assertEquals("result", "_____", result);
    }

    @Test
    public void testProtect7() throws Throwable {
        String result = ISOUtil.protect("\\7.=^6C3");
        assertEquals("result", "\\7.=^6C3", result);
    }

    @Test
    public void testProtect8() throws Throwable {
        String result = ISOUtil.protect("#<gF=uG!");
        assertEquals("result", "#<gF=___", result);
    }

    @Test
    public void testProtect9() throws Throwable {
        String result = ISOUtil.protect("^9a{=o;G");
        assertEquals("result", "^9a{=___", result);
    }

    @Test
    public void testProtectT2D1() throws Throwable {
        String result = ISOUtil.protect("#<gFDuG!");
        assertEquals("result", "#<gFD___", result);
    }

    @Test
    public void testProtectT2D2() throws Throwable {
        String result = ISOUtil.protect("9a{#<gFuG!53Do;G");
        assertEquals("result", "9a{#<g__G!53D___", result);
    }

    @Test
    public void testProtectT1D1() throws Throwable {
        String result = ISOUtil.protect("a{#<gFuG!53o;G609^FOO/BAR COM^67890o;G");
        assertEquals("result", "a{#<gF_______G609^FOO/BAR COM^________", result);
    }

    @Test
    public void testProtectT1D2() throws Throwable {
        String result = ISOUtil.protect("9a{#<gFuG!^FOO/BAR COM^67890o;G");
        assertEquals("result", "9a{#<gFuG!^FOO/BAR COM^________", result);
    }

    @Test
    public void testProtectT1D3() throws Throwable {
        String result = ISOUtil.protect("9a{D<gFuG!^FOO/BAR COM^67890o;G");
        assertEquals("result", "9a{D<gFuG!^FOO/BAR COM^________", result);
    }

    @Test
    public void testProtectThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.protect(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSleep() throws Throwable {
        ISOUtil.sleep(100L);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testSleepThrowsIllegalArgumentException() throws Throwable {
        try {
            ISOUtil.sleep(-30L);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "timeout value is negative", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcd() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", true);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) -16, result[0]);
    }

    @Test
    public void testStr2bcd1() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils", true);
        assertEquals("result.length", 6, result.length);
        assertEquals("result[0]", (byte) 117, result[0]);
    }

    @Test
    public void testStr2bcd10() throws Throwable {
        byte[] result = ISOUtil.str2bcd("", true);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testStr2bcd11() throws Throwable {
        byte[] result = ISOUtil.str2bcd("", true, (byte) 0);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testStr2bcd12() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils1", true, (byte) 0);
        assertEquals("result.length", 7, result.length);
        assertEquals("result[0]", (byte) 68, result[0]);
    }

    @Test
    public void testStr2bcd13() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils", true, (byte) 0);
        assertEquals("result.length", 6, result.length);
        assertEquals("result[0]", (byte) 117, result[0]);
    }

    @Test
    public void testStr2bcd14() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", true, (byte) 0);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) -16, result[0]);
    }

    @Test
    public void testStr2bcd15() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", false, (byte) 0);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testStr2bcd16() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils1", false, (byte) 0);
        assertEquals("result.length", 7, result.length);
        assertEquals("result[0]", (byte) 117, result[0]);
    }

    @Test
    public void testStr2bcd2() throws Throwable {
        byte[] d = new byte[0];
        byte[] result = ISOUtil.str2bcd("", true, d, 100);
        assertSame("result", d, result);
    }

    @Test
    public void testStr2bcd3() throws Throwable {
        byte[] d = new byte[1];
        byte[] result = ISOUtil.str2bcd("", true, d, 100);
        assertSame("result", d, result);
        assertEquals("d[0]", (byte) 0, d[0]);
    }

    @Test
    public void testStr2bcd4() throws Throwable {
        byte[] d = new byte[2];
        byte[] result = ISOUtil.str2bcd("3Ch", true, d, 0);
        assertEquals("d[0]", (byte) 3, d[0]);
        assertSame("result", d, result);
    }

    @Test
    public void testStr2bcd5() throws Throwable {
        byte[] d = new byte[2];
        byte[] result = ISOUtil.str2bcd(" ", false, d, 0);
        assertSame("result", d, result);
        assertEquals("d[0]", (byte) 0, d[0]);
    }

    @Test
    public void testStr2bcd6() throws Throwable {
        byte[] d = new byte[69];
        byte[] result = ISOUtil.str2bcd("testISOUtils1", false, d, 0);
        assertEquals("d[0]", (byte) 117, d[0]);
        assertSame("result", d, result);
    }

    @Test
    public void testStr2bcd7() throws Throwable {
        byte[] d = new byte[2];
        byte[] result = ISOUtil.str2bcd(" ", true, d, 0);
        assertEquals("d[0]", (byte) -16, d[0]);
        assertSame("result", d, result);
    }

    @Test
    public void testStr2bcd8() throws Throwable {
        byte[] d = new byte[4];
        byte[] result = ISOUtil.str2bcd("2C", true, d, 0);
        assertEquals("d[0]", (byte) 51, d[0]);
        assertSame("result", d, result);
    }

    @Test
    public void testStr2bcd9() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", false);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] d = new byte[2];
        try {
            ISOUtil.str2bcd("testISOUtils1", true, d, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("d[0]", (byte) 68, d[0]);
            // assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] d = new byte[2];
        try {
            ISOUtil.str2bcd("testISOUtils1", false, d, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("d[0]", (byte) 117, d[0]);
            // assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] d = new byte[2];
        try {
            ISOUtil.str2bcd("testISOUtils", true, d, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("d[0]", (byte) 117, d[0]);
            // assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] a = new byte[1];
        byte[] d = ISOUtil.asciiToEbcdic(a);
        try {
            ISOUtil.str2bcd("testISOUtils1", true, d, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("d[0]", (byte) 68, d[0]);
            // assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] d = new byte[2];
        try {
            ISOUtil.str2bcd("testISOUtils1", false, d, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            // assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.str2bcd(null, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsNullPointerException1() throws Throwable {
        try {
            ISOUtil.str2bcd(null, true, (byte) 0);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsNullPointerException2() throws Throwable {
        try {
            ISOUtil.str2bcd("testISOUtils1", true, (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsNullPointerException3() throws Throwable {
        try {
            ISOUtil.str2bcd("testISOUtils", true, (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStr2bcdThrowsNullPointerException4() throws Throwable {
        byte[] d = new byte[2];
        try {
            ISOUtil.str2bcd(null, true, d, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStrpad() throws Throwable {
        String result = ISOUtil.strpad("testISOUtils", 0);
        assertEquals("result", "testISOUtils", result);
    }

    @Test
    public void testStrpad1() throws Throwable {
        String result = ISOUtil.strpad("testISOUtils", 100);
        assertEquals("result",
                "testISOUtils                                                                                        ", result);
    }

    @Test
    public void testStrpadf() throws Throwable {
        String result = ISOUtil.strpadf("testISOUtils", 0);
        assertEquals("result", "testISOUtils", result);
    }

    @Test
    public void testStrpadf1() throws Throwable {
        String result = ISOUtil.strpadf("", 100);
        assertEquals("result",
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", result);
    }

    @Test
    public void testStrpadfThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.strpadf(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStrpadThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.strpad(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testToIntArray() throws Throwable {
        int[] result = ISOUtil.toIntArray("42");
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", 42, result[0]);
    }

    @Test
    public void testToIntArray1() throws Throwable {
        int[] result = ISOUtil.toIntArray("");
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testToIntArrayThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.toIntArray(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testToIntArrayThrowsNumberFormatException() throws Throwable {
        try {
            ISOUtil.toIntArray("testISOUtils");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \"testISOUtils\"", ex.getMessage());
        }
    }

    @Test
    public void testTrim() throws Throwable {
        byte[] array = new byte[2];
        byte[] result = ISOUtil.trim(array, 1);
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testTrim1() throws Throwable {
        byte[] array = new byte[2];
        byte[] result = ISOUtil.trim(array, 0);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testTrim2() throws Throwable {
        String result = ISOUtil.trim("testISOUtils");
        assertEquals("result", "testISOUtils", result);
    }

    @Test
    public void testTrim3() throws Throwable {
        String result = ISOUtil.trim(null);
        assertNull("result", result);
    }

    @Test
    public void testTrimf() throws Throwable {
        String result = ISOUtil.trimf("");
        assertEquals("result", "", result);
    }

    @Test
    public void testTrimf1() throws Throwable {
        String result = ISOUtil.trimf("2C");
        assertEquals("result", "2C", result);
    }

    @Test
    public void testTrimf2() throws Throwable {
        String result = ISOUtil.trimf("FF");
        assertEquals("result", "", result);
    }

    @Test
    public void testTrimf3() throws Throwable {
        String result = ISOUtil.trimf("F");
        assertEquals("result", "", result);
    }

    @Test
    public void testTrimf4() throws Throwable {
        String result = ISOUtil.trimf(" ");
        assertEquals("result", "", result);
    }

    @Test
    public void testTrimf5() throws Throwable {
        String result = ISOUtil.trimf(null);
        assertNull("result", result);
    }

    @Test
    public void testTrimThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] array = new byte[2];
        try {
            ISOUtil.trim(array, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testTrimThrowsNegativeArraySizeException() throws Throwable {
        byte[] array = new byte[3];
        try {
            ISOUtil.trim(array, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testTrimThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.trim((byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnPadLeft() throws Throwable {
        String result = ISOUtil.unPadLeft("", ' ');
        assertEquals("result", "", result);
    }

    @Test
    public void testUnPadLeft1() throws Throwable {
        String result = ISOUtil.unPadLeft("", '\u001F');
        assertEquals("result", "", result);
    }

    @Test
    public void testUnPadLeft3() throws Throwable {
        String result = ISOUtil.unPadLeft("testISOUtils", 't');
        assertEquals("result", "estISOUtils", result);
    }

    @Test
    public void testUnPadLeftThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.unPadLeft(null, ' ');
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnPadRight() throws Throwable {
        String result = ISOUtil.unPadRight("f", 'f');
        assertEquals("result", "f", result);
    }

    @Test
    public void testUnPadRight1() throws Throwable {
        String result = ISOUtil.unPadRight("", ' ');
        assertEquals("result", "", result);
    }

    @Test
    public void testUnPadRight2() throws Throwable {
        String result = ISOUtil.unPadRight("", 'A');
        assertEquals("result", "", result);
    }

    @Test
    public void testUnPadRight3() throws Throwable {
        String result = ISOUtil.unPadRight("f", ' ');
        assertEquals("result", "f", result);
    }

    @Test
    public void testUnPadRight4() throws Throwable {
        String result = ISOUtil.unPadRight("  &ON\\.!Wio=p^'@*xS'*ItLh|_g[,K2H|FkD]RPGQ", 'Q');
        assertEquals("result", "  &ON\\.!Wio=p^'@*xS'*ItLh|_g[,K2H|FkD]RPG", result);
    }

    @Test
    public void testUnPadRightThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.unPadRight(null, ' ');
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testXor() throws Throwable {
        byte[] op2 = new byte[0];
        byte[] result = ISOUtil.xor(ISOUtil.str2bcd("testISOUtils", true), op2);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testXor1() throws Throwable {
        byte[] op2 = new byte[4];
        byte[] op1 = new byte[0];
        byte[] result = ISOUtil.xor(op1, op2);
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testXor2() throws Throwable {
        byte[] op1 = new byte[3];
        byte[] op2 = new byte[2];
        byte[] result = ISOUtil.xor(op1, op2);
        assertEquals("result.length", 2, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testXor3() throws Throwable {
        byte[] op1 = new byte[3];
        byte[] op2 = new byte[5];
        byte[] result = ISOUtil.xor(op1, op2);
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", (byte) 0, result[0]);
    }

    @Test
    public void testXorThrowsNullPointerException() throws Throwable {
        byte[] op2 = new byte[0];
        try {
            ISOUtil.xor((byte[]) null, op2);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testZeropad() throws Throwable {
        String result = ISOUtil.zeropad("testISOUtils", 100);
        assertEquals("result",
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000testISOUtils", result);
    }

    @Test
    public void testZeropadRight() throws Throwable {
        String result = ISOUtil.zeropadRight("testISOUtils", 0);
        assertEquals("result", "testISOUtils", result);
    }

    @Test
    public void testZeropadRight1() throws Throwable {
        String result = ISOUtil.zeropadRight("", 100);
        assertEquals("result",
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", result);
    }

    @Test
    public void testZeropadRightThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.zeropadRight(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testZeropadThrowsISOException() throws Throwable {
        try {
            ISOUtil.zeropad("testISOUtils", 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len 12/0", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testZeropadThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.zeropad(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testZeroUnPad1() throws Throwable {
        String result = ISOUtil.zeroUnPad("");
        assertEquals("result", "", result);
    }

    @Test
    public void testZeroUnPadThrowsNullPointerException() throws Throwable {
        try {
            ISOUtil.zeroUnPad(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
    /**
     * Test of formatAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testFormatAmtConvRate() throws Exception {
        double rate = 3456.78;
        String expResult = "33456780";
        String result = ISOUtil.formatAmountConversionRate(rate);
        assertEquals(expResult, result);
    }

    /**
     * Test of formatAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testFormatAmtConvRate2() throws Exception {
        double rate = 0.00345678;
        String expResult = "93456780";
        String result = ISOUtil.formatAmountConversionRate(rate);
        assertEquals(expResult, result);
    }

    /**
     * Test of formatAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testFormatAmtConvRate3() throws Exception {
        double rate = 0.0000345678;
        String expResult = "90034567";
        String result = ISOUtil.formatAmountConversionRate(rate);
        assertEquals(expResult, result);
    }

    /**
     * Test of formatAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testFormatAmtConvRate4() throws Exception {
        double rate = 0;
        String expResult = null;
        String result = ISOUtil.formatAmountConversionRate(rate);
        assertEquals(expResult, result);
    }

    /**
     * Test of formatAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testFormatAmtConvRate5() throws Exception {
        double rate = 0.0000000001;
        String expResult = "90000000";
        String result = ISOUtil.formatAmountConversionRate(rate);
        assertEquals(expResult, result);
    }  
    
    /**
     * Test of parseAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testParseAmtConvRate() {
        String rate = "93456780";
        BigDecimal expResult = new BigDecimal(0.003456780, MathContext.DECIMAL64);
        BigDecimal result = new BigDecimal(ISOUtil.parseAmountConversionRate(rate),MathContext.DECIMAL64);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testParseAmtConvRate2() {
        String rate = "90034567";
        BigDecimal expResult = new BigDecimal(0.000034567, MathContext.DECIMAL64);
        BigDecimal result = new BigDecimal(ISOUtil.parseAmountConversionRate(rate), MathContext.DECIMAL64);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testParseAmtConvRate3() {
        String rate = null;
        try {
          ISOUtil.parseAmountConversionRate(rate);
          fail();
        } catch (IllegalArgumentException ex) {
          assertEquals("Invalid amount converion rate argument: '" +
              rate + "'", ex.getMessage());
        }
    }

    /**
     * Test of parseAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testParseAmtConvRate4() {
        String rate = "1234567";
        try {
          ISOUtil.parseAmountConversionRate(rate);
          fail();
        } catch (IllegalArgumentException ex) {
          assertEquals("Invalid amount converion rate argument: '" +
              rate + "'", ex.getMessage());
        }
    }

    /**
     * Test of parseAmtConvRate method, of class CSSUtil.
     */
    @Test
    public void testParseAmtConvRate5() {
        String rate = "123456789";
        try {
          ISOUtil.parseAmountConversionRate(rate);
          fail();
        } catch (IllegalArgumentException ex) {
          assertEquals("Invalid amount converion rate argument: '" +
              rate + "'", ex.getMessage());
        }
    }

    /**
     * @see org.jpos.iso.ISOUtil#commaEncode(String[])
     * @see org.jpos.iso.ISOUtil#commaDecode(String)
     */
    @Test
    public void testCommaEncodeAndDecode() {
        assertEquals("error encoding \"\"", "", ISOUtil.commaEncode(new String[]{}));
        assertEquals("error encoding \"a,b,c\"", "a,b,c", ISOUtil.commaEncode(new String[] { "a", "b", "c" }));
        assertEquals("error encoding \"\\,,\\\\,c\"", "\\,,\\\\,c", ISOUtil.commaEncode(new String[] { ",", "\\", "c"}));

        assertArrayEquals("error decoding \"\"", new String[]{}, ISOUtil.commaDecode(""));
        assertArrayEquals("error decoding \"a,b,c\"", new String[]{ "a", "b", "c"}, ISOUtil.commaDecode("a,b,c"));
        assertArrayEquals("error decoding \"\\,,\\\\,c\"",
                new String[] { ",", "\\", "c"}, ISOUtil.commaDecode("\\,,\\\\,c")
        );
    }
}
