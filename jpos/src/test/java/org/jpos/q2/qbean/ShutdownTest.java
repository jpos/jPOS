package org.jpos.q2.qbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.q2.Q2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ShutdownTest {
    @Mock
    Q2 server;

    @Test
    public void testConstructor() throws Throwable {
        Shutdown shutdown = new Shutdown();
        assertEquals("shutdown.getLog().getRealm()", "org.jpos.q2.qbean.Shutdown", shutdown.getLog().getRealm());
        assertEquals("shutdown.getState()", -1, shutdown.getState());
        assertTrue("shutdown.isModified()", shutdown.isModified());
    }

    @Test
    public void testStartService() throws Throwable {
        Shutdown shutdown = new Shutdown();
        String[] args = new String[2];
        args[0] = "";
        args[1] = "testString";
        shutdown.setServer(server);
        shutdown.startService();
        assertSame("shutdown.getServer()", server, shutdown.getServer());
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        try {
            new Shutdown().startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
