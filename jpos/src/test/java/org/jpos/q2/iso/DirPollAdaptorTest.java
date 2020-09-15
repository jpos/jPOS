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

package org.jpos.q2.iso;

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
public class DirPollAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        assertEquals("org.jpos.q2.iso.DirPollAdaptor", dirPollAdaptor.getLog().getRealm(), "dirPollAdaptor.getLog().getRealm()");
        assertEquals(-1, dirPollAdaptor.getState(), "dirPollAdaptor.getState()");
        assertTrue(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
        assertEquals(1000L, dirPollAdaptor.pollInterval, "dirPollAdaptor.pollInterval");
        assertEquals(1, dirPollAdaptor.poolSize, "dirPollAdaptor.poolSize");
    }

    @Test
    public void testGetPath() throws Throwable {
        String result = new DirPollAdaptor().getPath();
        assertEquals(".", result, "result");
    }

    @Test
    public void testGetPath1() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPath("testDirPollAdaptorPath");
        String result = dirPollAdaptor.getPath();
        assertEquals("testDirPollAdaptorPath", result, "result");
    }

    @Test
    public void testGetPollInterval() throws Throwable {
        long result = new DirPollAdaptor().getPollInterval();
        assertEquals(1000L, result, "result");
    }

    @Test
    public void testGetPollInterval1() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPollInterval(0L);
        long result = dirPollAdaptor.getPollInterval();
        assertEquals(0L, result, "result");
    }

    @Test
    public void testGetPoolSize() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPoolSize(0);
        int result = dirPollAdaptor.getPoolSize();
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetPoolSize1() throws Throwable {
        int result = new DirPollAdaptor().getPoolSize();
        assertEquals(1, result, "result");
    }

    @Test
    public void testGetPriorities() throws Throwable {
        String result = new DirPollAdaptor().getPriorities();
        assertNull(result, "result");
    }

    @Test
    public void testGetPriorities1() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPriorities("testDirPollAdaptorPriorities");
        String result = dirPollAdaptor.getPriorities();
        assertEquals("testDirPollAdaptorPriorities", result, "result");
    }

    @Test
    public void testGetProcessor() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setProcessor("testDirPollAdaptorProcessor");
        String result = dirPollAdaptor.getProcessor();
        assertEquals("testDirPollAdaptorProcessor", result, "result");
    }

    @Test
    public void testGetProcessor1() throws Throwable {
        String result = new DirPollAdaptor().getProcessor();
        assertNull(result, "result");
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        String[] args = new String[0];
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setServer(q2);
        try {
            dirPollAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(".", dirPollAdaptor.dirPoll.getPath(), "dirPollAdaptor.dirPoll.getPath()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.QFactory.getConfiguration(org.jdom2.Element)\" because \"factory\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        String[] args = new String[1];
        args[0] = "testString";
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setServer(q2);
        dirPollAdaptor.setPriorities("testDirPollAdaptorPriorities");
        try {
            dirPollAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(".", dirPollAdaptor.dirPoll.getPath(), "dirPollAdaptor.dirPoll.getPath()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.QFactory.getConfiguration(org.jdom2.Element)\" because \"factory\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException2() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        try {
            dirPollAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getFactory()\" because the return value of \"org.jpos.q2.iso.DirPollAdaptor.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(dirPollAdaptor.dirPoll, "dirPollAdaptor.dirPoll");
            assertTrue(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
        }
    }

    @Test
    public void testSetPath() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPath("testDirPollAdaptorPath");
        assertEquals("testDirPollAdaptorPath", dirPollAdaptor.path, "dirPollAdaptor.path");
        assertTrue(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
    }

    @Test
    public void testSetPollInterval() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPollInterval(100L);
        assertEquals(100L, dirPollAdaptor.pollInterval, "dirPollAdaptor.pollInterval");
        assertTrue(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
    }

    @Test
    public void testSetPoolSize() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPoolSize(100);
        assertEquals(100, dirPollAdaptor.poolSize, "dirPollAdaptor.poolSize");
        assertTrue(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
    }

    @Test
    public void testSetPriorities() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPriorities("testDirPollAdaptorPriorities");
        assertEquals("testDirPollAdaptorPriorities", dirPollAdaptor.priorities, "dirPollAdaptor.priorities");
        assertTrue(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
    }

    @Test
    public void testSetProcessor() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setProcessor("testDirPollAdaptorProcessor");
        assertEquals("testDirPollAdaptorProcessor", dirPollAdaptor.processorClass, "dirPollAdaptor.processorClass");
        assertTrue(dirPollAdaptor.isModified(), "dirPollAdaptor.isModified()");
    }

    @Test
    public void testStartService() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        try {
            dirPollAdaptor.startService();
        } catch (IllegalStateException e) {
            assertNull(dirPollAdaptor.dirPoll, "dirPollAdaptor.dirPoll");
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        try {
            dirPollAdaptor.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.util.DirPoll.destroy()\" because \"this.dirPoll\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(dirPollAdaptor.dirPoll, "dirPollAdaptor.dirPoll");
        }
    }
}
