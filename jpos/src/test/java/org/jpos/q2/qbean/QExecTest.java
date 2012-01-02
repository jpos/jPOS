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

package org.jpos.q2.qbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class QExecTest {

    @Test
    public void testConstructor() throws Throwable {
        QExec qExec = new QExec();
        assertEquals("qExec.getLog().getRealm()", "org.jpos.q2.qbean.QExec", qExec.getLog().getRealm());
        assertEquals("qExec.getState()", -1, qExec.getState());
        assertTrue("qExec.isModified()", qExec.isModified());
    }

    @Test
    public void testGetShutdownScript() throws Throwable {
        String result = new QExec().getShutdownScript();
        assertNull("result", result);
    }

    @Test
    public void testGetShutdownScript1() throws Throwable {
        QExec qExec = new QExec();
        qExec.setShutdownScript("testQExecScriptPath");
        String result = qExec.getShutdownScript();
        assertEquals("result", "testQExecScriptPath", result);
    }

    @Test
    public void testGetStartScript() throws Throwable {
        String result = new QExec().getStartScript();
        assertNull("result", result);
    }

    @Test
    public void testGetStartScript1() throws Throwable {
        QExec qExec = new QExec();
        qExec.setStartScript("testQExecScriptPath");
        String result = qExec.getStartScript();
        assertEquals("result", "testQExecScriptPath", result);
    }

    @Test
    public void testInitService() throws Throwable {
        QExec qExec = new QExec();
        qExec.initService();
        assertNull("qExec.getShutdownScript()", qExec.getShutdownScript());
    }

    @Test
    public void testSetShutdownScript() throws Throwable {
        QExec qExec = new QExec();
        qExec.setShutdownScript("testQExecScriptPath");
        assertEquals("qExec.shutdownScript", "testQExecScriptPath", qExec.shutdownScript);
    }

    @Test
    public void testSetStartScript() throws Throwable {
        QExec qExec = new QExec();
        qExec.setStartScript("testQExecScriptPath");
        assertEquals("qExec.startScript", "testQExecScriptPath", qExec.startScript);
    }

    @Test
    public void testStartServiceThrowsIllegalArgumentException() throws Throwable {
        QExec qExec = new QExec();
        qExec.setStartScript("");
        try {
            qExec.startService();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "Empty command", ex.getMessage());
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        try {
            new QExec().startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStopServiceThrowsArrayIndexOutOfBoundsException() throws Throwable {
        QExec qExec = new QExec();
        qExec.setShutdownScript(" ");
        try {
            qExec.stopService();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
        }
    }

    @Test
    public void testStopServiceThrowsIllegalArgumentException() throws Throwable {
        QExec qExec = new QExec();
        qExec.setShutdownScript("");
        try {
            qExec.stopService();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "Empty command", ex.getMessage());
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        try {
            new QExec().stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
