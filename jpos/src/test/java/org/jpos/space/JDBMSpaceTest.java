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

package org.jpos.space;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import org.junit.jupiter.api.Test;

public class JDBMSpaceTest {

    @Test
    public void testGetLong() throws Throwable {
        byte[] b = new byte[11];
        b[0] = (byte) 48;
        long result = JDBMSpace.getLong(b, 0);
        assertEquals(3458764513820540928L, result, "result");
    }

    @Test
    public void testGetLong1() throws Throwable {
        long result = JDBMSpace.getLong(new JDBMSpace.Ref(100L, 1000L).serialize(new JDBMSpace.Ref()), 0);
        assertEquals(0L, result, "result");
    }

    @Test
    public void testGetLongThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[9];
        try {
            JDBMSpace.getLong(b, -1);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 9", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[7];
        try {
            JDBMSpace.getLong(b, -4);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 7", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        byte[] b = new byte[5];
        try {
            JDBMSpace.getLong(b, -6);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 5", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsArrayIndexOutOfBoundsException3() throws Throwable {
        byte[] b = new byte[2];
        try {
            JDBMSpace.getLong(b, -7);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsArrayIndexOutOfBoundsException4() throws Throwable {
        byte[] b = new byte[8];
        try {
            JDBMSpace.getLong(b, -2);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 8", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsArrayIndexOutOfBoundsException5() throws Throwable {
        byte[] b = new byte[5];
        try {
            JDBMSpace.getLong(b, -3);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 5", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsArrayIndexOutOfBoundsException6() throws Throwable {
        byte[] b = new byte[3];
        try {
            JDBMSpace.getLong(b, -5);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetLongThrowsNullPointerException() throws Throwable {
        try {
            JDBMSpace.getLong(null, 100);
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
    public void testHeadConstructor() throws Throwable {
        JDBMSpace.Head head = new JDBMSpace.Head();
        assertEquals(-1L, head.last, "head.last");
        assertEquals(-1L, head.first, "head.first");
    }

    @Test
    public void testHeadReadExternalThrowsNullPointerException() throws Throwable {
        JDBMSpace.Head head = new JDBMSpace.Head();
        try {
            head.readExternal(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.ObjectInput.readLong()\" because \"in\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0L, head.count, "head.count");
            assertEquals(-1L, head.last, "head.last");
            assertEquals(-1L, head.first, "head.first");
        }
    }

    @Test
    public void testHeadToString() throws Throwable {
        new JDBMSpace.Head().toString();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testHeadWriteExternalThrowsNullPointerException() throws Throwable {
        try {
            new JDBMSpace.Head().writeExternal(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.ObjectOutput.writeLong(long)\" because \"out\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testPutLong() throws Throwable {
        byte[] b = new byte[66];
        JDBMSpace.putLong(b, 0, 100L);
        assertEquals((byte) 100, b[7], "b[7]");
    }

    @Test
    public void testPutLongThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = "42".getBytes();
        try {
            JDBMSpace.putLong(b, -6, 100L);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals((byte) 0, b[0], "b[0]");
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("-1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index -1 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(2, b.length, "b.length");
        }
    }

    @Test
    public void testPutLongThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[3];
        try {
            JDBMSpace.putLong(b, 100, 100L);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("107", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 107 out of bounds for length 3", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(3, b.length, "b.length");
        }
    }

    @Test
    public void testPutLongThrowsNullPointerException() throws Throwable {
        try {
            JDBMSpace.putLong(null, 100, 100L);
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
    public void testRefConstructor() throws Throwable {
        JDBMSpace.Ref ref = new JDBMSpace.Ref(100L, 1000L);
        assertEquals(100L, ref.recid, "ref.recid");
        assertEquals(-1L, ref.next, "ref.next");
        assertEquals(1000L, ref.expires, "ref.expires");
    }

    @Test
    public void testRefConstructor1() throws Throwable {
        JDBMSpace.Ref ref = new JDBMSpace.Ref();
        assertTrue(ref.isExpired(), "ref.isExpired()");
    }

    @Test
    public void testRefDeserialize() throws Throwable {
        JDBMSpace.Ref ref = new JDBMSpace.Ref();
        byte[] serialized = new byte[26];
        JDBMSpace.Ref result = (JDBMSpace.Ref) ref.deserialize(serialized);
        assertTrue(result.isExpired(), "result.isExpired()");
        assertEquals(0L, ref.recid, "ref.recid");
        assertEquals(0L, ref.expires, "ref.expires");
        assertEquals(0L, ref.next, "ref.next");
    }

    @Test
    public void testRefDeserializeThrowsArrayIndexOutOfBoundsException2() throws Throwable {
        JDBMSpace.Ref ref = new JDBMSpace.Ref(100L, 1000L);
        byte[] serialized = new byte[2];
        try {
            ref.deserialize(serialized);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("7", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 7 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(100L, ref.recid, "ref.recid");
            assertEquals(1000L, ref.expires, "ref.expires");
            assertEquals(-1L, ref.next, "ref.next");
        }
    }

    @Test
    public void testRefDeserializeThrowsNullPointerException() throws Throwable {
        JDBMSpace.Ref ref = new JDBMSpace.Ref(100L, 1000L);
        try {
            ref.deserialize(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot load from byte/boolean array because \"b\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(100L, ref.recid, "ref.recid");
            assertEquals(1000L, ref.expires, "ref.expires");
            assertEquals(-1L, ref.next, "ref.next");
        }
    }

    @Test
    public void testRefIsExpired() throws Throwable {
        long expirytime = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000;
        boolean result = new JDBMSpace.Ref(100L, expirytime).isExpired();
        assertFalse(result, "result");
    }

    @Test
    public void testRefIsExpired1() throws Throwable {
        boolean result = new JDBMSpace.Ref(100L, 1000L).isExpired();
        assertTrue(result, "result");
    }

    @Test
    public void testRefSerialize() throws Throwable {
        JDBMSpace.Ref ref = new JDBMSpace.Ref(100L, 1000L);
        byte[] result = ref.serialize(new JDBMSpace.Ref(1000L, 0L));
        assertEquals(24, result.length, "result.length");
        assertEquals((byte) 0, result[0], "result[0]");
    }

    @Test
    public void testRefSerializeThrowsClassCastException() throws Throwable {
        try {
            new JDBMSpace.Ref(100L, 1000L).serialize(Integer.valueOf(0));
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals(ClassCastException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testRefSerializeThrowsNullPointerException() throws Throwable {
        try {
            new JDBMSpace.Ref().serialize(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"recid\" because \"d\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testRefToString() throws Throwable {
        new JDBMSpace.Ref(100L, 1000L).toString();
        assertTrue(true, "Test completed without Exception");
    }
}
