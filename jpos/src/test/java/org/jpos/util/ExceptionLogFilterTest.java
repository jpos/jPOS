package org.jpos.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExceptionLogFilterTest {

    @Test
    public void testConstructor() throws Throwable {
        new ExceptionLogFilter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testLog() throws Throwable {
        LogEvent result = new ExceptionLogFilter().log(new LogEvent());
        assertNull("result", result);
    }

}
