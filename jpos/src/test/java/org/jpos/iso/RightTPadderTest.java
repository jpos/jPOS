package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class RightTPadderTest {

    @Test
    public void testPad() throws Throwable {
        String result = new RightTPadder(' ').pad("10Characte", 10);
        assertEquals("result", "10Characte", result);
    }

    @Test
    public void testPad1() throws Throwable {
        String result = RightTPadder.SPACE_PADDER.pad("", 100);
        assertEquals("result",
                "                                                                                                    ", result);
    }

    @Test
    public void testPad2() throws Throwable {
        String result = new RightTPadder(' ').pad("testRightTPadderData", 0);
        assertEquals("result", "", result);
    }

    @Test
    public void testPadThrowsNullPointerException() throws Throwable {
        try {
            new RightTPadder(' ').pad(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testPadThrowsStringIndexOutOfBoundsException() throws Throwable {
        try {
            new RightTPadder(' ').pad("testRightTPadderData", -1);
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: -1", ex.getMessage());
        }
    }
}
