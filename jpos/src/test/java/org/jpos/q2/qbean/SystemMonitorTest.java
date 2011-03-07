package org.jpos.q2.qbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jpos.q2.Q2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemMonitorTest {
    @Mock
    Q2 q2;

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new SystemMonitor().dump(null, "testString");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testRun() throws Throwable {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.setState(4);
        systemMonitor.run();
    }

    @Test
    public void testRun1() throws Throwable {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.setState(1);
        systemMonitor.run();
    }

    @Test
    public void testSetDetailRequired() throws Throwable {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.setDetailRequired(true);
        assertTrue("systemMonitor.getDetailRequired()", systemMonitor.getDetailRequired());
        assertTrue("systemMonitor.isModified()", systemMonitor.isModified());
    }

    @Test
    public void testSetDetailRequired1() throws Throwable {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.startService();
        systemMonitor.setDetailRequired(false);
        assertFalse("systemMonitor.getDetailRequired()", systemMonitor.getDetailRequired());
        assertTrue("systemMonitor.isModified()", systemMonitor.isModified());
    }

    @Test
    public void testSetSleepTime() throws Throwable {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.setSleepTime(1L);
        assertEquals("systemMonitor.getSleepTime()", 1L, systemMonitor.getSleepTime());
        assertTrue("systemMonitor.isModified()", systemMonitor.isModified());
    }

    @Test
    public void testSetSleepTime1() throws Throwable {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.startService();
        systemMonitor.setSleepTime(100L);
        assertEquals("systemMonitor.getSleepTime()", 100L, systemMonitor.getSleepTime());
        assertTrue("systemMonitor.isModified()", systemMonitor.isModified());
    }

    @Test
    public void testShowThreadGroupThrowsNullPointerException() throws Throwable {
        PrintStream printStream = new PrintStream(new ByteArrayOutputStream(), true, "US-ASCII");
        printStream.append("r");
        PrintStream p = new PrintStream(printStream, false, "ISO-8859-1");
        try {
            new SystemMonitor().showThreadGroup(null, p, "testSystemMonitorIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStartService() throws Throwable {
        new SystemMonitor().startService();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testStopService() throws Throwable {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.startService();
        systemMonitor.stopService();
        assertTrue("Test completed without Exception", true);
    }
}
