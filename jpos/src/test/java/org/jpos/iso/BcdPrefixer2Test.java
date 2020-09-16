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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import org.junit.jupiter.api.Test;

public class BcdPrefixer2Test {

    @Test
    public void testConstructor() throws Throwable {
        BcdPrefixer bcdPrefixer = new BcdPrefixer(100);
        assertEquals(50, bcdPrefixer.getPackedLength(), "bcdPrefixer.getPackedLength()");
    }

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] b = new byte[1];
        int result = new BcdPrefixer(0).decodeLength(b, 100);
        assertEquals(0, result, "result");
    }

    @Test
    public void testDecodeLength1() throws Throwable {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) 37;
        int result = BcdPrefixer.LL.decodeLength(bytes, 0);
        assertEquals(25, result, "result");
    }

    @Test
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[1];
        try {
            new BcdPrefixer(100).decodeLength(b, 0);
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
    public void testDecodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new BcdPrefixer(100).decodeLength(null, 100);
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
        byte[] bytes = new byte[2];
        BcdPrefixer.LLL.encodeLength(100, bytes);
        assertEquals((byte) 1, bytes[0], "bytes[0]");
    }

    @Test
    public void testEncodeLength1() throws Throwable {
        byte[] b = new byte[1];
        new BcdPrefixer(0).encodeLength(100, b);
        assertEquals(1, b.length, "b.length");
    }

    @Test
    public void testEncodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[1];
        try {
            BcdPrefixer.LLL.encodeLength(100, b);
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
        assertThrows(NullPointerException.class, () -> {
            BcdPrefixer.L.encodeLength(100, null);
        });
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new BcdPrefixer(0).getPackedLength();
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new BcdPrefixer(100).getPackedLength();
        assertEquals(50, result, "result");
    }
}
