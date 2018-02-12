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

package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

@Ignore ("test causes problems, closes stdout")
public class LoggerTest {

    @Test
    public void testAddListener() throws Throwable {
        Logger logger = new Logger();
        LogListener l = new SimpleLogListener();
        logger.addListener(l);
        assertEquals("m_logger.listeners.size()", 1, logger.listeners.size());
        assertSame("m_logger.listeners.get(0)", l, logger.listeners.get(0));
    }

    @Test
    public void testConstructor() throws Throwable {
        Logger logger = new Logger();
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
        assertEquals("m_logger.name", "", logger.name);
    }

    @Test
    public void testDestroy() throws Throwable {
        Logger logger = new Logger();
        LogListener l = new SimpleLogListener();
        logger.addListener(l);
        logger.destroy();
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
        assertFalse("m_logger.listeners.contains(l)", logger.listeners.contains(l));
    }

    @Test
    public void testDestroy1() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(new SimpleLogListener());
        logger.addListener(new ExceptionLogFilter());
        logger.destroy();
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
    }

    @Test
    public void testGetLogger() throws Throwable {
        Logger result = Logger.getLogger("testLoggerName");
        assertEquals("result.getName()", "testLoggerName", result.getName());
    }

    @Test
    public void testGetName() throws Throwable {
        String result = new Logger().getName();
        assertEquals("result", "", result);
    }

    @Test
    public void testHasListeners() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(new SimpleLogListener());
        boolean result = logger.hasListeners();
        assertTrue("result", result);
    }

    @Test
    public void testHasListeners1() throws Throwable {
        boolean result = new Logger().hasListeners();
        assertFalse("result", result);
    }

    @Test
    public void testLog() throws Throwable {
        Logger.log(new LogEvent(new SimpleLogSource(Logger.getLogger("testLoggerName"), "testLoggerRealm"), "testLoggerTag",
                "testString"));
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testLog1() throws Throwable {
        Logger.log(new LogEvent(new Log(), "testLoggerTag", "testString"));
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testLog2() throws Throwable {
        Logger.log(new LogEvent(null, "testLoggerTag", ""));
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testLogThrowsNullPointerException() throws Throwable {
        try {
            Logger.log(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testRemoveAllListeners() throws Throwable {
        Logger logger = Logger.getLogger("testLoggerName");
        LogListener l = new SimpleLogListener();
        logger.addListener(l);
        logger.removeAllListeners();
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
        assertFalse("m_logger.listeners.contains(l)", logger.listeners.contains(l));
    }

    @Test
    public void testRemoveAllListeners1() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(new FilterLogListener());
        logger.addListener(new RotateLogListener());
        logger.removeAllListeners();
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
    }

    @Test
    public void testRemoveAllListeners2() throws Throwable {
        Logger logger = new Logger();
        LogListener l = new RotateLogListener();
        logger.addListener(l);
        logger.removeAllListeners();
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
        assertFalse("m_logger.listeners.contains(l)", logger.listeners.contains(l));
    }

    @Test
    public void testRemoveAllListeners3() throws Throwable {
        Logger logger = new Logger();
        logger.removeAllListeners();
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
    }

    @Test
    public void testRemoveListener() throws Throwable {
        Logger logger = new Logger();
        logger.removeListener(new SimpleLogListener());
        assertEquals("m_logger.listeners.size()", 0, logger.listeners.size());
    }

    @Test
    public void testSetName() throws Throwable {
        Logger logger = new Logger();
        logger.setName("testLoggerName");
        assertEquals("m_logger.name", "testLoggerName", logger.name);
    }
}
