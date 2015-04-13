package org.jpos.util;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StopWatchTest {
    @Test
    public void testStopWatch() throws Throwable {
        long now = System.currentTimeMillis();
        StopWatch sw = new StopWatch (500, TimeUnit.MILLISECONDS);
        assertFalse(sw.isFinished());
        sw.finish();
        assertTrue(System.currentTimeMillis() - now >= 500);
        assertTrue (sw.isFinished());
    }
}
