package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.junit.Ignore;
import org.junit.Test;

@Ignore ("test causes problems, closes stdout")
public class RotateLogListenerTest {

    @Test
    public void testCheckSizeThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        try {
            rotateLogListener.checkSize();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("rotateLogListener.f", rotateLogListener.f);
            assertNotNull("rotateLogListener.p", rotateLogListener.p);
        }
    }

    @Test
    public void testCloseLogFile() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        rotateLogListener.closeLogFile();
        assertNull("rotateLogListener.f", rotateLogListener.f);
    }

    @Test
    public void testConstructor() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        assertNotNull("rotateLogListener.p", rotateLogListener.p);
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new RotateLogListener(null, 100, 1000, 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // xpected
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException1() throws Throwable {
        try {
            new RotateLogListener(null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testDestroy() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.destroy();
        assertNull("(DailyLogListener) dailyLogListener.f", ((DailyLogListener) dailyLogListener).f);
    }

    @Test
    public void testLog() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        LogEvent ev = new LogEvent();
        LogEvent result = dailyLogListener.log(ev);
        assertSame("result", ev, result);
        assertNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
        assertEquals("(DailyLogListener) dailyLogListener.msgCount", 1, ((DailyLogListener) dailyLogListener).msgCount);
    }

    @Test
    public void testLog1() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        LogEvent result = dailyLogListener.log(null);
        assertNull("result", result);
        assertNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
        assertEquals("(DailyLogListener) dailyLogListener.msgCount", 1, ((DailyLogListener) dailyLogListener).msgCount);
    }

    @Test
    public void testLogDebug() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNotNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
    }

    @Test
    public void testLogDebug1() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        dailyLogListener.close();
        dailyLogListener.logDebug("testRotateLogListenerMsg");
        assertNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
    }

    @Test
    public void testLogRotateThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        try {
            rotateLogListener.logRotate();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("rotateLogListener.f", rotateLogListener.f);
            assertNull("rotateLogListener.p", rotateLogListener.p);
        }
    }

    @Test
    public void testOpenLogFileThrowsNullPointerException() throws Throwable {
        RotateLogListener dailyLogListener = new DailyLogListener();
        try {
            dailyLogListener.openLogFile();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("(DailyLogListener) dailyLogListener.f", ((DailyLogListener) dailyLogListener).f);
            assertNotNull("(DailyLogListener) dailyLogListener.p", ((DailyLogListener) dailyLogListener).p);
        }
    }

    @Test
    public void testRotateConstructor() throws Throwable {
        new RotateLogListener().new Rotate();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        RotateLogListener rotateLogListener = new RotateLogListener();
        Configuration cfg = new SubConfiguration();
        try {
            rotateLogListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("rotateLogListener.logName", rotateLogListener.logName);
            assertEquals("rotateLogListener.sleepTime", 0L, rotateLogListener.sleepTime);
            assertEquals("rotateLogListener.maxSize", 0L, rotateLogListener.maxSize);
            assertEquals("rotateLogListener.maxCopies", 0, rotateLogListener.maxCopies);
            assertNull("rotateLogListener.rotate", rotateLogListener.rotate);
            assertNull("rotateLogListener.f", rotateLogListener.f);
            assertNotNull("rotateLogListener.p", rotateLogListener.p);
        }
    }
}
