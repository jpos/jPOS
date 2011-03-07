package org.jpos.util;

import static org.junit.Assert.assertNotNull;

import java.util.Timer;

import org.junit.Test;

public class DefaultTimerTest {

    @Test
    public void testGetTimer() throws Throwable {
        Timer result = DefaultTimer.getTimer();
        assertNotNull("result", result);
    }
}
