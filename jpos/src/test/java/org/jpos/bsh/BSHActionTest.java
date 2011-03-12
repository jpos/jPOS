package org.jpos.bsh;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;

import org.junit.Ignore;
import org.junit.Test;

public class BSHActionTest {

    @Ignore("test fails - needs a real action file")
    @Test
    public void testActionPerformed() throws Throwable {
        new BSHAction().actionPerformed(new ActionEvent("testString", 100, "testBSHActionParam3", 100L, 1000));
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testActionPerformedThrowsNullPointerException() throws Throwable {
        try {
            new BSHAction().actionPerformed(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new BSHAction();
        assertTrue("Test completed without Exception", true);
    }

}
