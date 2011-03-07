package org.jpos.security.jceadapter;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConsoleTest {

    @Test
    public void testConstructor() throws Throwable {
        new Console();
        assertTrue("Test completed without Exception", true);
    }
}
