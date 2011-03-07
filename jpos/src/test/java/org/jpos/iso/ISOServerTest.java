package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jpos.util.NameRegistrar;
import org.junit.Test;

public class ISOServerTest {

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ISOServer(100, null, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetServerThrowsNotFoundException() throws Throwable {
        try {
            ISOServer.getServer("testISOServerName");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("ex.getMessage()", "server.testISOServerName", ex.getMessage());
        }
    }
}
