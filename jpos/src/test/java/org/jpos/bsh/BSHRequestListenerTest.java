package org.jpos.bsh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.LogChannel;
import org.jpos.iso.channel.PostChannel;
import org.junit.Test;

public class BSHRequestListenerTest {

    @Test
    public void testConstructor() throws Throwable {
        new BSHRequestListener();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testProcess() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        bSHRequestListener.setConfiguration(new SimpleConfiguration());
        boolean result = bSHRequestListener.process(new PostChannel(), new ISOMsg("testBSHRequestListenerMti"));
        assertTrue("result", result);
        assertEquals("bSHRequestListener.whitelist.size()", 1, bSHRequestListener.whitelist.size());
    }

    @Test
    public void testProcess1() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        boolean result = bSHRequestListener.process(new CSChannel(), new ISOMsg());
        assertFalse("result", result);
        assertNull("bSHRequestListener.whitelist", bSHRequestListener.whitelist);
    }

    @Test
    public void testProcess2() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        ISOMsg m = new ISOMsg();
        m.setMTI("testBSHRequestListenerMti");
        boolean result = bSHRequestListener.process(new LogChannel(), m);
        assertFalse("result", result);
        assertNull("bSHRequestListener.whitelist", bSHRequestListener.whitelist);
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        Configuration cfg = new SimpleConfiguration();
        bSHRequestListener.setConfiguration(cfg);
        assertEquals("bSHRequestListener.whitelist.size()", 1, bSHRequestListener.whitelist.size());
        assertEquals("bSHRequestListener.bshSource.length", 0, bSHRequestListener.bshSource.length);
        assertSame("bSHRequestListener.cfg", cfg, bSHRequestListener.cfg);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        BSHRequestListener bSHRequestListener = new BSHRequestListener();
        Configuration cfg = new SubConfiguration();
        try {
            bSHRequestListener.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame("bSHRequestListener.cfg", cfg, bSHRequestListener.cfg);
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("bSHRequestListener.whitelist", bSHRequestListener.whitelist);
            assertNull("bSHRequestListener.bshSource", bSHRequestListener.bshSource);
        }
    }
}
