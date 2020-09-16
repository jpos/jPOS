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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.q2.Q2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DailyTaskAdaptorTest {
    @Mock
    Q2 q2;

    @Test
    public void testConstructor() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        assertEquals("org.jpos.q2.iso.DailyTaskAdaptor", dailyTaskAdaptor.getLog()
                .getRealm(), "dailyTaskAdaptor.getLog().getRealm()");
        assertEquals(-1, dailyTaskAdaptor.getState(), "dailyTaskAdaptor.getState()");
        assertNull(dailyTaskAdaptor.thisThread, "dailyTaskAdaptor.thisThread");
        assertTrue(dailyTaskAdaptor.isModified(), "dailyTaskAdaptor.isModified()");
    }

    @Test
    public void testGetWhenThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        try {
            dailyTaskAdaptor.getWhen();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(dailyTaskAdaptor.getConfiguration(), "dailyTaskAdaptor.getConfiguration()");
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
            assertEquals("For input string: \":0\"", ex.getMessage(), "ex.getMessage()");
            assertSame(cfg, dailyTaskAdaptor.getConfiguration(), "dailyTaskAdaptor.getConfiguration()");
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        try {
            dailyTaskAdaptor.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getFactory()\" because the return value of \"org.jpos.q2.iso.DailyTaskAdaptor.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertTrue(dailyTaskAdaptor.isModified(), "dailyTaskAdaptor.isModified()");
            assertNull(dailyTaskAdaptor.task, "dailyTaskAdaptor.task");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildTextTrim(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(dailyTaskAdaptor.isModified(), "dailyTaskAdaptor.isModified()");
            assertNull(dailyTaskAdaptor.task, "dailyTaskAdaptor.task");
        }
    }

    @Test
    public void testRun() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.run();
        assertNull(dailyTaskAdaptor.getConfiguration(), "dailyTaskAdaptor.getConfiguration()");
    }

    @Test
    public void testRunThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.setState(2);
        try {
            dailyTaskAdaptor.run();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(dailyTaskAdaptor.getConfiguration(), "dailyTaskAdaptor.getConfiguration()");
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
            assertEquals("For input string: \":0\"", ex.getMessage(), "ex.getMessage()");
            assertSame(cfg, dailyTaskAdaptor.getConfiguration(), "dailyTaskAdaptor.getConfiguration()");
        }
    }

    @Test
    public void testStartService() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.startService();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testStopService() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.startService();
        dailyTaskAdaptor.stopService();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testStopService1() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        dailyTaskAdaptor.stopService();
        assertNull(dailyTaskAdaptor.thisThread, "dailyTaskAdaptor.thisThread");
    }

    @Test
    public void testWaitUntilStartTimeThrowsNullPointerException() throws Throwable {
        DailyTaskAdaptor dailyTaskAdaptor = new DailyTaskAdaptor();
        try {
            dailyTaskAdaptor.waitUntilStartTime();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(dailyTaskAdaptor.getConfiguration(), "dailyTaskAdaptor.getConfiguration()");
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
            assertEquals("For input string: \":0\"", ex.getMessage(), "ex.getMessage()");
            assertSame(cfg, dailyTaskAdaptor.getConfiguration(), "dailyTaskAdaptor.getConfiguration()");
        }
    }

    @Test
    public void testGetWhen() {
    }
}
