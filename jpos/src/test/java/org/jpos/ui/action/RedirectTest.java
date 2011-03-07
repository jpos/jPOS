package org.jpos.ui.action;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;
import java.util.NoSuchElementException;

import org.junit.Test;

public class RedirectTest {

    @Test
    public void testActionPerformedThrowsNoSuchElementException() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent("", 100, "", 100L, 1000));
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testActionPerformedThrowsNullPointerException() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent(Long.valueOf(0L), 100, null, 100L, 1000));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testActionPerformedThrowsNullPointerException1() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent("", 100, "testRedirectParam3", 100L, 1000));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testActionPerformedThrowsNullPointerException2() throws Throwable {
        try {
            new Redirect().actionPerformed(new ActionEvent("", 100, "testRedirect\rParam3", 100L, 1000));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        new Redirect();
        assertTrue("Test completed without Exception", true);
    }

}
