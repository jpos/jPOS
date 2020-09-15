/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.jupiter.api.Test;

public class ChannelPoolTest {

    @Test
    public void testAddChannel1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        ISOChannel channel = new ASCIIChannel();
        channelPool.addChannel(channel);
        assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
        assertSame(channel, channelPool.pool.get(0), "channelPool.pool.get(0)");
    }

    @Test
    public void testAddChannelThrowsNotFoundException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.addChannel("testChannelPoolName1");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("channel.testChannelPoolName1", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
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
            assertEquals("port out of range:-1", ex.getMessage(), "ex.getMessage()");
            assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testConnectThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.connect();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals(IOException.class, ex.getClass(), "ex.getClass()");
            assertNull(channelPool.current, "channelPool.current");
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
            assertTrue(channelPool.usable, "channelPool.usable");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOChannel.connect()\" because \"c\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        assertEquals("", channelPool.name, "channelPool.name");
        assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
        assertNull(channelPool.cfg, "channelPool.cfg");
        assertTrue(channelPool.usable, "channelPool.usable");
    }

    @Test
    public void testDisconnect() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new BASE24TCPChannel());
        channelPool.disconnect();
        assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
        assertNull(channelPool.current, "channelPool.current");
    }

    @Test
    public void testDisconnect1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.disconnect();
        assertNull(channelPool.current, "channelPool.current");
    }

    @Test
    public void testDisconnectThrowsNullPointerException1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel((ISOChannel) null);
        try {
            channelPool.disconnect();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOChannel.disconnect()\" because \"c\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
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
            assertEquals("port out of range:-1", ex.getMessage(), "ex.getMessage()");
            assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testGetCurrentThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.getCurrent();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals(IOException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testGetLogger() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Logger logger = new Logger();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        Logger result = channelPool.getLogger();
        assertSame(logger, result, "result");
    }

    @Test
    public void testGetName() throws Throwable {
        String result = new ChannelPool().getName();
        assertEquals("", result, "result");
    }

    @Test
    public void testGetPackager() throws Throwable {
        ISOPackager result = new ChannelPool().getPackager();
        assertNull(result, "result");
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new ChannelPool().getRealm();
        assertNull(result, "result");
    }

    @Test
    public void testGetRealm1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(new Logger(), "testChannelPoolRealm");
        String result = channelPool.getRealm();
        assertEquals("testChannelPoolRealm", result, "result");
    }

    @Test
    public void testIsConnected() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        boolean result = channelPool.isConnected();
        assertFalse(result, "result");
        assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
        assertNull(channelPool.current, "channelPool.current");
    }

    @Test
    public void testIsConnectedThrowsIllegalArgumentException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel(new GZIPChannel("testChannelPoolHost", -1, new Base1SubFieldPackager()));
        try {
            channelPool.isConnected();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("port out of range:-1", ex.getMessage(), "ex.getMessage()");
            assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testIsConnectedDoNotThrowNullPointerExceptionWithNullLogListener() throws Throwable {
        Logger logger = new Logger();
        logger.addListener(null);
        ChannelPool channelPool = new ChannelPool();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        assertEquals(false, channelPool.isConnected(), "connected.isFalse");
        assertNull(channelPool.current, "channelPool.current");
        assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
        assertTrue(channelPool.usable, "channelPool.usable");
    }

    @Test
    public void testReceiveThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.receive();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals(IOException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
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
            assertEquals("port out of range:-1", ex.getMessage(), "ex.getMessage()");
            assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testReconnectThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.reconnect();
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals(IOException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testRemoveChannel() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.removeChannel(new BASE24TCPChannel());
        assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
    }

    @Test
    public void testRemoveChannelThrowsNotFoundException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.removeChannel("testChannelPoolName1");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("channel.testChannelPoolName1", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
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
            assertEquals("port out of range:-1", ex.getMessage(), "ex.getMessage()");
            assertEquals(1, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testSendThrowsIOException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        try {
            channelPool.send(new ISOMsg());
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals(IOException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
            assertNull(channelPool.current, "channelPool.current");
            assertTrue(channelPool.usable, "channelPool.usable");
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Configuration cfg = new SimpleConfiguration();
        channelPool.setConfiguration(cfg);
        assertSame(cfg, channelPool.cfg, "channelPool.cfg");
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Configuration cfg = new SubConfiguration();
        try {
            channelPool.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertSame(cfg, channelPool.cfg, "channelPool.cfg");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.getAll(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, channelPool.pool.size(), "channelPool.pool.size()");
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        Logger logger = new Logger();
        channelPool.setLogger(logger, "testChannelPoolRealm");
        assertSame(logger, channelPool.logger, "channelPool.logger");
        assertEquals("testChannelPoolRealm", channelPool.realm, "channelPool.realm");
    }

    @Test
    public void testSetName() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setName("testChannelPoolName");
        assertEquals("testChannelPoolName", channelPool.name, "channelPool.name");
    }

    @Test
    public void testSetPackager() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setPackager(new Base1SubFieldPackager());
        assertEquals("", channelPool.getName(), "channelPool.getName()");
    }

    @Test
    public void testSetUsable() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.setUsable(false);
        assertFalse(channelPool.usable, "channelPool.usable");
    }

    @Test
    public void testSize() throws Throwable {
        int result = new ChannelPool().size();
        assertEquals(0, result, "result");
    }

    @Test
    public void testSize1() throws Throwable {
        ChannelPool channelPool = new ChannelPool();
        channelPool.addChannel((ISOChannel) null);
        int result = channelPool.size();
        assertEquals(1, result, "result");
    }
}
