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
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class SecureKeyTest {

    @Test
    public void testGetKeyBytes() throws Throwable {
        SecureKey secureDESKey = new SecureDESKey();
        byte[] keyBytes = new byte[0];
        secureDESKey.setKeyBytes(keyBytes);
        byte[] result = secureDESKey.getKeyBytes();
        assertSame(keyBytes, result, "result");
    }

    @Test
    public void testGetKeyBytes1() throws Throwable {
        byte[] keyBytes = new byte[3];
        byte[] result = new SecureDESKey((short) 100, "testSecureKeyKeyType", keyBytes, keyBytes).getKeyBytes();
        assertSame(keyBytes, result, "result");
        assertEquals((byte) 0, keyBytes[0], "keyBytes[0]");
    }

    @Test
    public void testGetKeyLength() throws Throwable {
        SecureKey secureDESKey = new SecureDESKey();
        secureDESKey.setKeyLength((short) 100);
        short result = secureDESKey.getKeyLength();
        assertEquals((short) 100, result, "result");
    }

    @Test
    public void testGetKeyType() throws Throwable {
        SecureKey secureDESKey = new SecureDESKey();
        secureDESKey.setKeyType("testSecureKeyKeyType");
        String result = secureDESKey.getKeyType();
        assertEquals("testSecureKeyKeyType", result, "result");
    }

    @Test
    public void testSetKeyBytes() throws Throwable {
        byte[] keyBytes = new byte[3];
        SecureKey secureDESKey = new SecureDESKey((short) 100, "testSecureKeyKeyType", keyBytes, keyBytes);
        secureDESKey.setKeyBytes(keyBytes);
        assertSame(keyBytes, ((SecureDESKey) secureDESKey).keyBytes, "(SecureDESKey) secureDESKey.keyBytes");
    }

    @Test
    public void testSetKeyLength() throws Throwable {
        byte[] keyBytes = new byte[3];
        new SecureDESKey((short) 100, "testSecureKeyKeyType", keyBytes, keyBytes).getKeyBytes();
        SecureKey secureDESKey = new SecureDESKey((short) 1000, "testSecureKeyKeyType1", "3check-value>".getBytes(), keyBytes);
        secureDESKey.setKeyLength((short) 100);
        assertEquals((short) 100, ((SecureDESKey) secureDESKey).keyLength, "(SecureDESKey) secureDESKey.keyLength");
    }

    @Test
    public void testSetKeyType() throws Throwable {
        byte[] keyBytes = new byte[3];
        new SecureDESKey((short) 100, "testSecureKeyKeyType", keyBytes, keyBytes).getKeyBytes();
        SecureKey secureDESKey = new SecureDESKey((short) 1000, "testSecureKeyKeyType1", "3check-value>".getBytes(), keyBytes);
        secureDESKey.setKeyType("testSecureKeyKeyType");
        assertEquals("testSecureKeyKeyType", ((SecureDESKey) secureDESKey).keyType, "(SecureDESKey) secureDESKey.keyType");
    }
}
