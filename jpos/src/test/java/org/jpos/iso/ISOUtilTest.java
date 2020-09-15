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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ISOUtilTest {
    final String lineSep = System.getProperty("line.separator");

    @Test
    public void testAsciiToEbcdic() throws Throwable {
        byte[] a = new byte[0];
        byte[] result = ISOUtil.asciiToEbcdic(a);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testAsciiToEbcdic1() throws Throwable {
        byte[] a = new byte[1];
        byte[] result = ISOUtil.asciiToEbcdic(a);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testAsciiToEbcdic2() throws Throwable {
        byte[] e = new byte[13];
        ISOUtil.asciiToEbcdic("testISOUtils", e, 0);
        assertEquals((byte) -93, e[0], "e[0]");
    }

    @Test
    public void testAsciiToEbcdic3() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            ISOUtil.asciiToEbcdic("", new byte[3], 100);
        });
    }

    @Test
    public void testAsciiToEbcdic4() throws Throwable {
        byte[] result = ISOUtil.asciiToEbcdic("");
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testAsciiToEbcdic5() throws Throwable {
        byte[] result = ISOUtil.asciiToEbcdic("testISOUtils");
        assertEquals(12, result.length, "result.length");
        assertEquals((byte) -93, result[0], "result[0]");
    }

    @Test
    public void testAsciiToEbcdic6() throws Throwable {
        byte[] result = ISOUtil.asciiToEbcdic("testISOUtils");
        byte[] expected = new byte[]{(byte)0xA3, (byte)0x85, (byte)0xA2, (byte)0xA3, (byte)0xC9,
                (byte)0xE2, (byte)0xD6, (byte)0xE4, (byte)0xA3, (byte) 0x89, (byte)0x93, (byte)0xA2 };
        assertArrayEquals(expected, result, "full result");
    }

    @Test
    public void testAsciiToEbcdic7() throws Throwable {
        String testString = "testISOUtils1047`¬!\"£$%^&*()-=_+;:[]{}'@#~\\|,<>./?";
        byte[] result = ISOUtil.asciiToEbcdic(testString);
        byte[] expected = testString.getBytes("Cp1047");
        assertArrayEquals(expected, result, "full result");
    }

    @Test
    public void testAsciiToEbcdicThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] e = new byte[0];
        try {
            ISOUtil.asciiToEbcdic("testISOUtils", e, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) { }
    }


    @Test
    public void testAsciiToEbcdicThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.asciiToEbcdic((byte[]) null);
        });
    }

    @Test
    public void testAsciiToEbcdicThrowsNullPointerException1() throws Throwable {
        byte[] e = new byte[3];
        try {
            ISOUtil.asciiToEbcdic((String) null, e, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.lang.CharSequence.length()\" because \"csq\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(3, e.length, "e.length");
        }
    }

    @Test
    public void testAsciiToEbcdicThrowsNullPointerException2() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.asciiToEbcdic((String) null);
        });
    }

    @Test
    public void testBcd2str() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.bcd2str(b, 100, 0, true);
        assertEquals("", result, "result");
    }

    @Test
    public void testBcd2str1() throws Throwable {
        byte[] b = new byte[2];
        String result = ISOUtil.bcd2str(b, 0, 2, true);
        assertEquals("00", result, "result");
    }

    @Test
    public void testBcd2str2() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) -61;
        String result = ISOUtil.bcd2str(b, 0, 1, false);
        assertEquals("C", result, "result");
    }

    @Test
    public void testBcd2str3() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) -3;
        String result = ISOUtil.bcd2str(b, 0, 1, true);
        assertEquals("=", result, "result");
    }

    @Test
    public void testBcd2str4() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) -31;
        String result = ISOUtil.bcd2str(b, 0, 1, false);
        assertEquals("E", result, "result");
    }

    @Test
    public void testBcd2str5() throws Throwable {
        byte[] b = new byte[3];
        String result = ISOUtil.bcd2str(b, 0, 1, true);
        assertEquals("0", result, "result");
    }

    @Test
    public void testBcd2str6() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 14;
        String result = ISOUtil.bcd2str(b, 0, 3, true);
        assertEquals("00E", result, "result");
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] b = new byte[41];
            b[25] = (byte) -100;
            b[30] = (byte) 13;
            b[35] = (byte) -29;
            ISOUtil.bcd2str(b, 16, 61, true);
        });
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] b = new byte[41];
            b[25] = (byte) -100;
            b[30] = (byte) 13;
            b[35] = (byte) -29;
            ISOUtil.bcd2str(b, 16, 61, false);
        });
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] b = new byte[25];
            b[2] = (byte) -63;
            b[3] = (byte) 62;
            b[23] = (byte) 29;
            ISOUtil.bcd2str(b, 0, 100, true);
        });
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] b = new byte[41];
            b[25] = (byte) -100;
            b[35] = (byte) -29;
            ISOUtil.bcd2str(b, 16, 61, false);
        });
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] b = new byte[2];
            b[1] = (byte) 28;
            ISOUtil.bcd2str(b, 0, 27, true);
        });
    }

    @Test
    public void testBcd2strThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] b = new byte[25];
            b[2] = (byte) -63;
            b[3] = (byte) 62;
            ISOUtil.bcd2str(b, 0, 100, true);
        });
    }

    @Test
    public void testBcd2strThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(NegativeArraySizeException.class, () -> {
            byte[] b = new byte[3];
            ISOUtil.bcd2str(b, 100, -1, true);
        });
    }

    @Test
    public void testBcd2strThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.bcd2str(null, 100, 1, false);
        });
    }

    @Test
    public void testBcd2strThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.bcd2str(null, 100, 1, true);
        });
    }

    @Test
    public void testBcd2strThrowsNullPointerException2() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.bcd2str(null, 100, 1000, true);
        });
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
        assertEquals(8, result.length, "result.length");
        assertEquals((byte) -16, result[0], "result[0]");
    }

    @Test
    public void testBitSet2byte3() throws Throwable {
        byte[] result = ISOUtil.bitSet2byte(new BitSet(100));
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testBitSetByteHexInteroperability() throws Throwable {
        BitSet bs = new BitSet();
        bs.set(1);
        bs.set(63);
        bs.set(127);
        bs.set(191);
        byte[] b = ISOUtil.bitSet2byte(bs);
        BitSet bs1 = ISOUtil.byte2BitSet(b, 0, 192);
        BitSet bs2 = ISOUtil.hex2BitSet(ISOUtil.hexString(b).getBytes(), 0, 192);
        assertEquals(bs1, bs2, "BitSets should be equal");
    }

    @Test
    public void testBitSetByteHexInteroperability2() throws Throwable {
        byte[] b = ISOUtil.hex2byte("F23C04800EE0000080000000000000000000380000000000");
        BitSet bs1 = ISOUtil.byte2BitSet(b, 0, 192);
        BitSet bs2 = ISOUtil.hex2BitSet (ISOUtil.hexString(b).getBytes(), 0, 192);
        assertEquals(bs1, bs2, "BitSets should be equal");
        assertEquals(ISOUtil.hexString(b), ISOUtil.hexString(ISOUtil.bitSet2byte(bs1)), "Image matches");
    }

    @Test
    public void testBitSetByteHexInteroperability3() throws Throwable {
        byte[] b = ISOUtil.hex2byte("F23C04800AE00000800000000000010863BC780000000010");
        BitSet bs1 = ISOUtil.byte2BitSet(b, 0, 192);
        BitSet bs2 = ISOUtil.hex2BitSet (ISOUtil.hexString(b).getBytes(), 0, 192);
        assertEquals(bs1, bs2, "BitSets should be equal");
        assertEquals(ISOUtil.hexString(b), ISOUtil.hexString(ISOUtil.bitSet2byte(bs1)), "Image matches");
    }

    @Test
    public void testBitSet2byteThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.bitSet2byte(null);
        });
    }

    @Test
    public void testBitSet2extendedByte() throws Throwable {
        byte[] result = ISOUtil.bitSet2extendedByte(new BitSet(100));
        assertEquals(16, result.length, "result.length");
        assertEquals((byte) -128, result[0], "result[0]");
    }

    @Test
    public void testShortBitset2Byte() {
        byte[] expected = ISOUtil.hex2byte("C00000");
        BitSet b = new BitSet();
        b.set(1);
        b.set(2);
        int configuredLength = 3;
        int len = configuredLength >= 8 ? b.length()+62 >>6 <<3 : configuredLength;
        byte[] sb = ISOUtil.bitSet2byte(b, len);
        BitSet b1 = ISOUtil.byte2BitSet(sb, 0, len << 3);
        assertEquals(3, len);
        assertArrayEquals(expected, sb);
        assertEquals(b, b1);
    }

    @Test
    public void testBitSet2extendedByteThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.bitSet2extendedByte(null);
        });
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
                "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111000000000000000000000000000",
                result, "result");
    }

    @Test
    public void testBitSet2String1() throws Throwable {
        String result = ISOUtil.bitSet2String(new BitSet(100));
        assertEquals(
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                result, "result");
    }

    @Test
    public void testBitSet2String2() throws Throwable {
        String result = ISOUtil.bitSet2String(new BitSet(0));
        assertEquals("", result, "result");
    }

    @Test
    public void testBitSet2StringThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.bitSet2String(null);
        });
    }

    @Test
    public void testBlankUnPad() throws Throwable {
        String result = ISOUtil.blankUnPad("");
        assertEquals("", result, "result");
    }

    @Test
    public void testBlankUnPad1() throws Throwable {
        String result = ISOUtil.blankUnPad("1");
        assertEquals("1", result, "result");
    }

    @Test
    public void testBlankUnPadThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.blankUnPad(null);
        });
    }

    @Test
    public void testByte2BitSet() throws Throwable {
        byte[] b = new byte[9];
        BitSet result = ISOUtil.byte2BitSet(b, 0, true);
        assertEquals(64, result.size(), "result.size()");
    }

    @Test
    public void testByte2BitSet1() throws Throwable {
        byte[] b = new byte[9];
        b[4] = (byte) 1;
        BitSet result = ISOUtil.byte2BitSet(b, 0, true);
        assertEquals(64, result.size(), "result.size()");
    }

    @Test
    public void testByte2BitSet11() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 63);
        assertEquals(64, result.size(), "result.size()");
    }

    @Test
    public void testByte2BitSet2() throws Throwable {
        byte[] b = new byte[10];
        BitSet result = ISOUtil.byte2BitSet(b, 0, 1000);
        assertEquals(64, result.size(), "result.size()");
    }

    @Test
    public void testByte2BitSet3() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 127);
        assertEquals(64, result.size(), "result.size()");
    }

    @Test
    public void testByte2BitSet5() throws Throwable {
        byte[] b = new byte[1];
        BitSet result = ISOUtil.byte2BitSet(null, b, 100);
        assertNull(result, "result");
    }

    @Test
    public void testByte2BitSet6() throws Throwable {
        byte[] b = new byte[0];
        BitSet result = ISOUtil.byte2BitSet(null, b, 100);
        assertNull(result, "result");
    }

    @Test
    public void testByte2BitSet7() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 128);
        assertEquals(64, result.size(), "result.size()");
    }

    @Test
    public void testByte2BitSet8() throws Throwable {
        byte[] b = new byte[9];
        b[1] = (byte) -3;
        BitSet result = ISOUtil.byte2BitSet(b, 0, 1000);
        assertEquals(64, result.size(), "result.size()");
    }

    @Test
    public void testByte2BitSet9() throws Throwable {
        byte[] b = new byte[10];
        BitSet result = ISOUtil.byte2BitSet(b, 0, 100);
        assertEquals(64, result.size(), "result.size()");
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("71", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 71 out of bounds for length 71", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException10() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.byte2BitSet(b, 100, 64);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("100", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 100 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException13() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException14() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException15() throws Throwable {
        byte[] b = new byte[0];
        try {
            ISOUtil.byte2BitSet(b, 100, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("100", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 100 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException16() throws Throwable {
        byte[] b = new byte[0];
        try {
            ISOUtil.byte2BitSet(b, 100, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("100", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 100 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

//    This test SHOULD NOT throw an ArrayIndexOutOfBoundsException
//    because the first bit of the first byte (byte[1] since we're calling with offset 1) is 0.
//    Therefore, only 8 bytes should be decoded, which is enough for the 11 available bytes in the array
//    The test used to be successful (i.e. failed) because of a bad implementation of ISOUtil.byte2BitSet
//    when asked for more than 128 bits, and bit 1 of the bitmap was 0 and bit 65 was one (in which case it
//    wrongly attempted to unpack 24 bytes instead of just 8)
//    @Test
//    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException17() throws Throwable {
//        byte[] b = new byte[12];
//        b[1] = (byte) 1;
//        b[9] = (byte) -128;
//        try {
//            ISOUtil.byte2BitSet(b, 1, 129);
//            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
//        } catch (ArrayIndexOutOfBoundsException ex) {
//            assertEquals("ex.getMessage()", "12", ex.getMessage());
//        }
//    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException18() throws Throwable {
        byte[] b = new byte[12];
        b[1] = (byte) -63;
        b[9] = (byte) -128;
        try {
            ISOUtil.byte2BitSet(b, 1, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("12", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 12 out of bounds for length 12", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException7() throws Throwable {
        byte[] b = new byte[2];
        try {
            ISOUtil.byte2BitSet(b, 0, 128);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException8() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, 63);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testByte2BitSetThrowsArrayIndexOutOfBoundsException9() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.byte2BitSet(b, 0, 129);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("bitIndex < 0: -22", ex.getMessage(), "ex.getMessage()");
            assertEquals(128, bmap.size(), "bmap.size()");
        }
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.byte2BitSet(null, 100, true);
        });
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            byte[] b = new byte[4];
            b[2] = (byte) 127;
            ISOUtil.byte2BitSet(null, b, 100);
        });
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException2() throws Throwable {
        BitSet bmap = new BitSet(100);
        try {
            ISOUtil.byte2BitSet(bmap, null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"b\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(128, bmap.size(), "bmap.size()");
        }
    }

    @Test
    public void testByte2BitSetThrowsNullPointerException3() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.byte2BitSet(null, 100, 65);
        });
    }

    @Test
    public void testConcat() throws Throwable {
        byte[] array1 = new byte[3];
        byte[] result = ISOUtil.concat(array1, 0, 1, ISOUtil.asciiToEbcdic("testISOUtils"), 10, 0);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testConcat1() throws Throwable {
        byte[] array2 = new byte[3];
        byte[] array1 = new byte[3];
        byte[] result = ISOUtil.concat(array1, 0, 0, array2, 1, 0);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testConcat2() throws Throwable {
        byte[] array1 = new byte[1];
        byte[] array2 = new byte[3];
        byte[] result = ISOUtil.concat(array1, array2);
        assertEquals(4, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testConcat3() throws Throwable {
        byte[] array2 = new byte[0];
        byte[] array1 = new byte[0];
        byte[] result = ISOUtil.concat(array1, array2);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testConcatThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(NegativeArraySizeException.class, () -> {
            byte[] array1 = new byte[0];
            byte[] array2 = new byte[1];
            ISOUtil.concat(array1, 100, 0, array2, 1000, -1);
        });
    }

    @Test
    public void testConcatThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.concat(null, 100, 1000, ISOUtil.asciiToEbcdic("testISOUtils"), 0, -1);
        });
    }

    @Test
    public void testConcatThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            byte[] array2 = new byte[3];
            ISOUtil.concat(null, array2);
        });
    }

    @Test
    public void testDumpString() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 22;
        b[2] = (byte) 7;
        b[3] = (byte) 29;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{SYN}{BEL}[1D]\uFFEF", result, "result");
    }

    @Test
    public void testDumpString1() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 32;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ACK} {NULL}{DLE}{NULL}", result, "result");
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
        assertEquals("{ACK}{ENQ}\uFFE3\uFFFA[1B]", result, "result");
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
        assertEquals("{NAK}[0F]\uFF80\uFFFE{DLE}", result, "result");
    }

    @Test
    public void testDumpString101() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 13;
        b[1] = (byte) 93;
        b[2] = (byte) 17;
        String result = ISOUtil.dumpString(b);
        assertEquals("{CR}][11]", result, "result");
    }

    @Test
    public void testDumpString102() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 1;
        b[2] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SOH}{NULL}[1D]{NULL}", result, "result");
    }

    @Test
    public void testDumpString103() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[2] = (byte) -16;
        b[3] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{NULL}\uFFF0{FS}{NULL}", result, "result");
    }

    @Test
    public void testDumpString104() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 5;
        b[1] = (byte) 32;
        b[2] = (byte) 127;
        b[3] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ} [7F]{NAK}{NULL}", result, "result");
    }

    @Test
    public void testDumpString105() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 11;
        b[1] = (byte) -10;
        b[2] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("[0B]\uFFF6{NAK}", result, "result");
    }

    @Test
    public void testDumpString106() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        b[1] = (byte) -17;
        b[2] = (byte) 20;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}\uFFEF[14]", result, "result");
    }

    @Test
    public void testDumpString107() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 6;
        b[2] = (byte) 25;
        b[3] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{ACK}[19]\uFFE9", result, "result");
    }

    @Test
    public void testDumpString108() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 10;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{LF}", result, "result");
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
        assertEquals("{SOH}\uFFF1\uFFED\uFF94\uFFF0", result, "result");
    }

    @Test
    public void testDumpString11() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 24;
        b[1] = (byte) 6;
        b[3] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("[18]{ACK}{NULL}{SYN}", result, "result");
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
        assertEquals("{DLE}[09]]\uFFFD\uFFFF", result, "result");
    }

    @Test
    public void testDumpString111() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[1] = (byte) -13;
        b[2] = (byte) 26;
        String result = ISOUtil.dumpString(b);
        assertEquals("{BEL}\uFFF3[1A]", result, "result");
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
        assertEquals("{ACK}[0E]^\uFFFB\uFFE4", result, "result");
    }

    @Test
    public void testDumpString113() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) 30;
        b[2] = (byte) -91;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}{RS}\uFFA5{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString114() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString115() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 15;
        b[2] = (byte) -93;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}[0F]\uFFA3{NULL}{FS}", result, "result");
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
        assertEquals("{RS}\uFFE6\uFFF9\uFFE5[1B]", result, "result");
    }

    @Test
    public void testDumpString117() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 1;
        b[1] = (byte) 96;
        b[2] = (byte) 29;
        b[3] = (byte) 12;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SOH}`[1D][0C]", result, "result");
    }

    @Test
    public void testDumpString118() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 5;
        b[1] = (byte) 32;
        b[3] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ} {NULL}{NAK}{NULL}", result, "result");
    }

    @Test
    public void testDumpString119() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 16;
        b[1] = (byte) 9;
        b[2] = (byte) 93;
        String result = ISOUtil.dumpString(b);
        assertEquals("{DLE}[09]]{NULL}{NULL}", result, "result");
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
        assertEquals("{EOT}{SOH}{CR}\uFFE9[17]", result, "result");
    }

    @Test
    public void testDumpString120() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 24;
        b[1] = (byte) 6;
        b[2] = (byte) 23;
        b[3] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("[18]{ACK}[17]{SYN}", result, "result");
    }

    @Test
    public void testDumpString121() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 1;
        b[1] = (byte) 15;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SOH}[0F]", result, "result");
    }

    @Test
    public void testDumpString122() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 30;
        b[2] = (byte) 2;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{RS}{STX}\uFFFD", result, "result");
    }

    @Test
    public void testDumpString123() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{BEL}", result, "result");
    }

    @Test
    public void testDumpString124() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[1] = (byte) 1;
        b[2] = (byte) 13;
        b[3] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}{SOH}{CR}\uFFE9{NULL}", result, "result");
    }

    @Test
    public void testDumpString125() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 22;
        b[2] = (byte) 7;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}{SYN}{BEL}{NULL}\uFFEF", result, "result");
    }

    @Test
    public void testDumpString126() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[1] = (byte) 8;
        b[2] = (byte) -8;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ}[08]\uFFF8{DLE}", result, "result");
    }

    @Test
    public void testDumpString127() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 1;
        b[3] = (byte) -9;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{SOH}{NULL}\uFFF7", result, "result");
    }

    @Test
    public void testDumpString128() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 21;
        b[1] = (byte) -18;
        b[2] = (byte) 91;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}\uFFEE[", result, "result");
    }

    @Test
    public void testDumpString129() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[1] = (byte) -24;
        String result = ISOUtil.dumpString(b);
        assertEquals("{BEL}\uFFE8{NULL}", result, "result");
    }

    @Test
    public void testDumpString13() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[1] = (byte) -8;
        b[3] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}\uFFF8{NULL}[1F]", result, "result");
    }

    @Test
    public void testDumpString130() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[2] = (byte) 119;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ}{NULL}w{NULL}", result, "result");
    }

    @Test
    public void testDumpString131() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{ENQ}", result, "result");
    }

    @Test
    public void testDumpString132() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -6;
        b[4] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("{LF}\uFFFA{NULL}{NULL}{SYN}", result, "result");
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
        assertEquals("{LF}\uFFFA\uFFE3[0B]{SYN}", result, "result");
    }

    @Test
    public void testDumpString134() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 30;
        b[2] = (byte) 2;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{RS}{STX}\uFFFD", result, "result");
    }

    @Test
    public void testDumpString135() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 11;
        b[2] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("[0B]{NULL}{NAK}", result, "result");
    }

    @Test
    public void testDumpString136() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[1] = (byte) 8;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ}[08]{NULL}{DLE}", result, "result");
    }

    @Test
    public void testDumpString137() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 21;
        b[2] = (byte) -16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{NAK}\uFFF0{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString138() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        b[1] = (byte) -4;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}\uFFFC{NULL}", result, "result");
    }

    @Test
    public void testDumpString139() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[1] = (byte) 23;
        b[2] = (byte) 119;
        b[3] = (byte) -105;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ}[17]w\uFF97", result, "result");
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
        assertEquals("{LF}\uFFF4[1F][0F][1A]", result, "result");
    }

    @Test
    public void testDumpString140() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 1;
        b[1] = (byte) 96;
        b[2] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SOH}`[1D]{NULL}", result, "result");
    }

    @Test
    public void testDumpString141() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 15;
        b[2] = (byte) -128;
        b[4] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}[0F]\uFF80{NULL}{DLE}", result, "result");
    }

    @Test
    public void testDumpString142() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}{NULL}", result, "result");
    }

    @Test
    public void testDumpString143() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 22;
        b[3] = (byte) 29;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}{SYN}{NULL}[1D]\uFFEF", result, "result");
    }

    @Test
    public void testDumpString144() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 13;
        b[2] = (byte) 17;
        String result = ISOUtil.dumpString(b);
        assertEquals("{CR}{NULL}[11]", result, "result");
    }

    @Test
    public void testDumpString145() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 32;
        b[2] = (byte) 127;
        b[3] = (byte) 21;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL} [7F]{NAK}{NULL}", result, "result");
    }

    @Test
    public void testDumpString146() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[1] = (byte) 1;
        b[2] = (byte) 13;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}{SOH}{CR}{NULL}[17]", result, "result");
    }

    @Test
    public void testDumpString147() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 22;
        b[2] = (byte) -26;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{NULL}\uFFE6{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString148() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -29;
        b[4] = (byte) 9;
        String result = ISOUtil.dumpString(b);
        assertEquals("{RS}\uFFE3{NULL}{NULL}[09]", result, "result");
    }

    @Test
    public void testDumpString149() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{NULL}", result, "result");
    }

    @Test
    public void testDumpString15() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 23;
        b[2] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}[17]{ENQ}{NULL}", result, "result");
    }

    @Test
    public void testDumpString150() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[2] = (byte) -91;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}{NULL}\uFFA5{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString151() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) -20;
        b[4] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}\uFFEC{NULL}{NULL}{EOT}", result, "result");
    }

    @Test
    public void testDumpString16() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 1;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SOH}{NULL}", result, "result");
    }

    @Test
    public void testDumpString17() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 5;
        b[2] = (byte) -29;
        b[4] = (byte) 27;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ACK}{ENQ}\uFFE3{NULL}[1B]", result, "result");
    }

    @Test
    public void testDumpString18() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}[17]{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString19() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 5;
        b[2] = (byte) -8;
        b[3] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ}{NULL}\uFFF8{DLE}", result, "result");
    }

    @Test
    public void testDumpString2() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 4;
        b[1] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}{ENQ}", result, "result");
    }

    @Test
    public void testDumpString20() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 1;
        b[2] = (byte) 9;
        b[3] = (byte) -9;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{SOH}[09]\uFFF7", result, "result");
    }

    @Test
    public void testDumpString21() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -26;
        String result = ISOUtil.dumpString(b);
        assertEquals("{RS}\uFFE6{NULL}{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString22() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 8;
        b[2] = (byte) -31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}[08]\uFFE1{NULL}", result, "result");
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
        assertEquals("{STX}\uFFEB\uFFFA{BEL}[1F]", result, "result");
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
        assertEquals("{STX}{NAK}\uFFF0{FS}{STX}", result, "result");
    }

    @Test
    public void testDumpString25() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 2;
        b[2] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{NULL}\uFFE9", result, "result");
    }

    @Test
    public void testDumpString26() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -12;
        b[2] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{LF}\uFFF4[1F]{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString27() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[2] = (byte) 2;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{NULL}{STX}\uFFFD", result, "result");
    }

    @Test
    public void testDumpString28() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 18;
        b[2] = (byte) -26;
        b[3] = (byte) 11;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}[12]\uFFE6[0B]", result, "result");
    }

    @Test
    public void testDumpString29() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[2] = (byte) -93;
        b[3] = (byte) 4;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{NULL}\uFFA3{EOT}{FS}", result, "result");
    }

    @Test
    public void testDumpString3() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 30;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{RS}", result, "result");
    }

    @Test
    public void testDumpString30() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[1] = (byte) -8;
        b[2] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}\uFFF8{DLE}{NULL}", result, "result");
    }

    @Test
    public void testDumpString31() throws Throwable {
        byte[] b = new byte[5];
        b[2] = (byte) 13;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{NULL}{CR}{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString32() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 49;
        String result = ISOUtil.dumpString(b);
        assertEquals("1", result, "result");
    }

    @Test
    public void testDumpString33() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 16;
        b[1] = (byte) -7;
        String result = ISOUtil.dumpString(b);
        assertEquals("{DLE}\uFFF9", result, "result");
    }

    @Test
    public void testDumpString34() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[1] = (byte) -8;
        b[2] = (byte) 16;
        b[3] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}\uFFF8{DLE}[1F]", result, "result");
    }

    @Test
    public void testDumpString35() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 26;
        b[1] = (byte) 8;
        b[2] = (byte) -24;
        String result = ISOUtil.dumpString(b);
        assertEquals("[1A][08]\uFFE8", result, "result");
    }

    @Test
    public void testDumpString36() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 16;
        b[2] = (byte) 93;
        String result = ISOUtil.dumpString(b);
        assertEquals("{DLE}{NULL}]{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString37() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 21;
        b[1] = (byte) -18;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}\uFFEE{NULL}", result, "result");
    }

    @Test
    public void testDumpString38() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 21;
        b[2] = (byte) -16;
        b[3] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{NAK}\uFFF0{FS}{NULL}", result, "result");
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
        assertEquals("{STX}[0F]\uFFA3{EOT}{FS}", result, "result");
    }

    @Test
    public void testDumpString4() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) -12;
        b[2] = (byte) 27;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}\uFFF4[1B]{NULL}", result, "result");
    }

    @Test
    public void testDumpString40() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 28;
        b[2] = (byte) 20;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}{NULL}[14]", result, "result");
    }

    @Test
    public void testDumpString41() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 6;
        b[1] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ACK}{BEL}", result, "result");
    }

    @Test
    public void testDumpString42() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 24;
        b[3] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("[18]{NULL}{NULL}{SYN}", result, "result");
    }

    @Test
    public void testDumpString43() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[1] = (byte) 5;
        b[2] = (byte) -29;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ACK}{ENQ}\uFFE3{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString44() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) -1;
        b[2] = (byte) 13;
        String result = ISOUtil.dumpString(b);
        assertEquals("\uFFFF{NULL}{CR}{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString45() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 4;
        b[1] = (byte) -22;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}\uFFEA", result, "result");
    }

    @Test
    public void testDumpString46() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[1] = (byte) 1;
        b[3] = (byte) -23;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}{SOH}{NULL}\uFFE9[17]", result, "result");
    }

    @Test
    public void testDumpString47() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 15;
        b[2] = (byte) -93;
        b[3] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}[0F]\uFFA3{EOT}{NULL}", result, "result");
    }

    @Test
    public void testDumpString48() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 6;
        b[1] = (byte) 8;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ACK}[08]", result, "result");
    }

    @Test
    public void testDumpString49() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 2;
        b[1] = (byte) 16;
        b[2] = (byte) -23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{DLE}\uFFE9", result, "result");
    }

    @Test
    public void testDumpString5() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[1] = (byte) -6;
        b[3] = (byte) 11;
        b[4] = (byte) 22;
        String result = ISOUtil.dumpString(b);
        assertEquals("{LF}\uFFFA{NULL}[0B]{SYN}", result, "result");
    }

    @Test
    public void testDumpString50() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 24;
        b[1] = (byte) 18;
        String result = ISOUtil.dumpString(b);
        assertEquals("[18][12]", result, "result");
    }

    @Test
    public void testDumpString51() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.dumpString(b);
        assertEquals("", result, "result");
    }

    @Test
    public void testDumpString52() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) -21;
        b[3] = (byte) 7;
        b[4] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}\uFFEB{NULL}{BEL}[1F]", result, "result");
    }

    @Test
    public void testDumpString53() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 10;
        b[2] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{LF}{NULL}[1F]{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString54() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 5;
        b[1] = (byte) 32;
        b[2] = (byte) 127;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ENQ} [7F]{NULL}{NULL}", result, "result");
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
        assertEquals("{ACK} \uFFA3{DLE}\uFFE4", result, "result");
    }

    @Test
    public void testDumpString56() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 1;
        b[2] = (byte) 13;
        b[3] = (byte) -23;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{SOH}{CR}\uFFE9[17]", result, "result");
    }

    @Test
    public void testDumpString57() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[2] = (byte) 26;
        String result = ISOUtil.dumpString(b);
        assertEquals("{BEL}{NULL}[1A]", result, "result");
    }

    @Test
    public void testDumpString58() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 1;
        b[2] = (byte) 9;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{SOH}[09]{NULL}", result, "result");
    }

    @Test
    public void testDumpString59() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) -12;
        b[2] = (byte) 27;
        b[3] = (byte) 12;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}\uFFF4[1B][0C]", result, "result");
    }

    @Test
    public void testDumpString6() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) 23;
        b[2] = (byte) 5;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}[17]{ENQ}{NULL}", result, "result");
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
        assertEquals("{ENQ} [7F]{NAK}\uFFF7", result, "result");
    }

    @Test
    public void testDumpString61() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) -20;
        b[2] = (byte) 13;
        b[4] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}\uFFEC{CR}{NULL}{EOT}", result, "result");
    }

    @Test
    public void testDumpString62() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 4;
        b[1] = (byte) 23;
        b[2] = (byte) 5;
        b[3] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}[17]{ENQ}[1D]", result, "result");
    }

    @Test
    public void testDumpString63() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 1;
        b[1] = (byte) -15;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SOH}\uFFF1{NULL}{NULL}{NULL}", result, "result");
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
        assertEquals("{ETX}\uFFEC{CR}\uFFE8{EOT}", result, "result");
    }

    @Test
    public void testDumpString65() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) -18;
        b[2] = (byte) 30;
        b[3] = (byte) -27;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}\uFFEE{RS}\uFFE5", result, "result");
    }

    @Test
    public void testDumpString66() throws Throwable {
        byte[] b = new byte[3];
        b[2] = (byte) 6;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{NULL}{ACK}", result, "result");
    }

    @Test
    public void testDumpString67() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 2;
        b[1] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{DLE}{NULL}", result, "result");
    }

    @Test
    public void testDumpString68() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) -21;
        b[4] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}\uFFEB{NULL}{NULL}[1F]", result, "result");
    }

    @Test
    public void testDumpString69() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) -15;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}\uFFF1", result, "result");
    }

    @Test
    public void testDumpString7() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) -21;
        b[3] = (byte) 7;
        b[4] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}\uFFEB{NULL}{BEL}[1F]", result, "result");
    }

    @Test
    public void testDumpString70() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 22;
        b[1] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}[1D]", result, "result");
    }

    @Test
    public void testDumpString71() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 15;
        b[3] = (byte) 4;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}[0F]{NULL}{EOT}{FS}", result, "result");
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
        assertEquals("{RS}\uFFE3\uFFF8{BEL}[09]", result, "result");
    }

    @Test
    public void testDumpString73() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 3;
        b[1] = (byte) 92;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}\\", result, "result");
    }

    @Test
    public void testDumpString74() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[2] = (byte) -128;
        b[4] = (byte) 16;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}{NULL}\uFF80{NULL}{DLE}", result, "result");
    }

    @Test
    public void testDumpString75() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) -19;
        b[1] = (byte) 30;
        String result = ISOUtil.dumpString(b);
        assertEquals("\uFFED{RS}", result, "result");
    }

    @Test
    public void testDumpString76() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 6;
        b[1] = (byte) -27;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ACK}\uFFE5", result, "result");
    }

    @Test
    public void testDumpString77() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 28;
        b[2] = (byte) 3;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{FS}{ETX}", result, "result");
    }

    @Test
    public void testDumpString78() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) -20;
        b[2] = (byte) 13;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}\uFFEC{CR}{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString79() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 4;
        b[2] = (byte) 13;
        b[3] = (byte) -23;
        b[4] = (byte) 23;
        String result = ISOUtil.dumpString(b);
        assertEquals("{EOT}{NULL}{CR}\uFFE9[17]", result, "result");
    }

    @Test
    public void testDumpString8() throws Throwable {
        byte[] b = new byte[5];
        b[1] = (byte) 15;
        b[2] = (byte) -93;
        b[3] = (byte) 4;
        b[4] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}[0F]\uFFA3{EOT}{FS}", result, "result");
    }

    @Test
    public void testDumpString80() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 30;
        b[3] = (byte) -3;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{RS}{NULL}\uFFFD", result, "result");
    }

    @Test
    public void testDumpString81() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) 21;
        b[3] = (byte) 28;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}{NAK}{NULL}{FS}{NULL}", result, "result");
    }

    @Test
    public void testDumpString82() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 15;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}[0F]", result, "result");
    }

    @Test
    public void testDumpString83() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 3;
        b[1] = (byte) -20;
        b[2] = (byte) 13;
        b[4] = (byte) 4;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}\uFFEC{CR}{NULL}{EOT}", result, "result");
    }

    @Test
    public void testDumpString84() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 3;
        b[2] = (byte) 6;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}{NULL}{ACK}", result, "result");
    }

    @Test
    public void testDumpString85() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[2] = (byte) 9;
        b[3] = (byte) -9;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{NULL}[09]\uFFF7", result, "result");
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
        assertEquals("{ETX}{RS}\uFFA5^\uFFE2", result, "result");
    }

    @Test
    public void testDumpString87() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 7;
        b[1] = (byte) -24;
        b[2] = (byte) 90;
        String result = ISOUtil.dumpString(b);
        assertEquals("{BEL}\uFFE8Z", result, "result");
    }

    @Test
    public void testDumpString88() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 2;
        b[1] = (byte) -21;
        b[3] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("{STX}\uFFEB{NULL}{BEL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString89() throws Throwable {
        byte[] b = new byte[1];
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}", result, "result");
    }

    @Test
    public void testDumpString9() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 6;
        b[2] = (byte) 94;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ACK}{NULL}^{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString90() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 1;
        b[1] = (byte) 10;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SOH}{LF}", result, "result");
    }

    @Test
    public void testDumpString91() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 22;
        b[1] = (byte) 30;
        b[2] = (byte) 2;
        String result = ISOUtil.dumpString(b);
        assertEquals("{SYN}{RS}{STX}{NULL}", result, "result");
    }

    @Test
    public void testDumpString92() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[1] = (byte) 22;
        b[2] = (byte) 7;
        b[3] = (byte) 29;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}{SYN}{BEL}[1D]{NULL}", result, "result");
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
        assertEquals("{NAK}{SYN}{BEL}[1D]\uFFEF", result, "result");
    }

    @Test
    public void testDumpString94() throws Throwable {
        byte[] b = new byte[3];
        b[0] = (byte) 3;
        String result = ISOUtil.dumpString(b);
        assertEquals("{ETX}{NULL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString95() throws Throwable {
        byte[] b = new byte[4];
        b[1] = (byte) 6;
        b[2] = (byte) 25;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NULL}{ACK}[19]{NULL}", result, "result");
    }

    @Test
    public void testDumpString96() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 21;
        b[2] = (byte) 7;
        b[3] = (byte) 29;
        b[4] = (byte) -17;
        String result = ISOUtil.dumpString(b);
        assertEquals("{NAK}{NULL}{BEL}[1D]\uFFEF", result, "result");
    }

    @Test
    public void testDumpString97() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -29;
        b[3] = (byte) 7;
        String result = ISOUtil.dumpString(b);
        assertEquals("{RS}\uFFE3{NULL}{BEL}{NULL}", result, "result");
    }

    @Test
    public void testDumpString98() throws Throwable {
        byte[] b = new byte[4];
        b[0] = (byte) 28;
        b[2] = (byte) 16;
        b[3] = (byte) 31;
        String result = ISOUtil.dumpString(b);
        assertEquals("{FS}{NULL}{DLE}[1F]", result, "result");
    }

    @Test
    public void testDumpString99() throws Throwable {
        byte[] b = new byte[5];
        b[0] = (byte) 30;
        b[1] = (byte) -29;
        b[3] = (byte) 7;
        b[4] = (byte) 9;
        String result = ISOUtil.dumpString(b);
        assertEquals("{RS}\uFFE3{NULL}{BEL}[09]", result, "result");
    }

    @Test
    public void testDumpStringThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.dumpString(null);
        });
    }

    @Test
    public void testEbcdicToAscii() throws Throwable {
        byte[] e = new byte[2];
        String result = ISOUtil.ebcdicToAscii(e);
        assertEquals("\u0000\u0000", result, "result");
    }

    @Test
    public void testEbcdicToAscii1() throws Throwable {
        byte[] e = new byte[3];
        String result = ISOUtil.ebcdicToAscii(e, 0, 1);
        assertEquals("\u0000", result, "result");
    }

    @Test
    public void testEbcdicToAsciiBytes() throws Throwable {
        byte[] e = new byte[2];
        byte[] result = ISOUtil.ebcdicToAsciiBytes(e, 0, 1);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testEbcdicToAsciiBytes2() throws Throwable {
        byte[] e = new byte[0];
        byte[] result = ISOUtil.ebcdicToAsciiBytes(e);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testEbcdicToAsciiBytes3() throws Throwable {
        byte[] e = new byte[2];
        byte[] result = ISOUtil.ebcdicToAsciiBytes(e);
        assertEquals(2, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            ISOUtil.ebcdicToAsciiBytes(new byte[1], 0, 100);
        });
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            ISOUtil.ebcdicToAsciiBytes(new byte[1], 100, 1000);
        });
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            byte[] e = new byte[1];
            ISOUtil.ebcdicToAsciiBytes(e, 100, -1);
        });
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.ebcdicToAsciiBytes(null, 100, 1000);
        });
    }

    @Test
    public void testEbcdicToAsciiBytesThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.ebcdicToAsciiBytes(null);
        });
    }

    @Test
    public void testEbcdicToAsciiThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            ISOUtil.ebcdicToAscii(new byte[1], 100, 1000);
        });
    }

    @Test
    public void testEbcdicToAsciiThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            ISOUtil.ebcdicToAscii(new byte[1], 100, -1);
        });
    }

    @Test
    public void testEbcdicToAsciiThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.ebcdicToAscii(null);
        });
    }

    @Test
    public void testFormatAmount() throws Throwable {
        String result = ISOUtil.formatAmount(100000L, 7);
        assertEquals("1000.00", result, "result");
    }

    @Test
    public void testFormatAmount1() throws Throwable {
        String result = ISOUtil.formatAmount(1000L, 100);
        assertEquals(
                "                                                                                               10.00",
                result, "result");
    }

    @Test
    public void testFormatAmount2() throws Throwable {
        String result = ISOUtil.formatAmount(100L, 100);
        assertEquals(
                "                                                                                                1.00",
                result, "result");
    }

    @Test
    public void testFormatAmount3() throws Throwable {
        String result = ISOUtil.formatAmount(99L, 100);
        assertEquals(
                "                                                                                                0.99",
                result, "result");
    }

    @Test
    public void testFormatAmount4() throws Throwable {
        String result = ISOUtil.formatAmount(101L, 4);
        assertEquals("1.01", result, "result");
    }

    @Test
    public void testFormatAmountThrowsISOException() throws Throwable {
        try {
            ISOUtil.formatAmount(99L, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len 3/-1", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testFormatAmountThrowsISOException1() throws Throwable {
        try {
            ISOUtil.formatAmount(100L, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len 3/-1", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testFormatAmountThrowsISOException2() throws Throwable {
        try {
            ISOUtil.formatAmount(Long.MIN_VALUE, 100);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len 20/3", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testFormatDouble() throws Throwable {
        String result = ISOUtil.formatDouble(100.0, 100);
        assertEquals(
                "                                                                                              100.00",
                result, "result");
    }

    @Test
    public void testFormatDouble1() throws Throwable {
        String result = ISOUtil.formatDouble(100.0, 0);
        assertEquals("100.00", result, "result");
    }

    @Test
    public void testHex2BitSet() throws Throwable {
        byte[] b = new byte[82];
        BitSet result = ISOUtil.hex2BitSet(b, 0, false);
        assertEquals(128, result.size(), "result.size()");
    }

    @Test
    public void testHex2BitSet1() throws Throwable {
        byte[] b = new byte[82];
        BitSet result = ISOUtil.hex2BitSet(b, 0, true);
        assertEquals(256, result.size(), "result.size()");
    }

    @Test
    public void testHex2BitSet10() throws Throwable {
        byte[] b = new byte[80];
        BitSet result = ISOUtil.hex2BitSet(b, 0, 1000);
        assertEquals(384, result.size(), "result.size()");
    }

    @Test
    public void testHex2BitSet3() throws Throwable {
        byte[] b = new byte[0];
        BitSet result = ISOUtil.hex2BitSet(null, b, 100);
        assertNull(result, "result");
    }

    @Test
    public void testHex2BitSet4() throws Throwable {
        byte[] b = new byte[20];
        b[11] = (byte) 65;
        BitSet result = ISOUtil.hex2BitSet(b, 0, false);
        assertEquals(128, result.size(), "result.size()");
    }

    @Test
    public void testHex2BitSet7() throws Throwable {
        byte[] b = new byte[46];
        BitSet result = ISOUtil.hex2BitSet(b, 0, 64);
        assertEquals(128, result.size(), "result.size()");
    }

    @Test
    public void testHex2BitSet9() throws Throwable {
        byte[] b = new byte[80];
        BitSet result = ISOUtil.hex2BitSet(b, 0, 100);
        assertEquals(256, result.size(), "result.size()");
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[19];
        b[14] = (byte) 65;
        try {
            ISOUtil.hex2BitSet(b, 0, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("19", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 19 out of bounds for length 19", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("19", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 19 out of bounds for length 19", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("83", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 83 out of bounds for length 83", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException12() throws Throwable {
        byte[] b = new byte[19];
        try {
            ISOUtil.hex2BitSet(b, 0, 65);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("19", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 19 out of bounds for length 19", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("20", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 20 out of bounds for length 20", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[18];
        try {
            ISOUtil.hex2BitSet(b, 0, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("18", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 18 out of bounds for length 18", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2BitSet(b, 0, 65);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2BitSet(b, 0, 63);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException6() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2BitSet(b, 0, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException7() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.hex2BitSet(b, 0, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException8() throws Throwable {
        byte[] b = new byte[2];
        try {
            ISOUtil.hex2BitSet(b, 100, false);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("100", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 100 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2BitSetThrowsArrayIndexOutOfBoundsException9() throws Throwable {
        byte[] b = new byte[0];
        try {
            ISOUtil.hex2BitSet(b, 100, true);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("100", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 100 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("bitIndex < 0: -28", ex.getMessage(), "ex.getMessage()");
            assertEquals(128, bmap.size(), "bmap.size()");
        }
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hex2BitSet(null, 100, true);
        });
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hex2BitSet(null, 100, 63);
        });
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException2() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hex2BitSet(null, 100, 65);
        });
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException3() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            byte[] b = new byte[2];
            ISOUtil.hex2BitSet(null, b, 100);
        });
    }

    @Test
    public void testHex2BitSetThrowsNullPointerException4() throws Throwable {
        BitSet bmap = new BitSet();
        try {
            ISOUtil.hex2BitSet(bmap, null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"b\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(64, bmap.size(), "bmap.size()");
        }
    }

    @Test
    public void testHex2byte() throws Throwable {
        byte[] b = new byte[3];
        byte[] result = ISOUtil.hex2byte(b, 0, 1);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) -1, result[0], "result[0]");
    }

    @Test
    public void testHex2byte1() throws Throwable {
        byte[] b = new byte[3];
        byte[] result = ISOUtil.hex2byte(b, 100, 0);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testHex2byte2() throws Throwable {
        byte[] result = ISOUtil.hex2byte("");
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testHex2byte3() throws Throwable {
        byte[] result = ISOUtil.hex2byte("testISOUtils");
        assertEquals(6, result.length, "result.length");
        assertEquals((byte) -2, result[0], "result[0]");
    }

    @Test
    public void testHex2byteThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hex2byte(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHex2byteThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(NegativeArraySizeException.class, () -> {
            byte[] b = new byte[1];
            ISOUtil.hex2byte(b, 100, -1);
        });
    }

    @Test
    public void testHex2byteThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hex2byte(null, 100, 1000);
        });
    }

    @Test
    public void testHex2byteThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hex2byte(null);
        });
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
        assertEquals("0000  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep
                + "0010  7F                                                ." + lineSep, result, "result");
    }

    @Test
    public void testHexdump1() throws Throwable {
        byte[] b = new byte[34];
        b[17] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 32);
        assertEquals("0000  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep
                + "0010  00 1F 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep, result, "result");
    }

    @Test
    public void testHexdump10() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) 32;
        String result = ISOUtil.hexdump(b);
        assertEquals("0000  00 20                                             . " + lineSep, result, "result");
    }

    @Test
    public void testHexdump11() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 127;
        String result = ISOUtil.hexdump(b);
        assertEquals("0000  00 7F 00                                          ..." + lineSep, result, "result");
    }

    @Test
    public void testHexdump12() throws Throwable {
        byte[] b = new byte[12];
        b[8] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("0000  00 00 00 00 00 00 00 00  20 00                    ........ ." + lineSep, result, "result");
    }

    @Test
    public void testHexdump13() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.hexdump(b);
        assertEquals("", result, "result");
    }

    @Test
    public void testHexdump14() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 126;
        String result = ISOUtil.hexdump(b);
        assertEquals("0000  7E 00                                             ~." + lineSep, result, "result");
    }

    @Test
    public void testHexdump15() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 33;
        String result = ISOUtil.hexdump(b);
        assertEquals("0000  21                                                !" + lineSep, result, "result");
    }

    @Test
    public void testHexdump16() throws Throwable {
        byte[] b = new byte[2];
        b[0] = (byte) 31;
        String result = ISOUtil.hexdump(b);
        assertEquals("0000  1F 00                                             .." + lineSep, result, "result");
    }

    @Test
    public void testHexdump17() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 3);
        assertEquals("0000  00 1F 00                                          ..." + lineSep, result, "result");
    }

    @Test
    public void testHexdump18() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        b[5] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 17);
        assertEquals("0000  7F 20 00 00 00 1F 00 00  00 00 00 00 00 00 00 00  . .............." + lineSep
                + "0010  00                                                ." + lineSep, result, "result");
    }

    @Test
    public void testHexdump19() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 2);
        assertEquals("0000  7F 20                                             . " + lineSep, result, "result");
    }

    @Test
    public void testHexdump2() throws Throwable {
        byte[] b = new byte[19];
        b[1] = (byte) 32;
        b[5] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 17);
        assertEquals("0000  00 20 00 00 00 1F 00 00  00 00 00 00 00 00 00 00  . .............." + lineSep
                + "0010  00                                                ." + lineSep, result, "result");
    }

    @Test
    public void testHexdump20() throws Throwable {
        byte[] b = new byte[1];
        b[0] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 1);
        assertEquals("0000  20                                                 " + lineSep, result, "result");
    }

    @Test
    public void testHexdump21() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        String result = ISOUtil.hexdump(b, 0, 3);
        assertEquals("0000  7F 20 00                                          . ." + lineSep, result, "result");
    }

    @Test
    public void testHexdump22() throws Throwable {
        byte[] b = new byte[18];
        b[14] = (byte) 46;
        String result = ISOUtil.hexdump(b, 10,5);
        assertEquals("0000  00 00 00 00 2E                                    ....." + lineSep, result, "result");
    }

    @Test
    public void testHexdump23() throws Throwable {
        byte[] b = new byte[12];
        b[6] = (byte) -48;
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("0000  00 00 00 00 00 00 D0 00  00 00                    .........." + lineSep, result, "result");
    }

    @Test
    public void testHexdump3() throws Throwable {
        byte[] b = new byte[2];
        String result = ISOUtil.hexdump(b, 100, 0);
        assertEquals("", result, "result");
    }

    @Test
    public void testHexdump4() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) -4;
        String result = ISOUtil.hexdump(b, 1, 1);
        assertEquals("0000  FC                                                ." + lineSep, result, "result");
    }

    @Test
    public void testHexdump5() throws Throwable {
        byte[] b = new byte[19];
        b[0] = (byte) 127;
        b[1] = (byte) 32;
        b[5] = (byte) 31;
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("0000  7F 20 00 00 00 1F 00 00  00 00                    . ........" + lineSep, result, "result");
    }

    @Test
    public void testHexdump6() throws Throwable {
        byte[] b = new byte[34];
        String result = ISOUtil.hexdump(b, 0, 10);
        assertEquals("0000  00 00 00 00 00 00 00 00  00 00                    .........." + lineSep, result, "result");
    }

    @Test
    public void testHexdump7() throws Throwable {
        byte[] b = new byte[10];
        b[7] = (byte) -2;
        String result = ISOUtil.hexdump(b, 7, 1);
        assertEquals("0000  FE                                                ." + lineSep, result, "result");
    }

    @Test
    public void testHexdump8() throws Throwable {
        byte[] b = new byte[3];
        b[1] = (byte) 31;
        b[2] = (byte) -48;
        String result = ISOUtil.hexdump(b, 0, 3);
        assertEquals("0000  00 1F D0                                          ..." + lineSep, result, "result");
    }

    @Test
    public void testHexdump9() throws Throwable {
        byte[] b = new byte[34];
        b[16] = (byte) 127;
        String result = ISOUtil.hexdump(b, 0, 32);
        assertEquals("0000  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep
                + "0010  7F 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................" + lineSep, result, "result");
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("19", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 19 out of bounds for length 19", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("34", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 34 out of bounds for length 34", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHexdumpThrowsArrayIndexOutOfBoundsException10() throws Throwable {
        byte[] b = new byte[2];
        try {
            ISOUtil.hexdump(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("10", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 10 out of bounds for length 10", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("9", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 9 out of bounds for length 9", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("18", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 18 out of bounds for length 18", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("12", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 12 out of bounds for length 12", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("9", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 9 out of bounds for length 9", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHexdumpThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hexdump(null, 100, 1000);
        });
    }

    @Test
    public void testHexdumpThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hexdump(null);
        });
    }

    @Test
    public void testHexor() throws Throwable {
        String result = ISOUtil.hexor("", "");
        assertEquals("", result, "result");
    }

    @Test
    public void testHexor1() throws Throwable {
        String result = ISOUtil.hexor("testISOUtilOp1", "testISOUtilOp2");
        assertEquals("00000000000003", result, "result");
    }

    @Test
    public void testHexString() throws Throwable {
        byte[] b = new byte[2];
        String result = ISOUtil.hexString(b, 0, 1);
        assertEquals("00", result, "result");
    }

    @Test
    public void testHexString1() throws Throwable {
        byte[] b = new byte[3];
        String result = ISOUtil.hexString(b, 100, 0);
        assertEquals("", result, "result");
    }

    @Test
    public void testHexString2() throws Throwable {
        byte[] b = new byte[1];
        String result = ISOUtil.hexString(b);
        assertEquals("00", result, "result");
    }

    @Test
    public void testHexString3() throws Throwable {
        byte[] b = new byte[0];
        String result = ISOUtil.hexString(b);
        assertEquals("", result, "result");
    }

    @Test
    public void testHexStringThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[3];
        try {
            ISOUtil.hexString(b, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("100", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 100 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHexStringThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[1];
        try {
            ISOUtil.hexString(b, 0, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testHexStringThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(NegativeArraySizeException.class, () -> {
            byte[] b = new byte[1];
            ISOUtil.hexString(b, 100, -1);
        });
    }

    @Test
    public void testHexStringThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hexString(null, 100, 1000);
        });
    }

    @Test
    public void testHexStringThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.hexString(null);
        });
    }

    @Test
    public void testIsAlphaNumeric() throws Throwable {
        boolean result = ISOUtil.isAlphaNumeric("testISOUtil\rs");
        assertFalse(result, "result");
    }

    @Test
    public void testIsAlphaNumeric1() throws Throwable {
        boolean result = ISOUtil.isAlphaNumeric(")\r3\u001F,\u0011-\u0005\u000FU\u000E5M2)\u0017h\u0011&ln" + lineSep
                + "G4@\u0015\u000272A\u001D9&qW\u001An|YP");
        assertFalse(result, "result");
    }

    @Test
    public void testIsAlphaNumericThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.isAlphaNumeric(null);
        });
    }

    @Test
    public void testIsAlphaNumericThrowsStringIndexOutOfBoundsException() throws Throwable {
        try {
            ISOUtil.isAlphaNumeric(" ");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("String index out of range: 1", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testIsAlphaNumericThrowsStringIndexOutOfBoundsException1() throws Throwable {
        try {
            ISOUtil.isAlphaNumeric("");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("String index out of range: 0", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testIsAlphaNumericThrowsStringIndexOutOfBoundsException2() throws Throwable {
        try {
            ISOUtil.isAlphaNumeric("testISOUtils");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("String index out of range: 12", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testIsBlank() throws Throwable {
        boolean result = ISOUtil.isBlank("");
        assertTrue(result, "result");
    }

    @Test
    public void testIsBlank1() throws Throwable {
        boolean result = ISOUtil.isBlank("1");
        assertFalse(result, "result");
    }

    @Test
    public void testIsBlankThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.isBlank(null);
        });
    }

    @Test
    public void testIsNumeric() throws Throwable {
        boolean result = ISOUtil.isNumeric("10Characte", 32);
        assertTrue(result, "result");
    }

    @Test
    public void testIsNumeric1() throws Throwable {
        boolean result = ISOUtil.isNumeric("testISOUtils", 100);
        assertFalse(result, "result");
    }

    @Test
    public void testIsNumeric2() throws Throwable {
        boolean result = ISOUtil.isNumeric("testISOUtil\rs", 32);
        assertFalse(result, "result");
    }

    @Test
    public void testIsNumeric3() throws Throwable {
        boolean result = ISOUtil.isNumeric("", 100);
        assertFalse(result, "result");
    }

    @Test
    public void testIsNumericThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.isNumeric(null, 100);
        });
    }

    @Test
    public void testIsZero() throws Throwable {
        boolean result = ISOUtil.isZero("0 q");
        assertFalse(result, "result");
    }

    @Test
    public void testIsZero1() throws Throwable {
        boolean result = ISOUtil.isZero("000");
        assertTrue(result, "result");
    }

    @Test
    public void testIsZero2() throws Throwable {
        boolean result = ISOUtil.isZero("testISOUtils");
        assertFalse(result, "result");
    }

    @Test
    public void testIsZero3() throws Throwable {
        boolean result = ISOUtil.isZero("");
        assertTrue(result, "result");
    }

    @Test
    public void testIsZeroThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.isZero(null);
        });
    }

    @Test
    public void testNormalize() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "S\u0002\"m\u0011Ap}q\\!\u0005\u0010$:\"X\u0008FM\u00028\u000E&CZ#\"\"]'\u0008k\"\u000B$jT>L\u0017-<3z\u0000%E<\u0011\"j\u001F\u0014\u001C\fR\u00138\r\">\u0016<T7l\u0002]oM|w\"$4*ks{~Cqqx7fW^^zi}gV;cY<A=ylA%DR-9",
                        true);
        assertEquals(
                "S\\u0002&quot;m\\u0011Ap}q\\!\\u0005\\u0010$:&quot;X\\u0008FM\\u00028\\u000e&amp;CZ#&quot;&quot;]&apos;\\u0008k&quot;\\u000b$jT&gt;L\\u0017-&lt;3z\\u0000%E&lt;\\u0011&quot;j\\u001f\\u0014\\u001c\\u000cR\\u00138\\u000d&quot;&gt;\\u0016&lt;T7l\\u0002]oM|w&quot;$4*ks{~Cqqx7fW^^zi}gV;cY&lt;A=ylA%DR-9",
          result, "result");

    }

    @Test
    public void testNormalize1() throws Throwable {
        String result = ISOUtil.normalize("testISOUtil\rs", true);
        assertEquals("testISOUtil\\u000ds", result, "result");
    }

    @Test
    public void testNormalize10() throws Throwable {
        String result = ISOUtil.normalize(null, true);
        assertEquals("", result, "result");
    }

    @Test
    public void testNormalize11() throws Throwable {
        String result = ISOUtil.normalize("", true);
        assertEquals("", result, "result");
    }

    @Test
    public void testNormalize12() throws Throwable {
        String result = ISOUtil.normalize("\u5BC7<t2e76HTO- 63;TwFix", true);
        assertEquals("\u5BC7&lt;t2e76HTO- 63;TwFix", result, "result");
    }

    @Test
    public void testNormalize13() throws Throwable {
        String result = ISOUtil.normalize("\u0014", true);
        assertEquals("\\u0014", result, "result");
    }

    @Test
    public void testNormalize14() throws Throwable {
        String result = ISOUtil.normalize("\u7D79\u0001#h<Fuo|s)C<D&\\J:'{ul'p\\Nz@^dt`.", true);
        assertEquals("\u7D79\\u0001#h&lt;Fuo|s)C&lt;D&amp;\\J:&apos;{ul&apos;p\\Nz@^dt`.", result, "result");
    }

    @Test
    public void testNormalize15() throws Throwable {
        String result = ISOUtil.normalize("&quot;", true);
        assertEquals("&amp;quot;", result, "result");
    }

    @Test
    public void testNormalize16() throws Throwable {
        String result = ISOUtil.normalize(" ");
        assertEquals(" ", result, "result");
    }

    @Test
    public void testNormalize17() throws Throwable {
        String result = ISOUtil.normalize("testISOUtil\rs");
        assertEquals("testISOUtil\\u000ds", result, "result");
    }

    @Test
    public void testNormalize18() throws Throwable {
        String result = ISOUtil.normalize("\rI\u0004\"e", true);
        assertEquals("\\u000dI\\u0004&quot;e", result, "result");
    }

    @Test
    public void testNormalize19() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "\u0014iSa4\r@\u0005/\u001Ai>iK>&%EA]\u001E\u001D:B\u000F-\u0016\u0012K=9\u001D\u0019t:&.2\"F<}Nf\u0011\u0003\u0008,\u0015=\u001D\n?\u0012\u000Eo<E\u001F\u0007\u001AC\u001C3\u0012d7:rf^l,`",
                        false);
        assertEquals(
                "\\u0014iSa4&#13;@\\u0005/\\u001ai&gt;iK&gt;&amp;%EA]\\u001e\\u001d:B\\u000f-\\u0016\\u0012K=9\\u001d\\u0019t:&amp;.2&quot;F&lt;}Nf\\u0011\\u0003\\u0008,\\u0015=\\u001d&#10;?\\u0012\\u000eo&lt;E\\u001f\\u0007\\u001aC\\u001c3\\u0012d7:rf^l,`",
                result, "result");
    }

    @Test
    public void testNormalize2() throws Throwable {
        String result = ISOUtil.normalize(" ", true);
        assertEquals(" ", result, "result");
    }

    @Test
    public void testNormalize20() throws Throwable {
        String result = ISOUtil.normalize("\rX XX", false);
        assertEquals("&#13;X XX", result, "result");
    }

    @Test
    public void testNormalize21() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "\u0004r2\u0013&mX&\u0014\u0017hT\u000E}Y!{\u0004&\u0016\u000Fb\"D\u001B\u0014Qz\u001E-&fe<\u0012]<.5\\\u0001\u0000\u001D%v\u001FraS\"mMlc\u0014\u001F\u0008Q\"\u0019&\u000E\\\u0004\u000F\t\u000F&:\u4BF6",
                        true);
        assertEquals(
                "\\u0004r2\\u0013&amp;mX&amp;\\u0014\\u0017hT\\u000e}Y!{\\u0004&amp;\\u0016\\u000fb&quot;D\\u001b\\u0014Qz\\u001e-&amp;fe&lt;\\u0012]&lt;.5\\\\u0001\\u0000\\u001d%v\\u001fraS&quot;mMlc\\u0014\\u001f\\u0008Q&quot;\\u0019&amp;\\u000e\\\\u0004\\u000f\\u0009\\u000f&amp;:\u4BF6",
                result, "result");
    }

    @Test
    public void testNormalize22() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "@|k7\u0014[\"7w\u0002'>\u0005\u001D[\u001D\fu0,\"9'K\u0007u\u0017[@\">V><uNo>\u001Eyj8>, pS5V&</sCZ \u0008dBz\"M\"%Wv)lQ)<u7>c]VKWRExFkiXlc1#'>?QU |d45\\eZR",
                        true);
        assertEquals(
                "@|k7\\u0014[&quot;7w\\u0002&apos;&gt;\\u0005\\u001d[\\u001d\\u000cu0,&quot;9&apos;K\\u0007u\\u0017[@&quot;&gt;V&gt;&lt;uNo&gt;\\u001eyj8&gt;, pS5V&amp;&lt;/sCZ \\u0008dBz&quot;M&quot;%Wv)lQ)&lt;u7&gt;c]VKWRExFkiXlc1#&apos;&gt;?QU |d45\\eZR",
                result, "result");
    }

    @Test
    public void testNormalize23() throws Throwable {
        String result = ISOUtil.normalize(">&Y/G%za1ZHz$O^bPBeB nUlTf{n9,", true);
        assertEquals("&gt;&amp;Y/G%za1ZHz$O^bPBeB nUlTf{n9,", result, "result");
    }

    @Test
    public void testNormalize24() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "iC\u0012Vi<\t< A\\`>|&\rw\u0018l\u0000d\u000F_`>\\ N8\u0016%Up\rf\u0005\u0019G>%>1Wnx;Ul0Rz}%[wn\u000E\u001C*\t>DJ,<\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                        false);
        assertEquals(
                "iC\\u0012Vi&lt;\\u0009&lt; A\\`&gt;|&amp;&#13;w\\u0018l\\u0000d\\u000f_`&gt;\\ N8\\u0016%Up&#13;f\\u0005\\u0019G&gt;%&gt;1Wnx;Ul0Rz}%[wn\\u000e\\u001c*\\u0009&gt;DJ,&lt;\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                result, "result");
    }

    @Test
    public void testNormalize3() throws Throwable {
        String result = ISOUtil.normalize("#g];unko,nsk 3<yhj>\\-)fw47&3,@p~rh[2cGi[", true);
        assertEquals("#g];unko,nsk 3&lt;yhj&gt;\\-)fw47&amp;3,@p~rh[2cGi[", result, "result");
    }

    @Test
    public void testNormalize4() throws Throwable {
        String result = ISOUtil.normalize(" XX XXXX  XX XXXX X  XXX  XX XXXXX XXXXX   XX XXXXXXXX   XX X X  \t XXX",
                true);
        assertEquals(" XX XXXX  XX XXXX X  XXX  XX XXXXX XXXXX   XX XXXXXXXX   XX X X  \\u0009 XXX", result, "result");
    }

    @Test
    public void testNormalize5() throws Throwable {
        String result = ISOUtil
                .normalize(
                        "iC\u0012Vi<\t< A\\`>|&\rw\u0018l\u0000d\u000F_`>\\ N8\u0016%Up\rf\u0005\u0019G>%>1Wnx;Ul0Rz}%[wn\u000E\u001C*\t>DJ,<\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                        true);
        assertEquals(
                "iC\\u0012Vi&lt;\\u0009&lt; A\\`&gt;|&amp;\\u000dw\\u0018l\\u0000d\\u000f_`&gt;\\ N8\\u0016%Up\\u000df\\u0005\\u0019G&gt;%&gt;1Wnx;Ul0Rz}%[wn\\u000e\\u001c*\\u0009&gt;DJ,&lt;\uDB18\uCA4B\u8FF8\u340F\u4F1F",
                result, "result");
    }

    @Test
    public void testNormalize6() throws Throwable {
        String result = ISOUtil.normalize("\rI\u0004\"e", false);
        assertEquals("&#13;I\\u0004&quot;e", result, "result");
    }

    @Test
    public void testNormalize7() throws Throwable {
        String result = ISOUtil.normalize("J\u0006YTuVP>F}R+Js:(aD", true);
        assertEquals("J\\u0006YTuVP&gt;F}R+Js:(aD", result, "result");
    }

    @Test
    public void testNormalize8() throws Throwable {
        String result = ISOUtil.normalize(">=\u0011[\u0011_f\u0019<&[", true);
        assertEquals("&gt;=\\u0011[\\u0011_f\\u0019&lt;&amp;[", result, "result");
    }

    @Test
    public void testNormalize9() throws Throwable {
        String s = "<\u0010}\"\uCD88\uD16A\u1384\uFE1A\u44B2\u2712\u3BD7\u3AE5\uFCC4\uD19B\u16F2\uD45A\u45F8\u65FF\uA224\u3930\u946E\u8FB0\u3550\u061D\u4741\u8084\u8606\u6B1A\u8F81\uC634\u0190\u2053\uFA5A\u4C3F\uD365\uF7A7\uF8D4";

        String result = ISOUtil.normalize(s, true);
        assertEquals(
                "&lt;\\u0010}&quot;\uCD88\uD16A\u1384\uFE1A\u44B2\u2712\u3BD7\u3AE5\uFCC4\uD19B\u16F2\uD45A\u45F8\u65FF\uA224\u3930\u946E\u8FB0\u3550\u061D\u4741\u8084\u8606\u6B1A\u8F81\uC634\u0190\u2053\uFA5A\u4C3F\uD365\uF7A7\uF8D4",
                result, "result");
    }

    @Test
    public void testNormalizeDenormalize() throws Throwable {
        String s = "\u0010}\uCD88\uD16A\u1384\uFE1A\u44B2\u2712\u3BD7\u3AE5\uFCC4\uD19B\u16F2\uD45A\u45F8\u65FF\uA224\u3930\u946E\u8FB0\u3550\u061D\u4741\u8084\u8606\u6B1A\u8F81\uC634\u0190\u2053\uFA5A\u4C3F\uD365\uF7A7\uF8D4";

        String result = ISOUtil.normalize(s, true);
        assertEquals(
                "\\u0010}\uCD88\uD16A\u1384\uFE1A\u44B2\u2712\u3BD7\u3AE5\uFCC4\uD19B\u16F2\uD45A\u45F8\u65FF\uA224\u3930\u946E\u8FB0\u3550\u061D\u4741\u8084\u8606\u6B1A\u8F81\uC634\u0190\u2053\uFA5A\u4C3F\uD365\uF7A7\uF8D4",
          result, "result");

        assertEquals(s, ISOUtil.stripUnicode(result), "original");
    }

    @Test
    public void testPadleft() throws Throwable {
        String result = ISOUtil.padleft("testString", 11, '2');
        assertEquals("2testString", result, "result");
    }

    @Test
    public void testPadleft1() throws Throwable {
        String result = ISOUtil.padleft("2C", 2, ' ');
        assertEquals("2C", result, "result");
    }

    @Test
    public void testPadleftThrowsISOException() throws Throwable {
        try {
            ISOUtil.padleft("testString", 0, '\u0002');
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len 10/0", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testPadleftThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.padleft(null, 0, '\u0002');
        });
    }

    @Test
    public void testParseInt() throws Throwable {
        char[] cArray = new char[2];
        cArray[0] = 'S';
        cArray[1] = 'C';
        int result = ISOUtil.parseInt(cArray, 35);
        assertEquals(992, result, "result");
    }

    @Test
    public void testParseInt1() throws Throwable {
        char[] cArray = new char[1];
        cArray[0] = '1';
        int result = ISOUtil.parseInt(cArray);
        assertEquals(1, result, "result");
    }

    @Test
    public void testParseInt2() throws Throwable {
        int result = ISOUtil.parseInt("1", 10);
        assertEquals(1, result, "result");
    }

    @Test
    public void testParseInt3() throws Throwable {
        int result = ISOUtil.parseInt("2C", 31);
        assertEquals(74, result, "result");
    }

    @Test
    public void testParseInt4() throws Throwable {
        int result = ISOUtil.parseInt("1");
        assertEquals(1, result, "result");
    }

    @Test
    public void testParseInt5() throws Throwable {
        byte[] bArray = new byte[1];
        bArray[0] = (byte) 49;
        int result = ISOUtil.parseInt(bArray);
        assertEquals(1, result, "result");
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException() throws Throwable {
        char[] cArray = new char[0];
        try {
            ISOUtil.parseInt(cArray, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        char[] cArray = new char[0];
        try {
            ISOUtil.parseInt(cArray);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] bArray = new byte[0];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testParseIntThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] bArray = new byte[0];
        try {
            ISOUtil.parseInt(bArray);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testParseIntThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.parseInt((char[]) null, 100);
        });
    }

    @Test
    public void testParseIntThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.parseInt((char[]) null);
        });
    }

    @Test
    public void testParseIntThrowsNullPointerException2() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.parseInt((String) null, 100);
        });
    }

    @Test
    public void testParseIntThrowsNullPointerException3() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.parseInt((String) null);
        });
    }

    @Test
    public void testParseIntThrowsNullPointerException4() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.parseInt((byte[]) null, 100);
        });
    }

    @Test
    public void testParseIntThrowsNullPointerException5() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.parseInt((byte[]) null);
        });
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
            assertEquals("Char array contains non-digit", ex.getMessage(), "ex.getMessage()");
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
            assertEquals("Char array contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException10() throws Throwable {
        try {
            ISOUtil.parseInt("0\"/", 10);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("String contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException11() throws Throwable {
        try {
            ISOUtil.parseInt("9Characte", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("String contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException12() throws Throwable {
        try {
            ISOUtil.parseInt("8Charact", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("String contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException13() throws Throwable {
        try {
            ISOUtil.parseInt("10Characte", 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("Number can have maximum 9 digits", ex.getMessage(), "ex.getMessage()");
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
            assertEquals("Byte array contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException3() throws Throwable {
        byte[] bArray = new byte[8];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("Byte array contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException4() throws Throwable {
        byte[] bArray = new byte[9];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("Byte array contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException5() throws Throwable {
        byte[] bArray = new byte[10];
        try {
            ISOUtil.parseInt(bArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("Number can have maximum 9 digits", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException6() throws Throwable {
        char[] cArray = new char[9];
        try {
            ISOUtil.parseInt(cArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("Char array contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException7() throws Throwable {
        char[] cArray = new char[10];
        try {
            ISOUtil.parseInt(cArray, 100);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("Number can have maximum 9 digits", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException8() throws Throwable {
        try {
            ISOUtil.parseInt("o`2L\\*@@#", 28);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("String contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsNumberFormatException9() throws Throwable {
        try {
            ISOUtil.parseInt("9Characte", 28);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("String contains non-digit", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsStringIndexOutOfBoundsException() throws Throwable {
        try {
            ISOUtil.parseInt("", 100);
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("String index out of range: 0", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testParseIntThrowsStringIndexOutOfBoundsException1() throws Throwable {
        try {
            ISOUtil.parseInt("");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("String index out of range: 0", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testProtect() throws Throwable {
        String result = ISOUtil.protect("10Characte");
        assertEquals("10Characte", result, "result");
    }

    @Test
    public void testProtect1() throws Throwable {
        String result = ISOUtil.protect("=WaW=4V0");
        assertEquals("=___=___", result, "result");
    }

    @Test
    public void testProtect10() throws Throwable {
        String result = ISOUtil.protect("=====^===========^====^===");
        assertEquals("=====^===========^====^===", result, "result");
    }

    @Test
    public void testProtect11() throws Throwable {
        String result = ISOUtil.protect("testISOUtils");
        assertEquals("testIS__tils", result, "result");
    }

    @Test
    public void testProtect12() throws Throwable {
        String result = ISOUtil.protect("=HNb^D4uZfz0@|\")61b:~dSS`[.2!!qlL4Z0");
        assertEquals("=___^D4uZfz0@|\")61b:~dSS`[.2!!qlL4Z0", result, "result");
    }

    @Test
    public void testProtect13() throws Throwable {
        String result = ISOUtil.protect("^58*(=@");
        assertEquals("^58*(=_", result, "result");
    }

    @Test
    public void testProtect14() throws Throwable {
        String result = ISOUtil.protect("===\u3455w");
        assertEquals("===__", result, "result");
    }

    @Test
    public void testProtect15() throws Throwable {
        String result = ISOUtil.protect("=\u0AC4\uC024\uF29B=~2A)~5aCgl\"lLU*lm_cJ1M/!KFnA");
        assertEquals("=___=________________________KFnA", result, "result");
    }

    @Test
    public void testProtect16() throws Throwable {
        String result = ISOUtil.protect("\u30C5\uE09B\u6028\uB54E\u2094\uFA25\uAD56\u3A1F\uE55C\u31AA\u5FE0=$");
        assertEquals("\u30C5\uE09B\u6028\uB54E\u2094\uFA25_\u3A1F\uE55C\u31AA\u5FE0=_", result, "result");
    }

    @Test
    public void testProtect17() throws Throwable {
        String result = ISOUtil.protect("+6+[=I?");
        assertEquals("+6+[=__", result, "result");
    }

    @Test
    public void testProtect18() throws Throwable {
        String result = ISOUtil.protect("=======");
        assertEquals("=======", result, "result");
    }

    @Test
    public void testProtect19() throws Throwable {
        String result = ISOUtil.protect("===^^+^=");
        assertEquals("===^^+^=", result, "result");
    }

    @Test
    public void testProtect2() throws Throwable {
        String result = ISOUtil.protect("===^===");
        assertEquals("===^===", result, "result");
    }

    @Test
    public void testProtect20() throws Throwable {
        String result = ISOUtil.protect("\u6D1D^KI");
        assertEquals("_^KI", result, "result");
    }

    @Test
    public void testProtect21() throws Throwable {
        String result = ISOUtil.protect("=7G^=^");
        assertEquals("=__^=^", result, "result");
    }

    @Test
    public void testProtect3() throws Throwable {
        String result = ISOUtil.protect("^D==N^=r=\u0002^g)==");
        assertEquals("^D==_^=_=_^g)==", result, "result");
    }

    @Test
    public void testProtect4() throws Throwable {
        String result = ISOUtil.protect("=");
        assertEquals("=", result, "result");
    }

    @Test
    public void testProtect5() throws Throwable {
        String result = ISOUtil.protect("");
        assertEquals("", result, "result");
    }

    @Test
    public void testProtect6() throws Throwable {
        String result = ISOUtil.protect("VqM_'");
        assertEquals("_____", result, "result");
    }

    @Test
    public void testProtect7() throws Throwable {
        String result = ISOUtil.protect("\\7.=^6C3");
        assertEquals("\\7.=^6C3", result, "result");
    }

    @Test
    public void testProtect8() throws Throwable {
        String result = ISOUtil.protect("#<gF=uG!");
        assertEquals("#<gF=___", result, "result");
    }

    @Test
    public void testProtect9() throws Throwable {
        String result = ISOUtil.protect("^9a{=o;G");
        assertEquals("^9a{=___", result, "result");
    }

    @Test
    public void testProtectT2D1() throws Throwable {
        String result = ISOUtil.protect("#<gFDuG!");
        assertEquals("#<gFD___", result, "result");
    }

    @Test
    public void testProtectT2D2() throws Throwable {
        String result = ISOUtil.protect("9a{#<gFuG!53Do;G");
        assertEquals("9a{#<g__G!53D___", result, "result");
    }

    @Test
    public void testProtectT1D1() throws Throwable {
        String result = ISOUtil.protect("a{#<gFuG!53o;G609^FOO/BAR COM^67890o;G");
        assertEquals("a{#<gF_______G609^FOO/BAR COM^________", result, "result");
    }

    @Test
    public void testProtectT1D2() throws Throwable {
        String result = ISOUtil.protect("9a{#<gFuG!^FOO/BAR COM^67890o;G");
        assertEquals("9a{#<gFuG!^FOO/BAR COM^________", result, "result");
    }

    @Test
    public void testProtectT1D3() throws Throwable {
        String result = ISOUtil.protect("9a{D<gFuG!^FOO/BAR COM^67890o;G");
        assertEquals("9a{D<gFuG!^FOO/BAR COM^________", result, "result");
    }

    @Test
    public void testProtectThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.protect(null);
        });
    }

    @Test
    public void testSleep() throws Throwable {
        ISOUtil.sleep(100L);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testSleepThrowsIllegalArgumentException() throws Throwable {
        try {
            ISOUtil.sleep(-30L);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("timeout value is negative", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testStr2bcd() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", true);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) -16, result[0], "result[0]");
    }

    @Test
    public void testStr2bcd1() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils", true);
        assertEquals(6, result.length, "result.length");
        assertEquals((byte) 117, result[0], "result[0]");
    }

    @Test
    public void testStr2bcd10() throws Throwable {
        byte[] result = ISOUtil.str2bcd("", true);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testStr2bcd11() throws Throwable {
        byte[] result = ISOUtil.str2bcd("", true, (byte) 0);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testStr2bcd12() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils1", true, (byte) 0);
        assertEquals(7, result.length, "result.length");
        assertEquals((byte) 68, result[0], "result[0]");
    }

    @Test
    public void testStr2bcd13() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils", true, (byte) 0);
        assertEquals(6, result.length, "result.length");
        assertEquals((byte) 117, result[0], "result[0]");
    }

    @Test
    public void testStr2bcd14() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", true, (byte) 0);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) -16, result[0], "result[0]");
    }

    @Test
    public void testStr2bcd15() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", false, (byte) 0);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testStr2bcd16() throws Throwable {
        byte[] result = ISOUtil.str2bcd("testISOUtils1", false, (byte) 0);
        assertEquals(7, result.length, "result.length");
        assertEquals((byte) 117, result[0], "result[0]");
    }

    @Test
    public void testStr2bcd2() throws Throwable {
        byte[] d = new byte[0];
        byte[] result = ISOUtil.str2bcd("", true, d, 100);
        assertSame(d, result, "result");
    }

    @Test
    public void testStr2bcd3() throws Throwable {
        byte[] d = new byte[1];
        byte[] result = ISOUtil.str2bcd("", true, d, 100);
        assertSame(d, result, "result");
        assertEquals((byte) 0, d[0], "d[0]");
    }

    @Test
    public void testStr2bcd4() throws Throwable {
        byte[] d = new byte[2];
        byte[] result = ISOUtil.str2bcd("3Ch", true, d, 0);
        assertEquals((byte) 3, d[0], "d[0]");
        assertSame(d, result, "result");
    }

    @Test
    public void testStr2bcd5() throws Throwable {
        byte[] d = new byte[2];
        byte[] result = ISOUtil.str2bcd(" ", false, d, 0);
        assertSame(d, result, "result");
        assertEquals((byte) 0, d[0], "d[0]");
    }

    @Test
    public void testStr2bcd6() throws Throwable {
        byte[] d = new byte[69];
        byte[] result = ISOUtil.str2bcd("testISOUtils1", false, d, 0);
        assertEquals((byte) 117, d[0], "d[0]");
        assertSame(d, result, "result");
    }

    @Test
    public void testStr2bcd7() throws Throwable {
        byte[] d = new byte[2];
        byte[] result = ISOUtil.str2bcd(" ", true, d, 0);
        assertEquals((byte) -16, d[0], "d[0]");
        assertSame(d, result, "result");
    }

    @Test
    public void testStr2bcd8() throws Throwable {
        byte[] d = new byte[4];
        byte[] result = ISOUtil.str2bcd("2C", true, d, 0);
        assertEquals((byte) 51, d[0], "d[0]");
        assertSame(d, result, "result");
    }

    @Test
    public void testStr2bcd9() throws Throwable {
        byte[] result = ISOUtil.str2bcd(" ", false);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] d = new byte[2];
        try {
            ISOUtil.str2bcd("testISOUtils1", true, d, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals((byte) 68, d[0], "d[0]");
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
            assertEquals((byte) 117, d[0], "d[0]");
            // assertEquals("ex.getMessage()", "2", ex.getMessage());
        }
    }

    @Test
    public void str2bcdRightPadF() {
        byte[] d = ISOUtil.str2bcd("123", false, (byte) 0xF);
        assertEquals("123F", ISOUtil.hexString(d));
    }

    @Test
    public void str2bcdLeftPadF() {
        byte[] d = ISOUtil.str2bcd("123", true, (byte) 0xF);
        assertEquals("F123", ISOUtil.hexString(d));
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] d = new byte[2];
        try {
            ISOUtil.str2bcd("testISOUtils", true, d, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals((byte) 117, d[0], "d[0]");
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
            assertEquals((byte) 68, d[0], "d[0]");
        }
    }

    @Test
    public void testStr2bcdThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] d = new byte[2];
            ISOUtil.str2bcd("testISOUtils1", false, d, 100);
        });
    }

    @Test
    public void testStr2bcdThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.str2bcd(null, true);
        });
    }

    @Test
    public void testStr2bcdThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.str2bcd(null, true, (byte) 0);
        });
    }

    @Test
    public void testStr2bcdThrowsNullPointerException2() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.str2bcd("testISOUtils1", true, null, 100);
        });
    }

    @Test
    public void testStr2bcdThrowsNullPointerException3() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.str2bcd("testISOUtils", true, null, 100);
        });
    }

    @Test
    public void testStr2bcdThrowsNullPointerException4() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            byte[] d = new byte[2];
            ISOUtil.str2bcd(null, true, d, 100);
        });
    }

    @Test
    public void testStrpad() throws Throwable {
        String result = ISOUtil.strpad("testISOUtils", 0);
        assertEquals("testISOUtils", result, "result");
    }

    @Test
    public void testStrpad1() throws Throwable {
        String result = ISOUtil.strpad("testISOUtils", 100);
        assertEquals(
                "testISOUtils                                                                                        ",
                result, "result");
    }

    @Test
    public void testStrpadf() throws Throwable {
        String result = ISOUtil.strpadf("testISOUtils", 0);
        assertEquals("testISOUtils", result, "result");
    }

    @Test
    public void testStrpadf1() throws Throwable {
        String result = ISOUtil.strpadf("", 100);
        assertEquals(
                "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
                result, "result");
    }

    @Test
    public void testStrpadfThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.strpadf(null, 100);
        });
    }

    @Test
    public void testStrpadThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.strpad(null, 100);
        });
    }

    @Test
    public void testToIntArray() throws Throwable {
        int[] result = ISOUtil.toIntArray("42");
        assertEquals(1, result.length, "result.length");
        assertEquals(42, result[0], "result[0]");
    }

    @Test
    public void testToIntArray1() throws Throwable {
        int[] result = ISOUtil.toIntArray("");
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testToIntArrayThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.toIntArray(null);
        });
    }

    @Test
    public void testToIntArrayThrowsNumberFormatException() throws Throwable {
        try {
            ISOUtil.toIntArray("testISOUtils");
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("For input string: \"testISOUtils\"", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testTrim() throws Throwable {
        byte[] array = new byte[2];
        byte[] result = ISOUtil.trim(array, 1);
        assertEquals(1, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testTrim1() throws Throwable {
        byte[] array = new byte[2];
        byte[] result = ISOUtil.trim(array, 0);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testTrim2() throws Throwable {
        String result = ISOUtil.trim("testISOUtils");
        assertEquals("testISOUtils", result, "result");
    }

    @Test
    public void testTrimNullReturnsNull() throws Throwable {
        String result = ISOUtil.trim(null);
        assertNull(result, "result");
    }

    @Test
    public void testTrimf() throws Throwable {
        String result = ISOUtil.trimf("");
        assertEquals("", result, "result");
    }

    @Test
    public void testTrimf1() throws Throwable {
        String result = ISOUtil.trimf("2C");
        assertEquals("2C", result, "result");
    }

    @Test
    public void testTrimf2() throws Throwable {
        String result = ISOUtil.trimf("FF");
        assertEquals("", result, "result");
    }

    @Test
    public void testTrimf3() throws Throwable {
        String result = ISOUtil.trimf("F");
        assertEquals("", result, "result");
    }

    @Test
    public void testTrimf4() throws Throwable {
        String result = ISOUtil.trimf(" ");
        assertEquals("", result, "result");
    }

    @Test
    public void testTrimf5() throws Throwable {
        String result = ISOUtil.trimf(null);
        assertNull(result, "result");
    }

    @Test
    public void testTrimThrowsArrayIndexOutOfBoundsException() throws Throwable {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] array = new byte[2];
            ISOUtil.trim(array, 100);
        });
    }

    @Test
    public void testTrimThrowsNegativeArraySizeException() throws Throwable {
        assertThrows(NegativeArraySizeException.class, () -> {
            byte[] array = new byte[3];
            ISOUtil.trim(array, -1);
        });
    }

    @Test
    public void testTrimThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.trim(null, 100);
        });
    }

    @Test
    public void testUnPadLeft() throws Throwable {
        String result = ISOUtil.unPadLeft("", ' ');
        assertEquals("", result, "result");
    }

    @Test
    public void testUnPadLeft1() throws Throwable {
        String result = ISOUtil.unPadLeft("", '\u001F');
        assertEquals("", result, "result");
    }

    @Test
    public void testUnPadLeft3() throws Throwable {
        String result = ISOUtil.unPadLeft("testISOUtils", 't');
        assertEquals("estISOUtils", result, "result");
    }

    @Test
    public void testUnPadLeftThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.unPadLeft(null, ' ');
        });
    }

    @Test
    public void testUnPadRight() throws Throwable {
        String result = ISOUtil.unPadRight("f", 'f');
        assertEquals("f", result, "result");
    }

    @Test
    public void testUnPadRight1() throws Throwable {
        String result = ISOUtil.unPadRight("", ' ');
        assertEquals("", result, "result");
    }

    @Test
    public void testUnPadRight2() throws Throwable {
        String result = ISOUtil.unPadRight("", 'A');
        assertEquals("", result, "result");
    }

    @Test
    public void testUnPadRight3() throws Throwable {
        String result = ISOUtil.unPadRight("f", ' ');
        assertEquals("f", result, "result");
    }

    @Test
    public void testUnPadRight4() throws Throwable {
        String result = ISOUtil.unPadRight("  &ON\\.!Wio=p^'@*xS'*ItLh|_g[,K2H|FkD]RPGQ", 'Q');
        assertEquals("  &ON\\.!Wio=p^'@*xS'*ItLh|_g[,K2H|FkD]RPG", result, "result");
    }

    @Test
    public void testUnPadRightThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.unPadRight(null, ' ');
        });
    }

    @Test
    public void testXor() throws Throwable {
        byte[] op2 = new byte[0];
        byte[] result = ISOUtil.xor(ISOUtil.str2bcd("testISOUtils", true), op2);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testXor1() throws Throwable {
        byte[] op2 = new byte[4];
        byte[] op1 = new byte[0];
        byte[] result = ISOUtil.xor(op1, op2);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testXor2() throws Throwable {
        byte[] op1 = new byte[3];
        byte[] op2 = new byte[2];
        byte[] result = ISOUtil.xor(op1, op2);
        assertEquals(2, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testXor3() throws Throwable {
        byte[] op1 = new byte[3];
        byte[] op2 = new byte[5];
        byte[] result = ISOUtil.xor(op1, op2);
        assertEquals(3, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testXorThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            byte[] op2 = new byte[0];
            ISOUtil.xor(null, op2);
        });
    }

    @Test
    public void testZeropad() throws Throwable {
        String result = ISOUtil.zeropad("testISOUtils", 100);
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000testISOUtils",
                result, "result");
    }

    @Test
    public void testZeropadRight() throws Throwable {
        String result = ISOUtil.zeropadRight("testISOUtils", 0);
        assertEquals("testISOUtils", result, "result");
    }

    @Test
    public void testZeropadRight1() throws Throwable {
        String result = ISOUtil.zeropadRight("", 100);
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                result, "result");
    }

    @Test
    public void testZeropadRightThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.zeropadRight(null, 100);
        });
    }

    @Test
    public void testZeropadThrowsISOException() throws Throwable {
        try {
            ISOUtil.zeropad("testISOUtils", 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len 12/0", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testZeropadThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.zeropad(null, 100);
        });
    }

    @Test
    public void testZeroUnPad1() throws Throwable {
        String result = ISOUtil.zeroUnPad("");
        assertEquals("", result, "result");
    }

    @Test
    public void testZeroUnPadThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            ISOUtil.zeroUnPad(null);
        });
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
        BigDecimal result = new BigDecimal(ISOUtil.parseAmountConversionRate(rate), MathContext.DECIMAL64);
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
            assertEquals("Invalid amount converion rate argument: '" + rate + "'", ex.getMessage());
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
            assertEquals("Invalid amount converion rate argument: '" + rate + "'", ex.getMessage());
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
            assertEquals("Invalid amount converion rate argument: '" + rate + "'", ex.getMessage());
        }
    }

    /**
     * @see org.jpos.iso.ISOUtil#commaEncode(String[])
     * @see org.jpos.iso.ISOUtil#commaDecode(String)
     */
    @Test
    public void testCommaEncodeAndDecode() {
        assertEquals("", ISOUtil.commaEncode(new String[] {}), "error encoding \"\"");
        assertEquals("a,b,c", ISOUtil.commaEncode(new String[] { "a", "b", "c" }), "error encoding \"a,b,c\"");
        assertEquals("\\,,\\\\,c", ISOUtil.commaEncode(new String[] { ",", "\\", "c" }),
                "error encoding \"\\,,\\\\,c\"");

        assertArrayEquals(new String[] {}, ISOUtil.commaDecode(""), "error decoding \"\"");
        assertArrayEquals(new String[] { "a", "b", "c" }, ISOUtil.commaDecode("a,b,c"), "error decoding \"a,b,c\"");
        assertArrayEquals(new String[] { ",", "\\", "c" }, ISOUtil.commaDecode("\\,,\\\\,c"),
                "error decoding \"\\,,\\\\,c\"");
    }

    @Test
    public void testMillisToString() {
        Calendar cal = new GregorianCalendar(2012, Calendar.JUNE, 29, 10, 51, 47);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.MILLISECOND, 16);
        String result = ISOUtil.millisToString(cal.getTimeInMillis());
        assertThat(result, is("15520d 10:51:47.016"));
    }

    @Test
    public void testTakeFirstN() throws Exception {
        String result = ISOUtil.takeFirstN("abcdefgh", 3);
        assertThat(result, is("abc"));
    }

    @Test
    public void testTakeFirstNAndPad() throws Exception {
        String result = ISOUtil.takeFirstN("abc", 5);
        assertThat(result, is("00abc"));
    }

    @Test
    public void testTakeFirstNequal() throws Exception {
        String result = ISOUtil.takeFirstN("abc", 3);
        assertThat(result, is("abc"));
    }

    @Test
    public void testTakeLastN() throws Exception {
        String result = ISOUtil.takeLastN("abcdefgh", 3);
        assertThat(result, is("fgh"));
    }

    @Test
    public void testTakeLastNAndPad() throws Exception {
        String result = ISOUtil.takeLastN("abc", 5);
        assertThat(result, is("00abc"));
    }

    @Test
    public void testTakeLastNequal() throws Exception {
        String result = ISOUtil.takeLastN("abc", 3);
        assertThat(result, is("abc"));
    }

    @Test
    public void testToStringArray() throws Exception {
        String[] result = ISOUtil.toStringArray("a\tb\nc\rd\fe");
        assertThat(
                result,
                allOf(hasItemInArray("a"), hasItemInArray("b"), hasItemInArray("c"), hasItemInArray("d"),
                        hasItemInArray("e")));
    }

    @Test
    public void testcalcLUHN() throws Exception {
        char check = ISOUtil.calcLUHN("411111111111111");
        assertThat(check, is('1'));
    }

    @Test
    public void testEbcdicCharSet() throws Throwable {
        byte[] b = new byte[256];
        for (int i=0; i<b.length; i++) {
            b[i] = ((byte) (i & 0xFF));
        }
        String s = new String (b, ISOUtil.CHARSET);
        byte[] ebcdic = ISOUtil.asciiToEbcdic(s);
        byte[] ascii = ISOUtil.ebcdicToAsciiBytes(ebcdic);
        assertArrayEquals(b, ascii, "arrays should be equal");

        Charset c = Charset.forName("IBM1047");
        byte[] ebcdic1 = c.encode(s).array();
        String s1 = c.decode(ByteBuffer.wrap(ebcdic1)).toString();
        assertArrayEquals(ebcdic, ebcdic1, "arrays should match");

        Assertions.assertEquals (s, s1, "ASCII strings should be the same");
        Assertions.assertArrayEquals (b, s1.getBytes(ISOUtil.CHARSET), "byte[] should be the same as s1");
        Assertions.assertArrayEquals (b, new String(ascii, ISOUtil.CHARSET).getBytes(ISOUtil.CHARSET), "byte[] should be the same as ascii");
    }

    @Test
    public void testStringDLE() {
        byte[] b = new byte[32];
        for (int i=0; i<32; i++)
            b[i] = (byte) i;
        String s = "The quick brown fox " + new String(b, ISOUtil.CHARSET) + " jumps over the lazy dog";
        String normalized = ISOUtil.normalize (s);
        String stripped = ISOUtil.stripUnicode(normalized);
        assertEquals(s, stripped);
    }
}
