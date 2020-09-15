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

package org.jpos.q2.security;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.q2.Q2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KeyStoreAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        assertEquals("org.jpos.security.SimpleKeyFile", keyStoreAdaptor.clazz, "keyStoreAdaptor.clazz");
        assertEquals("org.jpos.q2.security.KeyStoreAdaptor", keyStoreAdaptor.getLog()
                .getRealm(), "keyStoreAdaptor.getLog().getRealm()");
        assertEquals(-1, keyStoreAdaptor.getState(), "keyStoreAdaptor.getState()");
        assertTrue(keyStoreAdaptor.isModified(), "keyStoreAdaptor.isModified()");
    }

    @Test
    public void testGetImpl() throws Throwable {
        String result = new KeyStoreAdaptor().getImpl();
        assertEquals("org.jpos.security.SimpleKeyFile", result, "result");
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        String[] args = new String[0];
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        keyStoreAdaptor.setServer(q2);
        try {
            keyStoreAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.QFactory.newInstance(String)\" because \"factory\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(keyStoreAdaptor.isModified(), "keyStoreAdaptor.isModified()");
            assertNull(keyStoreAdaptor.ks, "keyStoreAdaptor.ks");
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        try {
            keyStoreAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getFactory()\" because the return value of \"org.jpos.q2.security.KeyStoreAdaptor.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(keyStoreAdaptor.isModified(), "keyStoreAdaptor.isModified()");
            assertNull(keyStoreAdaptor.ks, "keyStoreAdaptor.ks");
        }
    }

    @Test
    public void testSetImpl() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        keyStoreAdaptor.setImpl("testKeyStoreAdaptorClazz");
        assertEquals("testKeyStoreAdaptorClazz", keyStoreAdaptor.clazz, "keyStoreAdaptor.clazz");
    }

 
    @Test
    public void testStopService() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        keyStoreAdaptor.setName("testKeyStoreAdaptorName");
        keyStoreAdaptor.destroyService();
        assertEquals("testKeyStoreAdaptorName", keyStoreAdaptor.getName(), "keyStoreAdaptor.getName()");
    }


}
