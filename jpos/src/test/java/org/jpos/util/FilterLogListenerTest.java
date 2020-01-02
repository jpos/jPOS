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

package org.jpos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.channel.BASE24TCPChannel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("test causes problems, closes stdout")
public class FilterLogListenerTest {

    @Test
    public void testClose() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.close();
        filterLogListener.close();
        assertNull(filterLogListener.p, "filterLogListener.p");
    }

    @Test
    public void testClose1() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.close();
        assertNull(filterLogListener.p, "filterLogListener.p");
    }

    @Test
    public void testConstructor() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        FilterLogListener filterLogListener = new FilterLogListener(p);
        assertSame(p, filterLogListener.p, "filterLogListener.p");
        assertEquals("info", filterLogListener.getPriority(), "filterLogListener.getPriority()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        assertNotNull(filterLogListener.p, "filterLogListener.p");
        assertEquals("info", filterLogListener.getPriority(), "filterLogListener.getPriority()");
    }

    @Test
    public void testLog() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.close();
        LogEvent result = filterLogListener.log(null);
        assertNull(result, "result");
    }

    @Test
    public void testLog1() throws Throwable {
        LogEvent ev = new LogEvent(new BASE24TCPChannel(), "testFilterLogListenerTag", "");
        FilterLogListener filterLogListener = new FilterLogListener();
        LogEvent result = filterLogListener.log(ev);
        assertSame(ev, result, "result");
        assertNotNull(filterLogListener.p, "filterLogListener.p");
    }

    @Test
    public void testLog2() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        filterLogListener.setPriority("error");
        LogEvent ev = new LogEvent("testFilterLogListenerTag");
        LogEvent result = filterLogListener.log(ev);
        assertSame(ev, result, "result");
    }

    @Test
    public void testLogThrowsNullPointerException1() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        FilterLogListener filterLogListener = new FilterLogListener(p);
        filterLogListener.setPriority("testFilterLogListenerPriority");
        LogEvent ev = new LogEvent("testFilterLogListenerTag");
        try {
            filterLogListener.log(ev);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertSame(p, filterLogListener.p, "filterLogListener.p");
        }
    }

    @Test
    public void testPermitLogging() throws Throwable {
        boolean result = new FilterLogListener().permitLogging("info");
        assertTrue(result, "result");
    }

    @Test
    public void testPermitLogging1() throws Throwable {
        boolean result = new FilterLogListener().permitLogging("trace");
        assertFalse(result, "result");
    }

    @Test
    public void testPermitLogging2() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream()));
        filterLogListener.setPriority("error");
        boolean result = filterLogListener.permitLogging("testFilterLogListenerTagLevel");
        assertFalse(result, "result");
    }

    @Test
    public void testPermitLogging3() throws Throwable {
        boolean result = new FilterLogListener().permitLogging("testFilterLogListenerTagLevel");
        assertTrue(result, "result");
    }

    @Test
    public void testPermitLoggingThrowsNullPointerException() throws Throwable {
        try {
            new FilterLogListener().permitLogging(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testPermitLoggingThrowsNullPointerException1() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        filterLogListener.setPriority("testFilterLogListenerPriority");
        try {
            filterLogListener.permitLogging("testFilterLogListenerTagLevel");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testPermitLoggingThrowsNullPointerException2() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        filterLogListener.setPriority("testFilterLogListenerPriority");
        try {
            filterLogListener.permitLogging("trace");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        Configuration cfg = new SimpleConfiguration();
        filterLogListener.setConfiguration(cfg);
        assertEquals("info", filterLogListener.getPriority(), "filterLogListener.getPriority()");
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream()));
        Configuration cfg = new SubConfiguration();
        try {
            filterLogListener.setConfiguration(cfg);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            assertEquals("info", filterLogListener.getPriority(), "filterLogListener.getPriority()");
        }
    }

    @Test
    public void testSetPrintStream() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.setPrintStream(p);
        assertSame(p, filterLogListener.p, "filterLogListener.p");
    }

    @Test
    public void testSetPriority() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        filterLogListener.setPriority("testFilterLogListenerPriority");
        assertEquals("testFilterLogListenerPriority", filterLogListener.getPriority(), "filterLogListener.getPriority()");
    }
}
