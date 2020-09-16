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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class SecureKeyStoreTest {

    @Test
    public void testSecureKeyStoreExceptionConstructor() throws Throwable {
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException();
        assertNull(secureKeyStoreException.getNested(), "secureKeyStoreException.getNested()");
    }

    @Test
    public void testSecureKeyStoreExceptionConstructor1() throws Throwable {
        Exception nested = new SecureKeyStore.SecureKeyStoreException();
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException(
                "testSecureKeyStoreExceptionDetail", nested);
        assertEquals("testSecureKeyStoreExceptionDetail", secureKeyStoreException.getMessage(),
                "secureKeyStoreException.getMessage()");
        assertSame(nested, secureKeyStoreException.getNested(), "secureKeyStoreException.getNested()");
    }

    @Test
    public void testSecureKeyStoreExceptionConstructor2() throws Throwable {
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException(
                "testSecureKeyStoreExceptionDetail");
        assertEquals("testSecureKeyStoreExceptionDetail", secureKeyStoreException.getMessage(),
                "secureKeyStoreException.getMessage()");
        assertNull(secureKeyStoreException.getNested(), "secureKeyStoreException.getNested()");
    }

    @Test
    public void testSecureKeyStoreExceptionConstructor3() throws Throwable {
        Exception nested = new SecureKeyStore.SecureKeyStoreException();
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException(nested);
        assertEquals("org.jpos.security.SecureKeyStore$SecureKeyStoreException",
                secureKeyStoreException.getMessage(), "secureKeyStoreException.getMessage()");
        assertSame(nested, secureKeyStoreException.getNested(), "secureKeyStoreException.getNested()");
    }

    @Test
    public void testSecureKeyStoreExceptionConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SecureKeyStore.SecureKeyStoreException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.lang.Throwable.toString()\" because \"nested\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
