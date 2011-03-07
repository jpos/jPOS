package org.jpos.ui.action;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;

import org.junit.Test;

public class DebugTest {

    @Test
    public void testActionPerformed() throws Throwable {
        Debug debug = new Debug();
        debug.actionPerformed(new ActionEvent(debug, 100, "testDebugParam3", 100L, 1000));
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testActionPerformedThrowsNullPointerException() throws Throwable {
        try {
            new Debug().actionPerformed(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new Debug();
        assertTrue("Test completed without Exception", true);
    }
}
