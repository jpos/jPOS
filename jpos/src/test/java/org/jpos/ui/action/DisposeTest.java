package org.jpos.ui.action;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;

import org.junit.Test;

public class DisposeTest {

    @Test
    public void testActionPerformedThrowsNullPointerException() throws Throwable {
        Dispose dispose = new Dispose();
        try {
            dispose.actionPerformed(new ActionEvent(Integer.valueOf(0), 100, "testDisposeParam3", 100L, 1000));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("dispose.ui", dispose.ui);
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new Dispose();
        assertTrue("Test completed without Exception", true);
    }

}
