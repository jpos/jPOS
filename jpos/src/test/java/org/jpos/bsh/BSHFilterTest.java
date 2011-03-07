package org.jpos.bsh;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVMsg;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.packager.PostPackager;
import org.jpos.util.LogEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BSHFilterTest {

    @Mock
    ISOVMsg m;

    @Test
    public void testConstructor() throws Throwable {
        new BSHFilter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testFilter() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        BSHFilter bSHFilter = new BSHFilter();
        bSHFilter.setConfiguration(cfg);
        ISOMsg result = bSHFilter.filter(new PADChannel(), null, new LogEvent("testBSHFilterTag", "testString"));
        assertNull("result", result);
        assertSame("bSHFilter.cfg", cfg, bSHFilter.cfg);
    }

    @Test
    public void testFilter1() throws Throwable {
        BSHFilter bSHFilter = new BSHFilter();
        Configuration cfg = new SimpleConfiguration();
        bSHFilter.setConfiguration(cfg);
        ISOChannel channel = new CSChannel();
        LogEvent evt = new LogEvent();

        ISOVMsg result = (ISOVMsg) bSHFilter.filter(channel, m, evt);
        assertSame("result", m, result);
        assertSame("bSHFilter.cfg", cfg, bSHFilter.cfg);
    }

    @Test
    public void testFilterThrowsNullPointerException() throws Throwable {
        BSHFilter bSHFilter = new BSHFilter();
        LogEvent evt = new LogEvent();
        try {
            bSHFilter.filter(new CSChannel("testBSHFilterHost", 100, new PostPackager()), new ISOMsg("testBSHFilterMti"), evt);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("bSHFilter.cfg", bSHFilter.cfg);
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHFilter bSHFilter = new BSHFilter();
        Configuration cfg = new SimpleConfiguration();
        bSHFilter.setConfiguration(cfg);
        assertSame("bSHFilter.cfg", cfg, bSHFilter.cfg);
    }
}
