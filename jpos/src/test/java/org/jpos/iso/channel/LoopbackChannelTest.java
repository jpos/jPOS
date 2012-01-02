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

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;
import org.junit.Test;

public class LoopbackChannelTest {

    @Test
    public void testConnect() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.connect();
        assertEquals("loopbackChannel.getCounters()[0]", 1, loopbackChannel.getCounters()[0]);
        assertEquals("loopbackChannel.getCounters().length", 3, loopbackChannel.getCounters().length);
        assertTrue("loopbackChannel.usable", loopbackChannel.usable);
    }

    @Test
    public void testConstructor() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        assertEquals("loopbackChannel.getOutgoingFilters().size()", 0, loopbackChannel.getOutgoingFilters().size());
        assertEquals("loopbackChannel.queue.consumerCount()", 0, loopbackChannel.queue.consumerCount());
        assertEquals("loopbackChannel.getIncomingFilters().size()", 0, loopbackChannel.getIncomingFilters().size());
        assertEquals("loopbackChannel.getCounters().length", 3, loopbackChannel.getCounters().length);
        assertTrue("loopbackChannel.usable", loopbackChannel.usable);
    }

    @Test
    public void testDisconnect() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.disconnect();
        assertFalse("loopbackChannel.usable", loopbackChannel.usable);
    }

    @Test
    public void testGetCounters() throws Throwable {
        int[] result = new LoopbackChannel().getCounters();
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", 0, result[0]);
    }

    @Test
    public void testGetLogger() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        Logger logger = new Logger();
        loopbackChannel.setLogger(logger, "testLoopbackChannelRealm");
        Logger result = loopbackChannel.getLogger();
        assertSame("result", logger, result);
    }

    @Test
    public void testGetPackager() throws Throwable {
        ISOPackager result = new LoopbackChannel().getPackager();
        assertNull("result", result);
    }

    @Test
    public void testGetRealm() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.setLogger(new Logger(), "testLoopbackChannelRealm");
        String result = loopbackChannel.getRealm();
        assertEquals("result", "testLoopbackChannelRealm", result);
    }

    @Test
    public void testGetRealm1() throws Throwable {
        String result = new LoopbackChannel().getRealm();
        assertNull("result", result);
    }

    @Test
    public void testIsConnected() throws Throwable {
        boolean result = new LoopbackChannel().isConnected();
        assertTrue("result", result);
    }

    @Test
    public void testReconnect() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.reconnect();
        assertTrue("loopbackChannel.usable", loopbackChannel.usable);
    }

    @Test
    public void testResetCounters() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.resetCounters();
        assertEquals("loopbackChannel.getCounters().length", 3, loopbackChannel.getCounters().length);
    }

    @Test
    public void testSendThrowsISOException() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.setUsable(false);
        try {
            loopbackChannel.send(new ISOMsg(100));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "unconnected ISOChannel", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertEquals("loopbackChannel.queue.consumerCount()", 0, loopbackChannel.queue.consumerCount());
            assertEquals("loopbackChannel.getCounters().length", 3, loopbackChannel.getCounters().length);
        }
    }

    @Test
    public void testSendThrowsNullPointerException() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        try {
            loopbackChannel.send((ISOMsg) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("loopbackChannel.queue.consumerCount()", 0, loopbackChannel.queue.consumerCount());
            assertEquals("loopbackChannel.getCounters().length", 3, loopbackChannel.getCounters().length);
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        Logger logger = new Logger();
        loopbackChannel.setLogger(logger, "testLoopbackChannelRealm");
        assertSame("loopbackChannel.logger", logger, loopbackChannel.logger);
        assertEquals("loopbackChannel.realm", "testLoopbackChannelRealm", loopbackChannel.realm);
    }

    @Test
    public void testSetName() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.setName("testLoopbackChannelName");
        assertEquals("loopbackChannel.name", "testLoopbackChannelName", loopbackChannel.name);
    }

    @Test
    public void testSetPackager() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.setPackager(new GenericPackager());
        assertNull("loopbackChannel.getName()", loopbackChannel.getName());
    }

    @Test
    public void testSetUsable() throws Throwable {
        LoopbackChannel loopbackChannel = new LoopbackChannel();
        loopbackChannel.setUsable(false);
        assertFalse("loopbackChannel.usable", loopbackChannel.usable);
    }
}
