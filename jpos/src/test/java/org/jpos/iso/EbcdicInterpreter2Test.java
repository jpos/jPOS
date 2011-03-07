package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class EbcdicInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new EbcdicInterpreter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new EbcdicInterpreter().getPackedLength(100);
        assertEquals("result", 100, result);
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[2];
        new EbcdicInterpreter().interpret("", b, 100);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[1];
        try {
            new EbcdicInterpreter().interpret("testEbcdicInterpreterData", b, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        try {
            EbcdicInterpreter.INSTANCE.interpret("testEbcdicInterpreterData", (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[0];
        String result = EbcdicInterpreter.INSTANCE.uninterpret(rawData, 100, 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testUninterpretThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] rawData = new byte[0];
        try {
            new EbcdicInterpreter().uninterpret(rawData, 100, 1000);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[2];
        try {
            new EbcdicInterpreter().uninterpret(rawData, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            new EbcdicInterpreter().uninterpret((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
