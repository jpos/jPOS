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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import org.junit.jupiter.api.Test;

public class AsciiPrefixer2Test {

    @Test
    public void testConstructor() throws Throwable {
        AsciiPrefixer asciiPrefixer = new AsciiPrefixer(100);
        assertEquals(100, asciiPrefixer.getPackedLength(), "asciiPrefixer.getPackedLength()");
    }

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] bytes = new byte[2];
        try {
            int result = AsciiPrefixer.LL.decodeLength(bytes, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Invalid character found. Expected digit.", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testDecodeLength1() throws Throwable {
        byte[] b = new byte[1];
        int result = new AsciiPrefixer(0).decodeLength(b, 100);
        assertEquals(0, result, "result");
    }

    @Test
    public void testDecodeBadLength1DigitThrowsISOException() throws Throwable {
        byte[] bytes = new byte[] {'9'+1};
        try {
            int result = AsciiPrefixer.L.decodeLength(bytes, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Invalid character found. Expected digit.", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testDecodeBadLength2DigitsDoesntFail() throws Throwable {
        byte[] bytes = new byte[] {'1', '0'-1, '0'};
        try {
            int result = AsciiPrefixer.LLL.decodeLength(bytes, 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Invalid character found. Expected digit.", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[] {'0', '0', '0'};
        try {
            new AsciiPrefixer(100).decodeLength(b, 0);
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
    public void testDecodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new AsciiPrefixer(100).decodeLength(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot load from byte/boolean array because \"b\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testEncodeLength() throws Throwable {
        byte[] b = new byte[3];
        new AsciiPrefixer(-1).encodeLength(0, b);
        assertEquals(3, b.length, "b.length");
    }

    @Test
    public void testEncodeLength1() throws Throwable {
        byte[] bytes = new byte[2];
        AsciiPrefixer.LL.encodeLength(0, bytes);
        assertEquals((byte) 48, bytes[0], "bytes[0]");
    }

    @Test
    public void testEncodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[0];
        try {
            new AsciiPrefixer(1).encodeLength(100, b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, b.length, "b.length");
        }
    }

    @Test
    public void testEncodeLengthThrowsISOException() throws Throwable {
        byte[] bytes = new byte[2];
        try {
            AsciiPrefixer.LL.encodeLength(100, bytes);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals((byte) 48, bytes[0], "bytes[0]");
            assertEquals("invalid len 100. Prefixing digits = 2", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testEncodeLengthThrowsISOException1() throws Throwable {
        byte[] b = new byte[0];
        try {
            new AsciiPrefixer(0).encodeLength(1, b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len 1. Prefixing digits = 0", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertEquals(0, b.length, "b.length");
        }
    }

    @Test
    public void testEncodeLengthThrowsISOException2() throws Throwable {
        byte[] b = new byte[0];
        try {
            new AsciiPrefixer(0).encodeLength(-1, b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len -1. Prefixing digits = 0", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
            assertEquals(0, b.length, "b.length");
        }
    }

    @Test
    public void testEncodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new AsciiPrefixer(2).encodeLength(100, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot store to byte/boolean array because \"b\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new AsciiPrefixer(0).getPackedLength();
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new AsciiPrefixer(100).getPackedLength();
        assertEquals(100, result, "result");
    }
}
