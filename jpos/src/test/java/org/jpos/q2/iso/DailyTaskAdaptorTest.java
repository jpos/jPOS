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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.q2.Q2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DailyTaskAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        assertEquals("dailyTaskAdaptor.getLog().getRealm()", "org.jpos.q2.iso.DailyTaskAdaptor", dailyTaskAdaptor.getLog()
                .getRealm());
        assertEquals("dailyTaskAdaptor.getState()", -1, dailyTaskAdaptor.getState());
        assertNull("dailyTaskAdaptor.thisThread", dailyTaskAdaptor.thisThread);
        assertTrue("dailyTaskAdaptor.isModified()", dailyTaskAdaptor.isModified());
    }

    @Test
    public void testGetWhenThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        try {
            dailyTaskAdaptor.getWhen();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dailyTaskAdaptor.getConfiguration()", dailyTaskAdaptor.getConfiguration());
        }
    }

    @Test
    public void testGetWhenThrowsNumberFormatException() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.setConfiguration(cfg);
        try {
            dailyTaskAdaptor.getWhen();
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \":0\"", ex.getMessage());
            assertSame("dailyTaskAdaptor.getConfiguration()", cfg, dailyTaskAdaptor.getConfiguration());
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        try {
            dailyTaskAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertTrue("dailyTaskAdaptor.isModified()", dailyTaskAdaptor.isModified());
            assertNull("dailyTaskAdaptor.task", dailyTaskAdaptor.task);
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        String[] args = new String[3];
        args[0] = "vI";
        args[1] = "e.";
        args[2] = "testString";
        dailyTaskAdaptor.setServer(q2);
        try {
            dailyTaskAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("dailyTaskAdaptor.isModified()", dailyTaskAdaptor.isModified());
            assertNull("dailyTaskAdaptor.task", dailyTaskAdaptor.task);
        }
    }

    @Test
    public void testRun() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.run();
        assertNull("dailyTaskAdaptor.getConfiguration()", dailyTaskAdaptor.getConfiguration());
    }

    @Test
    public void testRunThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.setState(2);
        try {
            dailyTaskAdaptor.run();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dailyTaskAdaptor.getConfiguration()", dailyTaskAdaptor.getConfiguration());
        }
    }

    @Test
    public void testRunThrowsNumberFormatException() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.setState(2);
        dailyTaskAdaptor.setConfiguration(cfg);
        try {
            dailyTaskAdaptor.run();
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \":0\"", ex.getMessage());
            assertSame("dailyTaskAdaptor.getConfiguration()", cfg, dailyTaskAdaptor.getConfiguration());
        }
    }

    @Test
    public void testStartService() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.startService();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testStopService() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.startService();
        dailyTaskAdaptor.stopService();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testStopService1() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.stopService();
        assertNull("dailyTaskAdaptor.thisThread", dailyTaskAdaptor.thisThread);
    }

    @Test
    public void testWaitUntilStartTimeThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        try {
            dailyTaskAdaptor.waitUntilStartTime();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dailyTaskAdaptor.getConfiguration()", dailyTaskAdaptor.getConfiguration());
        }
    }

    @Test
    public void testWaitUntilStartTimeThrowsNumberFormatException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        Configuration cfg = new SimpleConfiguration();
        dailyTaskAdaptor.setConfiguration(cfg);
        try {
            dailyTaskAdaptor.waitUntilStartTime();
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException ex) {
            assertEquals("ex.getMessage()", "For input string: \":0\"", ex.getMessage());
            assertSame("dailyTaskAdaptor.getConfiguration()", cfg, dailyTaskAdaptor.getConfiguration());
        }
    }

    @Test
    public void testGetWhen() {
    }
}
