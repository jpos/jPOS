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

package org.jpos.iso.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.junit.Test;

public class ChannelPoolTest {

    @Test
    public void testAddChannel1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        ISOChannel channel = new ASCIIChannel();
        channelPool.addChannel(channel);
        assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
        assertSame("channelPool.pool.get(0)", channel, channelPool.pool.get(0));
    }

    @Test
    public void testAddChannelThrowsNotFoundException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.addChannel("testChannelPoolName1");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("ex.getMessage()", "channel.testChannelPoolName1", ex.getMessage());
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
        }
    }

    @Test
    public void testConnectThrowsIllegalArgumentException() throws Throwable {
        byte[] TPDU = new byte[2];
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new NACChannel("testChannelPoolHost", -1, new ISOBaseValidatingPackager(), TPDU));
        try {
            channelPool.connect();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "port out of range:-1", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testConnectThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.connect();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals("ex.getClass()", IOException.class, ex.getClass());
            assertNull("channelPool.current", channelPool.current);
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testConnectThrowsNullPointerException() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(null);
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        try {
            channelPool.connect();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("channelPool.current", channelPool.current);
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testConnectThrowsNullPointerException1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel((ISOChannel) null);
        try {
            channelPool.connect();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        assertEquals("channelPool.name", "", channelPool.name);
        assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
        assertNull("channelPool.cfg", channelPool.cfg);
        assertTrue("channelPool.usable", channelPool.usable);
    }

    @Test
    public void testDisconnect() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new BASE24TCPChannel());
        channelPool.disconnect();
        assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
        assertNull("channelPool.current", channelPool.current);
    }

    @Test
    public void testDisconnect1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.disconnect();
        assertNull("channelPool.current", channelPool.current);
    }

    @Test
    public void testDisconnectThrowsNullPointerException1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel((ISOChannel) null);
        try {
            channelPool.disconnect();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
        }
    }

    @Test
    public void testDisconnectThrowsNullPointerException2() throws Throwable {
        Logger logger = new Logger();
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new ASCIIChannel());
        logger.addListener(null);
        channelPool.setLogger(logger, "testChannelPoolRealm");
        try {
            channelPool.disconnect();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
        }
    }

    @Test
    public void testDisconnectThrowsNullPointerException3() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(null);
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        try {
            channelPool.disconnect();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("channelPool.current", channelPool.current);
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
        }
    }

    @Test
    public void testGetCurrentThrowsIllegalArgumentException() throws Throwable {
        byte[] TPDU = new byte[2];
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new NACChannel("testChannelPoolHost", -1, new ISOBaseValidatingPackager(), TPDU));
        try {
            channelPool.getCurrent();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "port out of range:-1", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testGetCurrentThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.getCurrent();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals("ex.getClass()", IOException.class, ex.getClass());
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testGetLogger() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Logger logger = new Logger();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        Logger result = channelPool.getLogger();
        assertSame("result", logger, result);
    }

    @Test
    public void testGetName() throws Throwable {
        String result = new ChannelPool().getName();
        assertEquals("result", "", result);
    }

    @Test
    public void testGetPackager() throws Throwable {
        ISOPackager result = new ChannelPool().getPackager();
        assertNull("result", result);
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new ChannelPool().getRealm();
        assertNull("result", result);
    }

    @Test
    public void testGetRealm1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(new Logger(), "testChannelPoolRealm");
        String result = channelPool.getRealm();
        assertEquals("result", "testChannelPoolRealm", result);
    }

    @Test
    public void testIsConnected() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        boolean result = channelPool.isConnected();
        assertFalse("result", result);
        assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
        assertNull("channelPool.current", channelPool.current);
    }

    @Test
    public void testIsConnectedThrowsIllegalArgumentException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new GZIPChannel("testChannelPoolHost", -1, new Base1SubFieldPackager()));
        try {
            channelPool.isConnected();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "port out of range:-1", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testIsConnectedThrowsNullPointerException() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(null);
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        try {
            channelPool.isConnected();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("channelPool.current", channelPool.current);
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testReceiveThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.receive();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals("ex.getClass()", IOException.class, ex.getClass());
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testReceiveThrowsNullPointerException() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(null);
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        try {
            channelPool.receive();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("channelPool.current", channelPool.current);
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testReconnectThrowsIllegalArgumentException() throws Throwable {
        byte[] TPDU = new byte[2];
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new NACChannel("testChannelPoolHost", -1, new ISOBaseValidatingPackager(), TPDU));
        try {
            channelPool.reconnect();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "port out of range:-1", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testReconnectThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.reconnect();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals("ex.getClass()", IOException.class, ex.getClass());
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testRemoveChannel() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.removeChannel(new BASE24TCPChannel());
        assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
    }

    @Test
    public void testRemoveChannelThrowsNotFoundException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.removeChannel("testChannelPoolName1");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("ex.getMessage()", "channel.testChannelPoolName1", ex.getMessage());
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
        }
    }

    @Test
    public void testSendThrowsIllegalArgumentException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new GZIPChannel("testChannelPoolHost", -1, new Base1SubFieldPackager()));
        try {
            channelPool.send((ISOMsg) new ISOMsg().clone());
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "port out of range:-1", ex.getMessage());
            assertEquals("channelPool.pool.size()", 1, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testSendThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.send(new ISOMsg());
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals("ex.getClass()", IOException.class, ex.getClass());
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertNull("channelPool.current", channelPool.current);
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testSendThrowsNullPointerException() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(null);
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        try {
            channelPool.send(new ISOMsg("testChannelPoolMti"));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("channelPool.current", channelPool.current);
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
            assertTrue("channelPool.usable", channelPool.usable);
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Configuration cfg = new SimpleConfiguration();
        channelPool.setConfiguration(cfg);
        assertSame("channelPool.cfg", cfg, channelPool.cfg);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Configuration cfg = new SubConfiguration();
        try {
            channelPool.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame("channelPool.cfg", cfg, channelPool.cfg);
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("channelPool.pool.size()", 0, channelPool.pool.size());
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Logger logger = new Logger();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        assertSame("channelPool.logger", logger, channelPool.logger);
        assertEquals("channelPool.realm", "testChannelPoolRealm", channelPool.realm);
    }

    @Test
    public void testSetName() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setName("testChannelPoolName");
        assertEquals("channelPool.name", "testChannelPoolName", channelPool.name);
    }

    @Test
    public void testSetPackager() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setPackager(new Base1SubFieldPackager());
        assertEquals("channelPool.getName()", "", channelPool.getName());
    }

    @Test
    public void testSetUsable() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setUsable(false);
        assertFalse("channelPool.usable", channelPool.usable);
    }

    @Test
    public void testSize() throws Throwable {
        int result = new ChannelPool().size();
        assertEquals("result", 0, result);
    }

    @Test
    public void testSize1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel((ISOChannel) null);
        int result = channelPool.size();
        assertEquals("result", 1, result);
    }
}
