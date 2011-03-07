package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class RightPadder2Test {

    @Test
    public void testPad() throws Throwable {
        String result = new RightPadder(' ').pad("", 100);
        assertEquals("result",
                "                                                                                                    ", result);
    }

    @Test
    public void testPad1() throws Throwable {
        String result = new RightPadder(' ').pad("", 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testPadThrowsISOException() throws Throwable {
        try {
            new RightPadder(' ').pad("testRightPadderData", 0);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "Data is too long. Max = 0", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testPadThrowsNullPointerException() throws Throwable {
        try {
            new RightPadder(' ').pad(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpad() throws Throwable {
        String result = RightPadder.SPACE_PADDER.unpad(" ");
        assertEquals("result", "", result);
    }

    @Test
    public void testUnpad1() throws Throwable {
        String result = RightTPadder.SPACE_PADDER.unpad("X ");
        assertEquals("result", "X", result);
    }

    @Test
    public void testUnpad2() throws Throwable {
        String result = new RightPadder(' ').unpad("1");
        assertEquals("result", "1", result);
    }

    @Test
    public void testUnpad3() throws Throwable {
        String result = new RightPadder(' ').unpad("");
        assertEquals("result", "", result);
    }

    @Test
    public void testUnpadThrowsNullPointerException() throws Throwable {
        try {
            new RightPadder(' ').unpad(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
