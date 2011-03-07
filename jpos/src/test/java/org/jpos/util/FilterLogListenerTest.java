package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.channel.BASE24TCPChannel;
import org.junit.Test;

public class FilterLogListenerTest {

    @Test
    public void testClose() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.close();
        filterLogListener.close();
        assertNull("filterLogListener.p", filterLogListener.p);
    }

    @Test
    public void testClose1() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.close();
        assertNull("filterLogListener.p", filterLogListener.p);
    }

    @Test
    public void testConstructor() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        FilterLogListener filterLogListener = new FilterLogListener(p);
        assertSame("filterLogListener.p", p, filterLogListener.p);
        assertEquals("filterLogListener.getPriority()", "info", filterLogListener.getPriority());
    }

    @Test
    public void testConstructor1() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        assertNotNull("filterLogListener.p", filterLogListener.p);
        assertEquals("filterLogListener.getPriority()", "info", filterLogListener.getPriority());
    }

    @Test
    public void testLog() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.close();
        LogEvent result = filterLogListener.log(null);
        assertNull("result", result);
    }

    @Test
    public void testLog1() throws Throwable {
        LogEvent ev = new LogEvent(new BASE24TCPChannel(), "testFilterLogListenerTag", "");
        FilterLogListener filterLogListener = new FilterLogListener();
        LogEvent result = filterLogListener.log(ev);
        assertSame("result", ev, result);
        assertNotNull("filterLogListener.p", filterLogListener.p);
    }

    @Test
    public void testLog2() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        filterLogListener.setPriority("error");
        LogEvent ev = new LogEvent("testFilterLogListenerTag");
        LogEvent result = filterLogListener.log(ev);
        assertSame("result", ev, result);
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("filterLogListener.p", p, filterLogListener.p);
        }
    }

    @Test
    public void testPermitLogging() throws Throwable {
        boolean result = new FilterLogListener().permitLogging("info");
        assertTrue("result", result);
    }

    @Test
    public void testPermitLogging1() throws Throwable {
        boolean result = new FilterLogListener().permitLogging("trace");
        assertFalse("result", result);
    }

    @Test
    public void testPermitLogging2() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream()));
        filterLogListener.setPriority("error");
        boolean result = filterLogListener.permitLogging("testFilterLogListenerTagLevel");
        assertFalse("result", result);
    }

    @Test
    public void testPermitLogging3() throws Throwable {
        boolean result = new FilterLogListener().permitLogging("testFilterLogListenerTagLevel");
        assertTrue("result", result);
    }

    @Test
    public void testPermitLoggingThrowsNullPointerException() throws Throwable {
        try {
            new FilterLogListener().permitLogging(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener();
        Configuration cfg = new SimpleConfiguration();
        filterLogListener.setConfiguration(cfg);
        assertEquals("filterLogListener.getPriority()", "info", filterLogListener.getPriority());
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream()));
        Configuration cfg = new SubConfiguration();
        try {
            filterLogListener.setConfiguration(cfg);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertEquals("filterLogListener.getPriority()", "info", filterLogListener.getPriority());
        }
    }

    @Test
    public void testSetPrintStream() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        FilterLogListener filterLogListener = new FilterLogListener();
        filterLogListener.setPrintStream(p);
        assertSame("filterLogListener.p", p, filterLogListener.p);
    }

    @Test
    public void testSetPriority() throws Throwable {
        FilterLogListener filterLogListener = new FilterLogListener(new PrintStream(new ByteArrayOutputStream(), true));
        filterLogListener.setPriority("testFilterLogListenerPriority");
        assertEquals("filterLogListener.getPriority()", "testFilterLogListenerPriority", filterLogListener.getPriority());
    }
}
