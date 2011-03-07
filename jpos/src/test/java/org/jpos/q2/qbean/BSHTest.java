package org.jpos.q2.qbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class BSHTest {

    @Test
    public void testConstructor() throws Throwable {
        BSH bSH = new BSH();
        assertEquals("bSH.getLog().getRealm()", "org.jpos.q2.qbean.BSH", bSH.getLog().getRealm());
        assertEquals("bSH.getState()", -1, bSH.getState());
        assertTrue("bSH.isModified()", bSH.isModified());
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        BSH bSH = new BSH();
        try {
            bSH.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testRun() throws Throwable {
        BSH bSH = new BSH();
        bSH.run();
        assertNull("bSH.bsh", bSH.bsh);
        assertFalse("bSH.isModified()", bSH.isModified());
    }

    @Test
    public void testStartService() throws Throwable {
        BSH bSH = new BSH();
        bSH.startService();
        assertNull("bSH.getName()", bSH.getName());
    }
}
