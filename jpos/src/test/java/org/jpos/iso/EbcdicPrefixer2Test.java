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

public class EbcdicPrefixer2Test {

    @Test
    public void testConstructor() throws Throwable {
        EbcdicPrefixer ebcdicPrefixer = new EbcdicPrefixer(100);
        assertEquals(100, ebcdicPrefixer.getPackedLength(), "ebcdicPrefixer.getPackedLength()");
    }

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] b = new byte[1];
        int result = new EbcdicPrefixer(0).decodeLength(b, 100);
        assertEquals(0, result, "result");
    }

    @Test
    public void testDecodeLength1() throws Throwable {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) -14;
        int result = EbcdicPrefixer.LL.decodeLength(bytes, 0);
        assertEquals(20, result, "result");
    }

    @Test
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[0];
        try {
            new EbcdicPrefixer(100).decodeLength(b, 100);
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
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[50];
        try {
            new EbcdicPrefixer(100).decodeLength(b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("50", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 50 out of bounds for length 50", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testDecodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new EbcdicPrefixer(100).decodeLength(null, 100);
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
        byte[] b = new byte[4];
        new EbcdicPrefixer(1).encodeLength(100, b);
        assertEquals((byte) -16, b[0], "b[0]");
    }

    @Test
    public void testEncodeLength1() throws Throwable {
        byte[] b = new byte[0];
        new EbcdicPrefixer(0).encodeLength(100, b);
        assertEquals(0, b.length, "b.length");
    }

    @Test
    public void testEncodeLength2() throws Throwable {
        byte[] bytes = new byte[2];
        EbcdicPrefixer.LL.encodeLength(100, bytes);
        assertEquals((byte) -16, bytes[0], "bytes[0]");
    }

    @Test
    public void testEncodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[2];
        try {
            new EbcdicPrefixer(2).encodeLength(-10, b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals((byte) -16, b[1], "b[1]");
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 10", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(2, b.length, "b.length");
        }
    }

    @Test
    public void testEncodeLengthThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[1];
        try {
            new EbcdicPrefixer(2).encodeLength(100, b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 1", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, b.length, "b.length");
        }
    }

    @Test
    public void testEncodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new EbcdicPrefixer(2).encodeLength(100, null);
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
        int result = new EbcdicPrefixer(100).getPackedLength();
        assertEquals(100, result, "result");
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new EbcdicPrefixer(0).getPackedLength();
        assertEquals(0, result, "result");
    }
}
