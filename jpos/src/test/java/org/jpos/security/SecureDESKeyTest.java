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

package org.jpos.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class SecureDESKeyTest {

    static final byte[] kcv  = new byte[0];
    static final byte[] kcv1 = new byte[1];
    static final short  length = (short) 100;
    static final String keyType = "Key-Type123";
    static final String keyHEX  = "testKeyHEX";
    static final String kcvHEX  = "testKeyKCV_HEX";

    @Test
    public void testConstructor() throws Throwable {
        SecureDESKey key = new SecureDESKey(length, keyType, keyHEX,kcvHEX);
        assertEquals(keyType, key.getKeyType());
        assertEquals(7, key.getKeyCheckValue().length);
        assertEquals(5, key.getKeyBytes().length);
        assertEquals(length, key.getKeyLength());
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] keyBytes = new byte[2];
        SecureDESKey key = new SecureDESKey(length, keyType, keyBytes, kcv);
        assertEquals(keyType, key.getKeyType());
        assertSame(kcv, key.getKeyCheckValue());
        assertSame(keyBytes, key.getKeyBytes());
        assertEquals(length, key.getKeyLength());
    }

    @Test
    public void testConstructor2() throws Throwable {
        SecureDESKey key = new SecureDESKey();
        assertNull(key.getKeyCheckValue());
        assertNull(key.getKeyBytes());
    }

    @Test
    public void testConstructorExtType() throws Throwable {
        byte[] keyBytes = new byte[2];
        String kt = "Key-Type123:4U";
        SecureDESKey key = new SecureDESKey(length, kt, keyBytes, kcv);
        assertEquals(kt, key.getKeyType());
        assertSame(kcv, key.getKeyCheckValue());
        assertSame(keyBytes, key.getKeyBytes());
        assertEquals(length, key.getKeyLength());
        assertEquals(4, key.getVariant());
        assertEquals(KeyScheme.U, key.getScheme());
    }

    @Test
    public void testConstructorExtType_InvalidVariant() throws Throwable {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] keyBytes = new byte[2];
            new SecureDESKey(length, "Key-Type123:JU", keyBytes, kcv);
        });
    }

    @Test
    public void testConstructorExtType_InvalidScheme() throws Throwable {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] keyBytes = new byte[2];
            new SecureDESKey(length, "Key-Type123:3H", keyBytes, kcv);
        });
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            new SecureDESKey(length, keyType, null, "testSecureDESKeyKeyCheckValueHexString");
        });
    }

    @Test
    public void testConstructorUnevenHexDigits() throws Throwable {
        new SecureDESKey(length, keyType, keyHEX, kcvHEX);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDump() throws Throwable {
        byte[] keyBytes = new byte[1];
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        new SecureDESKey(length, keyType, keyBytes, kcv).dump(p, "testIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            PrintStream p = new PrintStream(new ByteArrayOutputStream());
            new SecureDESKey(length, keyType, null, kcv1).dump(p, "");
        });
    }

    @Test
    public void testDumpThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            byte[] keyBytes = new byte[2];
            new SecureDESKey(length, keyType, keyBytes, kcv).dump(null, "");
        });
    }

    @Test
    public void testGetKeyCheckValue() throws Throwable {
        SecureDESKey key = new SecureDESKey(length, keyType, keyHEX, kcvHEX);
        key.setKeyCheckValue(kcv);
        assertSame(kcv, key.getKeyCheckValue());
    }

    @Test
    public void testGetKeyCheckValue1() throws Throwable {
        SecureDESKey key = new SecureDESKey(length, keyType, keyHEX, kcvHEX);
        key.setKeyCheckValue(kcv1);
        assertSame(kcv1, key.getKeyCheckValue());
        assertEquals((byte) 0, kcv1[0]);
    }

    @Test
    public void testSetKeyCheckValue() throws Throwable {
        byte[] keyBytes = new byte[3];
        SecureDESKey key = new SecureDESKey(length, keyType, keyBytes, kcv1);
        byte[] kcv2 = new byte[0];
        key.setKeyCheckValue(kcv2);
        assertSame(kcv2, key.getKeyCheckValue());
    }

}
