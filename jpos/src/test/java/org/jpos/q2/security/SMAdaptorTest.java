/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

import org.jpos.q2.Q2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SMAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        SMAdaptor sMAdaptor = new SMAdaptor();
        assertEquals("sMAdaptor.getLog().getRealm()", "org.jpos.q2.security.SMAdaptor", sMAdaptor.getLog().getRealm());
        assertEquals("sMAdaptor.getState()", -1, sMAdaptor.getState());
        assertTrue("sMAdaptor.isModified()", sMAdaptor.isModified());
    }

    @Test
    public void testGetImpl() throws Throwable {
        SMAdaptor sMAdaptor = new SMAdaptor();
        sMAdaptor.setImpl("testSMAdaptorClazz");
        String result = sMAdaptor.getImpl();
        assertEquals("result", "testSMAdaptorClazz", result);
    }

    @Test
    public void testGetImpl1() throws Throwable {
        String result = new SMAdaptor().getImpl();
        assertNotNull("result", result);
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        SMAdaptor sMAdaptor = new SMAdaptor();
        String[] args = new String[1];
        args[0] = "testString";
        sMAdaptor.setServer(q2);
        try {
            sMAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("sMAdaptor.sm", sMAdaptor.sm);
            assertFalse("sMAdaptor.isModified()", sMAdaptor.isModified());
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        SMAdaptor sMAdaptor = new SMAdaptor();
        try {
            sMAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("sMAdaptor.sm", sMAdaptor.sm);
            assertFalse("sMAdaptor.isModified()", sMAdaptor.isModified());
        }
    }

    @Test
    public void testSetImpl() throws Throwable {
        SMAdaptor sMAdaptor = new SMAdaptor();
        sMAdaptor.setImpl("testSMAdaptorClazz");
        assertEquals("sMAdaptor.clazz", "testSMAdaptorClazz", sMAdaptor.clazz);
    }

    @Test
    public void testStopService() throws Throwable {
        SMAdaptor sMAdaptor = new SMAdaptor();
        sMAdaptor.setName("testSMAdaptorName");
        sMAdaptor.stopService();
        assertEquals("sMAdaptor.getName()", "testSMAdaptorName", sMAdaptor.getName());
    }

}
