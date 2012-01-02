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

public class BcdPrefixer2Test {

    @Test
    public void testConstructor() throws Throwable {
        BcdPrefixer bcdPrefixer = new BcdPrefixer(100);
        assertEquals("bcdPrefixer.getPackedLength()", 50, bcdPrefixer.getPackedLength());
    }

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] b = new byte[1];
        int result = new BcdPrefixer(0).decodeLength(b, 100);
        assertEquals("result", 0, result);
    }

    @Test
    public void testDecodeLength1() throws Throwable {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) 37;
        int result = BcdPrefixer.LL.decodeLength(bytes, 0);
        assertEquals("result", 25, result);
    }

    @Test
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[1];
        try {
            new BcdPrefixer(100).decodeLength(b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testDecodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new BcdPrefixer(100).decodeLength((byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEncodeLength() throws Throwable {
        byte[] bytes = new byte[2];
        BcdPrefixer.LLL.encodeLength(100, bytes);
        assertEquals("bytes[0]", (byte) 1, bytes[0]);
    }

    @Test
    public void testEncodeLength1() throws Throwable {
        byte[] b = new byte[1];
        new BcdPrefixer(0).encodeLength(100, b);
        assertEquals("b.length", 1, b.length);
    }

    @Test
    public void testEncodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[1];
        try {
            BcdPrefixer.LLL.encodeLength(100, b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
            assertEquals("b.length", 1, b.length);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeLengthThrowsNullPointerException() throws Throwable {
        BcdPrefixer.L.encodeLength(100, (byte[]) null);
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new BcdPrefixer(0).getPackedLength();
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new BcdPrefixer(100).getPackedLength();
        assertEquals("result", 50, result);
    }
}
