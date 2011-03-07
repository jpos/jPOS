package org.jpos.q2.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jdom.Element;
import org.junit.Test;

public class UITest {

    @Test
    public void testConstructor() throws Throwable {
        UI uI = new UI();
        assertEquals("uI.getLog().getRealm()", "org.jpos.q2.ui.UI", uI.getLog().getRealm());
        assertEquals("uI.getState()", -1, uI.getState());
        assertTrue("uI.isModified()", uI.isModified());
    }

    @Test
    public void testNewInstanceThrowsNullPointerException() throws Throwable {
        try {
            new UI().newInstance("testUIClazz");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        UI uI = new UI();
        uI.setPersist(new Element("testUIName", "testUIPrefix", "testUIUri"));
        try {
            uI.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("uI.isModified()", uI.isModified());
            assertNull("uI.ui", uI.ui);
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException1() throws Throwable {
        UI uI = new UI();
        try {
            uI.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("uI.isModified()", uI.isModified());
            assertNull("uI.ui", uI.ui);
        }
    }

    @Test
    public void testStopService() throws Throwable {
        UI uI = new UI();
        uI.setName("ui");
        uI.stopService();
        assertNull("uI.ui", uI.ui);
    }
}
