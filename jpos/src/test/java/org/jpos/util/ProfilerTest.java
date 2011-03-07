package org.jpos.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class ProfilerTest {

    @Test
    public void testCheckPoint() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.checkPoint("testProfilerDetail1");

    }

    @Test
    public void testCheckPointNull() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.checkPoint(null);
        assertEquals("profiler.events.size()", 1, profiler.events.size());

    }

    @Test
    public void testConstructor() throws Throwable {
        Profiler profiler = new Profiler();
        assertEquals("profiler.events.size()", 0, profiler.events.size());
    }

    @Test
    public void testDump() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.dump(new PrintStream(new ByteArrayOutputStream()), "testProfilerIndent");
        assertEquals("profiler.events.size()", 1, profiler.events.size());
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        Profiler profiler = new Profiler();
        try {
            profiler.dump(null, "testProfilerIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("profiler.events.size()", 1, profiler.events.size());
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetPartial() throws Throwable {
        new Profiler().getPartial();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testReset() throws Throwable {
        Profiler profiler = new Profiler();
        profiler.reset();
        assertEquals("profiler.events.size()", 0, profiler.events.size());
    }
}
