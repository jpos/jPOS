package org.jpos.q2.qbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jdom.Element;
import org.junit.Test;

public class SpaceProxyTest {

    @Test
    public void testConstructor() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        assertNull("spaceProxyAdaptor.getSpaceName()", spaceProxyAdaptor.getSpaceName());
        assertEquals("spaceProxyAdaptor.getLog().getRealm()", "org.jpos.q2.qbean.SpaceProxyAdaptor", spaceProxyAdaptor.getLog()
                .getRealm());
        assertEquals("spaceProxyAdaptor.getState()", -1, spaceProxyAdaptor.getState());
        assertTrue("spaceProxyAdaptor.isModified()", spaceProxyAdaptor.isModified());
    }

    @Test
    public void testGetKeysThrowsNullPointerException() throws Throwable {
        try {
            new SpaceProxyAdaptor().getKeys();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetSpaceName() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        spaceProxyAdaptor.setPersist(new Element("testSpaceProxyAdaptorName"));
        spaceProxyAdaptor.setSpaceName("testSpaceProxyAdaptorSpaceName");
        assertEquals("spaceProxyAdaptor.getSpaceName()", "testSpaceProxyAdaptorSpaceName", spaceProxyAdaptor.getSpaceName());
        assertTrue("spaceProxyAdaptor.isModified()", spaceProxyAdaptor.isModified());
    }

    @Test
    public void testSetSpaceNameThrowsNullPointerException() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        try {
            spaceProxyAdaptor.setSpaceName("testSpaceProxyAdaptorSpaceName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("spaceProxyAdaptor.getSpaceName()", "testSpaceProxyAdaptorSpaceName", spaceProxyAdaptor.getSpaceName());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("spaceProxyAdaptor.isModified()", spaceProxyAdaptor.isModified());
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        try {
            spaceProxyAdaptor.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
