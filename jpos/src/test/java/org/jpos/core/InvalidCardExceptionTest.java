package org.jpos.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InvalidCardExceptionTest {

    @Test
    public void testConstructor() throws Throwable {
        new InvalidCardException();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testConstructor1() throws Throwable {
        InvalidCardException invalidCardException = new InvalidCardException("testInvalidCardExceptions");
        assertEquals("invalidCardException.getMessage()", "testInvalidCardExceptions", invalidCardException.getMessage());
    }
}
