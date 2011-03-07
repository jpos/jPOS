package org.jpos.q2.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jdom.Element;
import org.jpos.iso.ISOMsg;
import org.junit.Test;

public class MUXPoolTest {

    @Test
    public void testConstructor() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        assertEquals("mUXPool.getLog().getRealm()", "org.jpos.q2.iso.MUXPool", mUXPool.getLog().getRealm());
        assertEquals("mUXPool.getState()", -1, mUXPool.getState());
        assertTrue("mUXPool.isModified()", mUXPool.isModified());
        assertEquals("mUXPool.msgno", 0, mUXPool.msgno);
        assertEquals("mUXPool.strategy", 0, mUXPool.strategy);
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        try {
            mUXPool.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("mUXPool.isModified()", mUXPool.isModified());
            assertNull("mUXPool.muxName", mUXPool.muxName);
            assertNull("mUXPool.mux", mUXPool.mux);
            assertEquals("mUXPool.strategy", 0, mUXPool.strategy);
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException1() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        mUXPool.setPersist(new Element("testMUXPoolName", "testMUXPoolUri"));
        try {
            mUXPool.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("mUXPool.mux", mUXPool.mux);
            assertFalse("mUXPool.isModified()", mUXPool.isModified());
            assertEquals("mUXPool.strategy", 0, mUXPool.strategy);
            assertNull("mUXPool.muxName", mUXPool.muxName);
        }
    }

    @Test
    public void testIsConnectedThrowsNullPointerException() throws Throwable {
        try {
            new MUXPool().isConnected();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testRequestThrowsNullPointerException() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        try {
            mUXPool.request(new ISOMsg("testMUXPoolMti"), 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("mUXPool.msgno", 1, mUXPool.msgno);
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStopService() throws Throwable {
        MUXPool mUXPool = new MUXPool();
        mUXPool.stopService();
        assertNull("mUXPool.getName()", mUXPool.getName());
    }
}
