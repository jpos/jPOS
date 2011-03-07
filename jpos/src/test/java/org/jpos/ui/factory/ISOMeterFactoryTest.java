package org.jpos.ui.factory;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ISOMeterFactoryTest {

    @Test
    public void testConstructor() throws Throwable {
        new ISOMeterFactory();
        assertTrue("Test completed without Exception", true);
    }
}
