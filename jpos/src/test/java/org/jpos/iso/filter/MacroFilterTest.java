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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.BASE24TCPChannel;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.GZIPChannel;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.packager.GenericSubFieldPackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.jpos.iso.packager.X92GenericPackager;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.LogEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MacroFilterTest {
    @Mock
    ISOMsg m;

    @Test
    public void testConstructor() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        assertEquals("macroFilter.unsetFields.length", 0, macroFilter.unsetFields.length);
        assertEquals("macroFilter.validFields.length", 0, macroFilter.validFields.length);
    }

    @Test
    public void testFilter() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        ISOChannel channel = new GZIPChannel(new X92GenericPackager());
        LogEvent evt = new LogEvent("testMacroFilterTag", "\u0000\u0000");
        when(m.getMaxField()).thenReturn(0);
        ISOMsg result = macroFilter.filter(channel, m, evt);
        assertSame("result", m, result);
        verify(m).hasField(0);
    }

    @Test
    public void testFilter1() throws Throwable {
        ISOMsg m = new ISOMsg(100);
        m.merge(new ISOMsg("testMacroFilterMti"));
        m.set(1, "");
        ISOMsg result = new MacroFilter().filter(new CSChannel(new ISOBaseValidatingPackager()), m, new LogEvent(
                "testMacroFilterTag", ""));
        assertEquals("result.getDirection()", 0, result.getDirection());
    }

    @Test
    public void testFilter2() throws Throwable {
        ISOMsg m = new ISOMsg();
        m.set(100, "testMacroFilterValue");
        ISOMsg result = new MacroFilter().filter(new BASE24TCPChannel(), m, new LogEvent("testMacroFilterTag"));
        assertSame("result", m, result);
    }

    @Test
    public void testFilter3() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        ISOChannel channel = new PADChannel(new GenericSubFieldPackager());
        LogEvent evt = new LogEvent();
        when(m.getMaxField()).thenReturn(0);
        when(m.hasField(0)).thenReturn(false);

        ISOMsg result = macroFilter.filter(channel, m, evt);
        assertSame("result", m, result);
    }

    @Test
    public void testFilter4() throws Throwable {
        ISOMsg m = new ISOMsg(100);
        byte[] value = new byte[2];
        m.set(100, value);
        m.merge(new ISOMsg("testMacroFilterMti"));
        m.set(100, "");
        ISOMsg result = new MacroFilter().filter(new CSChannel(new ISOBaseValidatingPackager()), m, new LogEvent(
                "testMacroFilterTag", ""));
        assertSame("result", m, result);
    }

    @Test
    public void testFilter5() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        LogEvent evt = new LogEvent();
        ISOChannel channel = new NACChannel();

        when(m.getMaxField()).thenReturn(0);
        when(m.hasField(0)).thenReturn(true);
        when(m.getValue(0)).thenReturn("N/A in Composite");

        ISOMsg result = macroFilter.filter(channel, m, evt);
        assertSame("result", m, result);
    }

    @Test
    public void testFilter6() throws Throwable {
        ISOMsg m = new ISOMsg(100);
        m.set(100, "");
        ISOMsg result = new MacroFilter().filter(new CSChannel(new ISOBaseValidatingPackager()), m, new LogEvent(
                "testMacroFilterTag", ""));
        assertEquals("result.getDirection()", 0, result.getDirection());
    }

    @Test
    public void testFilterThrowsNullPointerException() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        LogEvent evt = new LogEvent();
        try {
            macroFilter.filter(new PADChannel("testMacroFilterHost", 100, new XMLPackager()), null, evt);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("macroFilter.cfg", macroFilter.cfg);
            assertNull("macroFilter.seq", macroFilter.seq);
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        Configuration cfg = new SimpleConfiguration();
        macroFilter.setConfiguration(cfg);
        assertNotNull("macroFilter.seq", macroFilter.seq);
        assertSame("macroFilter.cfg", cfg, macroFilter.cfg);
        assertEquals("macroFilter.unsetFields.length", 0, macroFilter.unsetFields.length);
        assertEquals("macroFilter.validFields.length", 0, macroFilter.validFields.length);
    }

    @Test
    public void testSetConfiguration1() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        Configuration cfg = new SimpleConfiguration();
        macroFilter.setConfiguration(cfg);
        macroFilter.setConfiguration(cfg);
        assertSame("macroFilter.cfg", cfg, macroFilter.cfg);
        assertEquals("macroFilter.unsetFields.length", 0, macroFilter.unsetFields.length);
        assertEquals("macroFilter.validFields.length", 0, macroFilter.validFields.length);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        MacroFilter macroFilter = new MacroFilter();
        Configuration cfg = new SubConfiguration();
        try {
            macroFilter.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame("macroFilter.cfg", cfg, macroFilter.cfg);
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("macroFilter.unsetFields.length", 0, macroFilter.unsetFields.length);
            assertEquals("macroFilter.validFields.length", 0, macroFilter.validFields.length);
            assertNull("macroFilter.seq", macroFilter.seq);
        }
    }
}
