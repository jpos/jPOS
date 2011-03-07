package org.jpos.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import junitx.util.PrivateAccessor;

import org.junit.Test;

public class VolatileSequencerTest {

    @Test
    public void testConstructor() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        assertNotNull(PrivateAccessor.getField(volatileSequencer, "map"));
    }

    @Test
    public void testGet() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        volatileSequencer.get("abcdefghijklmnopqrstuvwxyz", 1);
        int result = volatileSequencer.get("abcdefghijklmnopqrstuvwxyz", -1);
        assertEquals("result", 0, result);
    }

    @Test
    public void testGet1() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int add = volatileSequencer.get("  ", -1);
        int result = volatileSequencer.get("  ", 0);
        assertEquals("result", add, result);
    }

    @Test
    public void testGet2() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int result = volatileSequencer.get("testVolatileSequencerCounterName", 0);
        assertEquals("result", 0, result);
    }

    @Test
    public void testGet3() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int result = volatileSequencer.get("testVolatileSequencerCounterName");
        assertEquals("result", 1, result);
    }

    @Test
    public void testGet4() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        volatileSequencer.set("testString", -2);
        volatileSequencer.get("testString");
        int result = volatileSequencer.get("testString");
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetCounterNames() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        volatileSequencer.get("testVolatileSequencerCounterName", 100);
        String[] result = volatileSequencer.getCounterNames();
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", "testVolatileSequencerCounterName", result[0]);
    }

    @Test
    public void testGetCounterNames1() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        String[] result = volatileSequencer.getCounterNames();
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testSet() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int add = volatileSequencer.get("", 100);
        int result = volatileSequencer.set("", add);
        assertEquals("result", add, result);
    }

    @Test
    public void testSet1() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int add = volatileSequencer.get("testString", 0);
        int result = volatileSequencer.set("testString", 100);
        assertEquals("result", add, result);
    }

    @Test
    public void testSet2() throws Throwable {
        VolatileSequencer volatileSequencer = new VolatileSequencer();
        int result = volatileSequencer.set("testVolatileSequencerCounterName", 100);
        assertEquals("result", 0, result);
    }
}
