/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.BASE24TCPChannel;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.util.LogEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelayFilterTest {
    @Mock
    ISOMsg m;

    @Test
    public void testConstructor() throws Throwable {
        DelayFilter delayFilter = new DelayFilter(100);
        assertEquals("delayFilter.delay", 100, delayFilter.delay);
    }

    @Test
    public void testConstructor1() throws Throwable {
        DelayFilter delayFilter = new DelayFilter();
        assertEquals("delayFilter.delay", 0, delayFilter.delay);
    }

    @Test
    public void testFilter() throws Throwable {
        DelayFilter delayFilter = new DelayFilter(0);
        LogEvent evt = new LogEvent("testDelayFilterTag", "testString");
        ISOChannel channel = new CSChannel(new Base1SubFieldPackager(), new ServerSocket());
        ISOMsg result = delayFilter.filter(channel, m, evt);
        assertEquals("evt.payLoad.size()", 2, evt.getPayLoad().size());
        assertEquals("evt.payLoad.get(1)", "<delay-filter delay=\"0\"/>", evt.getPayLoad().get(1));
        assertSame("result", m, result);
    }

    @Test
    public void testFilter1() throws Throwable {
        LogEvent evt = new LogEvent(new BASE24TCPChannel("testDelayFilterHost", 100, new Base1SubFieldPackager()),
                "testDelayFilterTag", null);
        ISOMsg result = new DelayFilter(1).filter(new PADChannel(), null, evt);
        assertEquals("evt.payLoad.size()", 2, evt.getPayLoad().size());
        assertEquals("evt.payLoad.get(1)", "<delay-filter delay=\"1\"/>", evt.getPayLoad().get(1));
        assertNull("result", result);
    }

    @Test
    public void testFilter2() throws Throwable {
        DelayFilter delayFilter = new DelayFilter(-1);
        ISOChannel channel = new PADChannel(new CTCSubFieldPackager());
        LogEvent evt = new LogEvent("testDelayFilterTag", "");
        ISOMsg result = delayFilter.filter(channel, m, evt);
        assertEquals("evt.payLoad.size()", 2, evt.getPayLoad().size());
        assertEquals("evt.payLoad.get(1)", "<delay-filter delay=\"-1\"/>", evt.getPayLoad().get(1));
        assertSame("result", m, result);
    }

    @Test
    public void testFilterThrowsNullPointerException() throws Throwable {
        try {
            new DelayFilter(0).filter(new NACChannel(), new ISOMsg("testDelayFilterMti"), null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        DelayFilter delayFilter = new DelayFilter(100);
        delayFilter.setConfiguration(new SimpleConfiguration());
        assertEquals("delayFilter.delay", 0, delayFilter.delay);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        DelayFilter delayFilter = new DelayFilter(100);
        Configuration cfg = new SubConfiguration();
        try {
            delayFilter.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("delayFilter.delay", 100, delayFilter.delay);
        }
    }
}
