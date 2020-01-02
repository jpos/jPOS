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

package org.jpos.q2.qbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import org.junit.jupiter.api.Test;

public class QExecTest {

    @Test
    public void testConstructor() throws Throwable {
        QExec qExec = new QExec();
        assertEquals("org.jpos.q2.qbean.QExec", qExec.getLog().getRealm(), "qExec.getLog().getRealm()");
        assertEquals(-1, qExec.getState(), "qExec.getState()");
        assertTrue(qExec.isModified(), "qExec.isModified()");
    }

    @Test
    public void testGetShutdownScript() throws Throwable {
        String result = new QExec().getShutdownScript();
        assertNull(result, "result");
    }

    @Test
    public void testGetShutdownScript1() throws Throwable {
        QExec qExec = new QExec();
        qExec.setShutdownScript("testQExecScriptPath");
        String result = qExec.getShutdownScript();
        assertEquals("testQExecScriptPath", result, "result");
    }

    @Test
    public void testGetStartScript() throws Throwable {
        String result = new QExec().getStartScript();
        assertNull(result, "result");
    }

    @Test
    public void testGetStartScript1() throws Throwable {
        QExec qExec = new QExec();
        qExec.setStartScript("testQExecScriptPath");
        String result = qExec.getStartScript();
        assertEquals("testQExecScriptPath", result, "result");
    }

    @Test
    public void testSetShutdownScript() throws Throwable {
        QExec qExec = new QExec();
        qExec.setShutdownScript("testQExecScriptPath");
        assertEquals("testQExecScriptPath", qExec.shutdownScript, "qExec.shutdownScript");
    }

    @Test
    public void testSetStartScript() throws Throwable {
        QExec qExec = new QExec();
        qExec.setStartScript("testQExecScriptPath");
        assertEquals("testQExecScriptPath", qExec.startScript, "qExec.startScript");
    }

    @Test
    public void testStartServiceThrowsIllegalArgumentException() throws Throwable {
        QExec qExec = new QExec();
        qExec.setStartScript("");
        try {
            qExec.startService();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("Empty command", ex.getMessage(), "ex.getMessage()");
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("Empty command", ex.getMessage(), "ex.getMessage()");
        }
    }
}
