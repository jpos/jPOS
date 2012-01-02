/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.channel.BASE24TCPChannel;
import org.jpos.iso.channel.GZIPChannel;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.channel.PostChannel;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.EuroSubFieldPackager;
import org.jpos.iso.packager.GenericValidatingPackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.jpos.iso.packager.PostPackager;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.LogEvent;
import org.junit.Test;

public class MD5FilterTest {

    @Test
    public void testConstructor() throws Throwable {
        new MD5Filter();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testFilter() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        m.setDirection(2);
        ISOMsg result = mD5Filter.filter(new ASCIIChannel(new EuroSubFieldPackager()), m, new LogEvent());
        assertEquals("m.getMaxField()", 128, m.getMaxField());
        assertSame("result", m, result);
    }

    @Test
    public void testFilter1() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        int[] fields = new int[1];
        fields[0] = -100;
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        m.setDirection(2);
        ISOMsg result = mD5Filter.filter(new ASCIIChannel(new ISOBaseValidatingPackager()), m, new LogEvent("testMD5FilterTag"));
        assertEquals("m.getMaxField()", 128, m.getMaxField());
        assertSame("result", m, result);
    }

    @Test
    public void testFilter2() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        int[] fields = new int[1];
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        m.setDirection(2);
        ISOMsg result = mD5Filter.filter(new ASCIIChannel(new ISOBaseValidatingPackager()), m, new LogEvent("testMD5FilterTag"));
        assertEquals("m.getMaxField()", 128, m.getMaxField());
        assertSame("result", m, result);
    }

    @Test
    public void testFilterThrowsNullPointerException() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        int[] fields = new int[1];
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        try {
            mD5Filter.filter(new GZIPChannel(new XMLPackager()), m, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsNullPointerException1() throws Throwable {
        int[] fields = new int[1];
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        mD5Filter.setFields(fields);
        LogEvent evt = new LogEvent("testMD5FilterTag", Integer.valueOf(-12));
        try {
            mD5Filter.filter(new PostChannel(new CTCSubFieldPackager()), null, evt);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFilterThrowsNullPointerException2() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        try {
            mD5Filter.filter(new ASCIIChannel(new GenericValidatingPackager()), m, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsNullPointerException3() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        int[] fields = new int[1];
        fields[0] = -100;
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        try {
            mD5Filter.filter(new GZIPChannel(new XMLPackager()), m, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsNullPointerException4() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        LogEvent evt = new LogEvent();
        try {
            mD5Filter.filter(new BASE24TCPChannel(), null, evt);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testFilterThrowsVetoException() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration(new Properties()));
        int[] fields = new int[3];
        fields[1] = 57;
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        LogEvent evt = new LogEvent("testMD5FilterTag", new XMLPackager());
        try {
            mD5Filter.filter(new ASCIIChannel(null), m, evt);
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("evt.payLoad.size()", 4, evt.getPayLoad().size());
            assertEquals("ex.getMessage()", "org.jpos.iso.ISOFilter$VetoException: invalid MAC", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "invalid MAC", ex.getNested().getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsVetoException1() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        m.setDirection(3);
        LogEvent evt = new LogEvent();
        try {
            mD5Filter.filter(new PostChannel("testMD5FilterHost", 100, new PostPackager()), m, evt);
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("evt.payLoad.size()", 3, evt.getPayLoad().size());
            assertEquals("ex.getMessage()", "org.jpos.iso.ISOFilter$VetoException: invalid MAC", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "invalid MAC", ex.getNested().getMessage());
            assertEquals("m.getDirection()", 3, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsVetoException2() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        int[] fields = new int[1];
        fields[0] = -100;
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        m.setDirection(3);
        LogEvent evt = new LogEvent("testMD5FilterTag");
        try {
            mD5Filter.filter(new ASCIIChannel(new ISOBaseValidatingPackager()), m, evt);
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("evt.payLoad.size()", 3, evt.getPayLoad().size());
            assertEquals("ex.getMessage()", "org.jpos.iso.ISOFilter$VetoException: invalid MAC", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "invalid MAC", ex.getNested().getMessage());
            assertEquals("m.getDirection()", 3, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsVetoException3() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        LogEvent evt = new LogEvent("testMD5FilterTag");
        int[] fields = new int[1];
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        try {
            mD5Filter.filter(new BASE24TCPChannel("testMD5FilterHost", 100, new PostPackager()), m, evt);
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("evt.payLoad.size()", 3, evt.getPayLoad().size());
            assertEquals("ex.getMessage()", "org.jpos.iso.ISOFilter$VetoException: invalid MAC", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "invalid MAC", ex.getNested().getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsVetoException4() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        int[] fields = new int[1];
        fields[0] = -100;
        mD5Filter.setFields(fields);
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        m.setDirection(1);
        LogEvent evt = new LogEvent("testMD5FilterTag");
        try {
            mD5Filter.filter(new ASCIIChannel(new ISOBaseValidatingPackager()), m, evt);
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("evt.payLoad.size()", 3, evt.getPayLoad().size());
            assertEquals("ex.getMessage()", "org.jpos.iso.ISOFilter$VetoException: invalid MAC", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "invalid MAC", ex.getNested().getMessage());
            assertEquals("m.getDirection()", 1, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsVetoException5() throws Throwable {
        ISOMsg m = new ISOMsg("testMD5FilterMti");
        LogEvent evt = new LogEvent();
        try {
            new MD5Filter().filter(new PostChannel(), m, evt);
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("ex.getMessage()", "MD5Filter not configured", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testFilterThrowsVetoException6() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        mD5Filter.setFields((int[]) null);
        try {
            mD5Filter.filter(new PADChannel(new PostPackager()), null, null);
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("ex.getMessage()", "MD5Filter not configured", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testGetFields() throws Throwable {
        int[] fields = new int[3];
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setFields(fields);
        int[] result = mD5Filter.getFields(new ISOMsg("testMD5FilterMti"));
        assertSame("result", fields, result);
        assertEquals("fields[0]", 0, fields[0]);
    }

    @Test
    public void testGetKey() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        byte[] result = mD5Filter.getKey();
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testGetKeyThrowsNullPointerException() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        try {
            mD5Filter.getKey();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setConfiguration(new SimpleConfiguration());
        assertEquals("mD5Filter.key", "", mD5Filter.key);
        assertEquals("mD5Filter.fields.length", 0, mD5Filter.fields.length);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        MD5Filter mD5Filter = new MD5Filter();
        Configuration cfg = new SubConfiguration();
        try {
            mD5Filter.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("mD5Filter.key", mD5Filter.key);
            assertNull("mD5Filter.fields", mD5Filter.fields);
        }
    }

    @Test
    public void testSetFields() throws Throwable {
        int[] fields = new int[3];
        MD5Filter mD5Filter = new MD5Filter();
        mD5Filter.setFields(fields);
        assertSame("mD5Filter.fields", fields, mD5Filter.fields);
    }
}
