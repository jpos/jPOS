package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NullPadderTest {

    @Test
    public void testConstructor() throws Throwable {
        new NullPadder();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPad() throws Throwable {
        String result = NullPadder.INSTANCE.pad("testNullPadderData", 100);
        assertEquals("result", "testNullPadderData", result);
    }

    @Test
    public void testPad1() throws Throwable {
        String result = new NullPadder().pad(null, 100);
        assertNull("result", result);
    }

    @Test
    public void testUnpad() throws Throwable {
        String result = new NullPadder().unpad(null);
        assertNull("result", result);
    }

    @Test
    public void testUnpad1() throws Throwable {
        String result = new NullPadder().unpad("testNullPadderPaddedData");
        assertEquals("result", "testNullPadderPaddedData", result);
    }
}
