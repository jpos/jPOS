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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class AsciiPrefixer2Test {

    @Test
    public void testConstructor() throws Throwable {
        AsciiPrefixer asciiPrefixer = new AsciiPrefixer(100);
        assertEquals("asciiPrefixer.getPackedLength()", 100, asciiPrefixer.getPackedLength());
    }

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] bytes = new byte[2];
        int result = AsciiPrefixer.LL.decodeLength(bytes, 0);
        assertEquals("result", -528, result);
    }

    @Test
    public void testDecodeLength1() throws Throwable {
        byte[] b = new byte[1];
        int result = new AsciiPrefixer(0).decodeLength(b, 100);
        assertEquals("result", 0, result);
    }

    @Test
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[3];
        try {
            new AsciiPrefixer(100).decodeLength(b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testDecodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new AsciiPrefixer(100).decodeLength((byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEncodeLength() throws Throwable {
        byte[] b = new byte[3];
        new AsciiPrefixer(-1).encodeLength(0, b);
        assertEquals("b.length", 3, b.length);
    }

    @Test
    public void testEncodeLength1() throws Throwable {
        byte[] bytes = new byte[2];
        AsciiPrefixer.LL.encodeLength(0, bytes);
        assertEquals("bytes[0]", (byte) 48, bytes[0]);
    }

    @Test
    public void testEncodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[0];
        try {
            new AsciiPrefixer(1).encodeLength(100, b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
            assertEquals("b.length", 0, b.length);
        }
    }

    @Test
    public void testEncodeLengthThrowsISOException() throws Throwable {
        byte[] bytes = new byte[2];
        try {
            AsciiPrefixer.LL.encodeLength(100, bytes);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("bytes[0]", (byte) 48, bytes[0]);
            assertEquals("ex.getMessage()", "invalid len 100. Prefixing digits = 2", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testEncodeLengthThrowsISOException1() throws Throwable {
        byte[] b = new byte[0];
        try {
            new AsciiPrefixer(0).encodeLength(1, b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len 1. Prefixing digits = 0", ex.getMessage());
            assertNull("ex.nested", ex.nested);
            assertEquals("b.length", 0, b.length);
        }
    }

    @Test
    public void testEncodeLengthThrowsISOException2() throws Throwable {
        byte[] b = new byte[0];
        try {
            new AsciiPrefixer(0).encodeLength(-1, b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len -1. Prefixing digits = 0", ex.getMessage());
            assertNull("ex.nested", ex.nested);
            assertEquals("b.length", 0, b.length);
        }
    }

    @Test
    public void testEncodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new AsciiPrefixer(2).encodeLength(100, (byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new AsciiPrefixer(0).getPackedLength();
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new AsciiPrefixer(100).getPackedLength();
        assertEquals("result", 100, result);
    }
}
