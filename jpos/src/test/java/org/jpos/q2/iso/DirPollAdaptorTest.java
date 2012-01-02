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

package org.jpos.q2.iso;

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
public class DirPollAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        assertEquals("dirPollAdaptor.getLog().getRealm()", "org.jpos.q2.iso.DirPollAdaptor", dirPollAdaptor.getLog().getRealm());
        assertEquals("dirPollAdaptor.getState()", -1, dirPollAdaptor.getState());
        assertTrue("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
        assertEquals("dirPollAdaptor.pollInterval", 1000L, dirPollAdaptor.pollInterval);
        assertEquals("dirPollAdaptor.poolSize", 1, dirPollAdaptor.poolSize);
    }

    @Test
    public void testGetPath() throws Throwable {
        String result = new DirPollAdaptor().getPath();
        assertEquals("result", ".", result);
    }

    @Test
    public void testGetPath1() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPath("testDirPollAdaptorPath");
        String result = dirPollAdaptor.getPath();
        assertEquals("result", "testDirPollAdaptorPath", result);
    }

    @Test
    public void testGetPollInterval() throws Throwable {
        long result = new DirPollAdaptor().getPollInterval();
        assertEquals("result", 1000L, result);
    }

    @Test
    public void testGetPollInterval1() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPollInterval(0L);
        long result = dirPollAdaptor.getPollInterval();
        assertEquals("result", 0L, result);
    }

    @Test
    public void testGetPoolSize() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPoolSize(0);
        int result = dirPollAdaptor.getPoolSize();
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetPoolSize1() throws Throwable {
        int result = new DirPollAdaptor().getPoolSize();
        assertEquals("result", 1, result);
    }

    @Test
    public void testGetPriorities() throws Throwable {
        String result = new DirPollAdaptor().getPriorities();
        assertNull("result", result);
    }

    @Test
    public void testGetPriorities1() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPriorities("testDirPollAdaptorPriorities");
        String result = dirPollAdaptor.getPriorities();
        assertEquals("result", "testDirPollAdaptorPriorities", result);
    }

    @Test
    public void testGetProcessor() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setProcessor("testDirPollAdaptorProcessor");
        String result = dirPollAdaptor.getProcessor();
        assertEquals("result", "testDirPollAdaptorProcessor", result);
    }

    @Test
    public void testGetProcessor1() throws Throwable {
        String result = new DirPollAdaptor().getProcessor();
        assertNull("result", result);
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
            assertEquals("dirPollAdaptor.dirPoll.getPath()", ".", dirPollAdaptor.dirPoll.getPath());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
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
            assertEquals("dirPollAdaptor.dirPoll.getPath()", ".", dirPollAdaptor.dirPoll.getPath());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException2() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        try {
            dirPollAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dirPollAdaptor.dirPoll", dirPollAdaptor.dirPoll);
            assertTrue("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
        }
    }

    @Test
    public void testSetPath() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPath("testDirPollAdaptorPath");
        assertEquals("dirPollAdaptor.path", "testDirPollAdaptorPath", dirPollAdaptor.path);
        assertTrue("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
    }

    @Test
    public void testSetPollInterval() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPollInterval(100L);
        assertEquals("dirPollAdaptor.pollInterval", 100L, dirPollAdaptor.pollInterval);
        assertTrue("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
    }

    @Test
    public void testSetPoolSize() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPoolSize(100);
        assertEquals("dirPollAdaptor.poolSize", 100, dirPollAdaptor.poolSize);
        assertTrue("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
    }

    @Test
    public void testSetPriorities() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setPriorities("testDirPollAdaptorPriorities");
        assertEquals("dirPollAdaptor.priorities", "testDirPollAdaptorPriorities", dirPollAdaptor.priorities);
        assertTrue("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
    }

    @Test
    public void testSetProcessor() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.setProcessor("testDirPollAdaptorProcessor");
        assertEquals("dirPollAdaptor.processorClass", "testDirPollAdaptorProcessor", dirPollAdaptor.processorClass);
        assertTrue("dirPollAdaptor.isModified()", dirPollAdaptor.isModified());
    }

    @Test
    public void testStartService() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        dirPollAdaptor.startService();
        assertNull("dirPollAdaptor.dirPoll", dirPollAdaptor.dirPoll);
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        DirPollAdaptor dirPollAdaptor = new DirPollAdaptor();
        try {
            dirPollAdaptor.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dirPollAdaptor.dirPoll", dirPollAdaptor.dirPoll);
        }
    }
}
