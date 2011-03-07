package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class AsciiInterpreter2Test {

    @Test
    public void testConstructor() throws Throwable {
        new AsciiInterpreter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new AsciiInterpreter().getPackedLength(0);
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetPackedLength1() throws Throwable {
        int result = new AsciiInterpreter().getPackedLength(100);
        assertEquals("result", 100, result);
    }

    @Test
    public void testInterpret() throws Throwable {
        byte[] b = new byte[10];
        new AsciiInterpreter().interpret("10Characte", b, 0);
        assertEquals("b[0]", (byte) 49, b[0]);
    }

    @Test
    public void testInterpretThrowsNullPointerException() throws Throwable {
        try {
            new AsciiInterpreter().interpret("testAsciiInterpreterData", (byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testInterpretThrowsNullPointerException1() throws Throwable {
        byte[] b = new byte[2];
        try {
            new AsciiInterpreter().interpret(null, b, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("b.length", 2, b.length);
        }
    }

    @Test
    public void testUninterpret() throws Throwable {
        byte[] rawData = new byte[2];
        String result = new AsciiInterpreter().uninterpret(rawData, 0, 1);
        assertEquals("result", "\u0000", result);
    }

    @Test
    public void testUninterpretThrowsNegativeArraySizeException() throws Throwable {
        byte[] rawData = new byte[0];
        try {
            new AsciiInterpreter().uninterpret(rawData, 100, -1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUninterpretThrowsNullPointerException() throws Throwable {
        try {
            new AsciiInterpreter().uninterpret((byte[]) null, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
