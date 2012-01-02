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

package org.jpos.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class SecureDESKeyTest {

    @Test
    public void testConstructor() throws Throwable {
        SecureDESKey secureDESKey = new SecureDESKey((short) 100, "testSecureDESKeyKeyType", "testSecureDESKeyKeyHexString",
                "testSecureDESKeyKeyCheckValueHexString");
        assertEquals("secureDESKey.keyType", "testSecureDESKeyKeyType", secureDESKey.keyType);
        assertEquals("secureDESKey.keyCheckValue.length", 19, secureDESKey.keyCheckValue.length);
        assertEquals("secureDESKey.keyBytes.length", 14, secureDESKey.keyBytes.length);
        assertEquals("secureDESKey.keyLength", (short) 100, secureDESKey.keyLength);
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] keyBytes = new byte[2];
        byte[] keyCheckValue = new byte[0];
        SecureDESKey secureDESKey = new SecureDESKey((short) 100, "testSecureDESKeyKeyType", keyBytes, keyCheckValue);
        assertEquals("secureDESKey.keyType", "testSecureDESKeyKeyType", secureDESKey.keyType);
        assertSame("secureDESKey.keyCheckValue", keyCheckValue, secureDESKey.keyCheckValue);
        assertSame("secureDESKey.keyBytes", keyBytes, secureDESKey.keyBytes);
        assertEquals("secureDESKey.keyLength", (short) 100, secureDESKey.keyLength);
    }

    @Test
    public void testConstructor2() throws Throwable {
        SecureDESKey secureDESKey = new SecureDESKey();
        assertNull("secureDESKey.keyCheckValue", secureDESKey.keyCheckValue);
        assertNull("secureDESKey.keyBytes", secureDESKey.keyBytes);
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SecureDESKey((short) 100, "testSecureDESKeyKeyType", (String) null, "testSecureDESKeyKeyCheckValueHexString");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorUnevenHexDigits() throws Throwable {
        new SecureDESKey((short) 100, "testSecureDESKeyKeyType", "testSecureDESKeyKeyHexString",
                "testSecureDESKeyKeyCheckValueHexString1");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump() throws Throwable {
        byte[] keyCheckValue = new byte[0];
        byte[] keyBytes = new byte[1];
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-16");
        new SecureDESKey((short) 100, "testSecureDESKeyKeyType", keyBytes, keyCheckValue).dump(p, "testSecureDESKeyIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        byte[] keyCheckValue = new byte[1];
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-16");
        try {
            new SecureDESKey((short) 100, "testSecureDESKeyKeyType", (byte[]) null, keyCheckValue).dump(p, "testSecureDESKeyIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDumpThrowsNullPointerException1() throws Throwable {
        byte[] keyBytes = new byte[2];
        byte[] keyCheckValue = new byte[0];
        try {
            new SecureDESKey((short) 100, "testSecureDESKeyKeyType", keyBytes, keyCheckValue).dump(null, "testSecureDESKeyIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetKeyCheckValue() throws Throwable {
        SecureDESKey secureDESKey = new SecureDESKey((short) 100, "testSecureDESKeyKeyType", "testSecureDESKeyKeyHexString",
                "testSecureDESKeyKeyCheckValueHexString");
        byte[] keyCheckValue = new byte[0];
        secureDESKey.setKeyCheckValue(keyCheckValue);
        byte[] result = secureDESKey.getKeyCheckValue();
        assertSame("result", keyCheckValue, result);
    }

    @Test
    public void testGetKeyCheckValue1() throws Throwable {
        SecureDESKey secureDESKey = new SecureDESKey((short) 100, "testSecureDESKeyKeyType", "testSecureDESKeyKeyHexString",
                "testSecureDESKeyKeyCheckValueHexString");
        byte[] keyCheckValue = new byte[1];
        secureDESKey.setKeyCheckValue(keyCheckValue);
        byte[] result = secureDESKey.getKeyCheckValue();
        assertSame("result", keyCheckValue, result);
        assertEquals("keyCheckValue[0]", (byte) 0, keyCheckValue[0]);
    }

    @Test
    public void testSetKeyCheckValue() throws Throwable {
        byte[] keyCheckValue = new byte[1];
        byte[] keyBytes = new byte[3];
        SecureDESKey secureDESKey = new SecureDESKey((short) 100, "testSecureDESKeyKeyType", keyBytes, keyCheckValue);
        byte[] keyCheckValue2 = new byte[0];
        secureDESKey.setKeyCheckValue(keyCheckValue2);
        assertSame("secureDESKey.keyCheckValue", keyCheckValue2, secureDESKey.keyCheckValue);
    }
}
