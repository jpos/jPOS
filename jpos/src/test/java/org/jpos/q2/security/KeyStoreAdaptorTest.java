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

package org.jpos.q2.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.q2.Q2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KeyStoreAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        assertEquals("keyStoreAdaptor.clazz", "org.jpos.security.SimpleKeyFile", keyStoreAdaptor.clazz);
        assertEquals("keyStoreAdaptor.getLog().getRealm()", "org.jpos.q2.security.KeyStoreAdaptor", keyStoreAdaptor.getLog()
                .getRealm());
        assertEquals("keyStoreAdaptor.getState()", -1, keyStoreAdaptor.getState());
        assertTrue("keyStoreAdaptor.isModified()", keyStoreAdaptor.isModified());
    }

    @Test
    public void testGetImpl() throws Throwable {
        String result = new KeyStoreAdaptor().getImpl();
        assertEquals("result", "org.jpos.security.SimpleKeyFile", result);
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("keyStoreAdaptor.isModified()", keyStoreAdaptor.isModified());
            assertNull("keyStoreAdaptor.ks", keyStoreAdaptor.ks);
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        try {
            keyStoreAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("keyStoreAdaptor.isModified()", keyStoreAdaptor.isModified());
            assertNull("keyStoreAdaptor.ks", keyStoreAdaptor.ks);
        }
    }

    @Test
    public void testSetImpl() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        keyStoreAdaptor.setImpl("testKeyStoreAdaptorClazz");
        assertEquals("keyStoreAdaptor.clazz", "testKeyStoreAdaptorClazz", keyStoreAdaptor.clazz);
    }

 
    @Test
    public void testStopService() throws Throwable {
        KeyStoreAdaptor keyStoreAdaptor = new KeyStoreAdaptor();
        keyStoreAdaptor.setName("testKeyStoreAdaptorName");
        keyStoreAdaptor.stopService();
        assertEquals("keyStoreAdaptor.getName()", "testKeyStoreAdaptorName", keyStoreAdaptor.getName());
    }


}
