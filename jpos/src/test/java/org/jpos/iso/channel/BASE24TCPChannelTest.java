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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.iso.packager.ISO93APackager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BASE24TCPChannelTest {
    @Mock
    ISOMsg m;

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new ISO87APackager();
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel(p);
        assertEquals("bASE24TCPChannel.getIncomingFilters().size()", 0, bASE24TCPChannel.getIncomingFilters().size());
        assertEquals("bASE24TCPChannel.getMaxPacketLength()", 100000, bASE24TCPChannel.getMaxPacketLength());
        assertSame("bASE24TCPChannel.getPackager()", p, bASE24TCPChannel.getPackager());
        assertEquals("bASE24TCPChannel.getPort()", 0, bASE24TCPChannel.getPort());
        assertEquals("bASE24TCPChannel.getName()", "", bASE24TCPChannel.getName());
        assertEquals("bASE24TCPChannel.getCounters().length", 3, bASE24TCPChannel.getCounters().length);
        assertNull("bASE24TCPChannel.getLogger()", bASE24TCPChannel.getLogger());
        assertNull("bASE24TCPChannel.getSocketFactory()", bASE24TCPChannel.getSocketFactory());
        assertNull("bASE24TCPChannel.getHeader()", bASE24TCPChannel.getHeader());
        assertEquals("bASE24TCPChannel.getOutgoingFilters().size()", 0, bASE24TCPChannel.getOutgoingFilters().size());
        assertNull("bASE24TCPChannel.getServerSocket()", bASE24TCPChannel.getServerSocket());
        assertEquals("bASE24TCPChannel.getOriginalRealm()", "org.jpos.iso.channel.BASE24TCPChannel",
                bASE24TCPChannel.getOriginalRealm());
        assertNull("bASE24TCPChannel.getRealm()", bASE24TCPChannel.getRealm());
        assertNull("bASE24TCPChannel.getHost()", bASE24TCPChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO87APackager();
        ServerSocket serverSocket = new ServerSocket();
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel(p, serverSocket);
        assertEquals("bASE24TCPChannel.getIncomingFilters().size()", 0, bASE24TCPChannel.getIncomingFilters().size());
        assertEquals("bASE24TCPChannel.getMaxPacketLength()", 100000, bASE24TCPChannel.getMaxPacketLength());
        assertSame("bASE24TCPChannel.getPackager()", p, bASE24TCPChannel.getPackager());
        assertEquals("bASE24TCPChannel.getPort()", 0, bASE24TCPChannel.getPort());
        assertEquals("bASE24TCPChannel.getName()", "", bASE24TCPChannel.getName());
        assertEquals("bASE24TCPChannel.getCounters().length", 3, bASE24TCPChannel.getCounters().length);
        assertNull("bASE24TCPChannel.getLogger()", bASE24TCPChannel.getLogger());
        assertNull("bASE24TCPChannel.getSocketFactory()", bASE24TCPChannel.getSocketFactory());
        assertNull("bASE24TCPChannel.getHeader()", bASE24TCPChannel.getHeader());
        assertEquals("bASE24TCPChannel.getOutgoingFilters().size()", 0, bASE24TCPChannel.getOutgoingFilters().size());
        assertSame("bASE24TCPChannel.getServerSocket()", serverSocket, bASE24TCPChannel.getServerSocket());
        assertEquals("bASE24TCPChannel.getOriginalRealm()", "org.jpos.iso.channel.BASE24TCPChannel",
                bASE24TCPChannel.getOriginalRealm());
        assertNull("bASE24TCPChannel.getRealm()", bASE24TCPChannel.getRealm());
        assertNull("bASE24TCPChannel.getHost()", bASE24TCPChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new ISO93APackager();
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel("testBASE24TCPChannelHost", 100, p);
        assertEquals("bASE24TCPChannel.getIncomingFilters().size()", 0, bASE24TCPChannel.getIncomingFilters().size());
        assertEquals("bASE24TCPChannel.getMaxPacketLength()", 100000, bASE24TCPChannel.getMaxPacketLength());
        assertSame("bASE24TCPChannel.getPackager()", p, bASE24TCPChannel.getPackager());
        assertEquals("bASE24TCPChannel.getPort()", 100, bASE24TCPChannel.getPort());
        assertEquals("bASE24TCPChannel.getName()", "", bASE24TCPChannel.getName());
        assertEquals("bASE24TCPChannel.getCounters().length", 3, bASE24TCPChannel.getCounters().length);
        assertNull("bASE24TCPChannel.getLogger()", bASE24TCPChannel.getLogger());
        assertNull("bASE24TCPChannel.getSocketFactory()", bASE24TCPChannel.getSocketFactory());
        assertNull("bASE24TCPChannel.getHeader()", bASE24TCPChannel.getHeader());
        assertEquals("bASE24TCPChannel.getOutgoingFilters().size()", 0, bASE24TCPChannel.getOutgoingFilters().size());
        assertNull("bASE24TCPChannel.getServerSocket()", bASE24TCPChannel.getServerSocket());
        assertEquals("bASE24TCPChannel.getOriginalRealm()", "org.jpos.iso.channel.BASE24TCPChannel",
                bASE24TCPChannel.getOriginalRealm());
        assertNull("bASE24TCPChannel.getRealm()", bASE24TCPChannel.getRealm());
        assertEquals("bASE24TCPChannel.getHost()", "testBASE24TCPChannelHost", bASE24TCPChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel();
        assertEquals("bASE24TCPChannel.getIncomingFilters().size()", 0, bASE24TCPChannel.getIncomingFilters().size());
        assertEquals("bASE24TCPChannel.getMaxPacketLength()", 100000, bASE24TCPChannel.getMaxPacketLength());
        assertEquals("bASE24TCPChannel.getPort()", 0, bASE24TCPChannel.getPort());
        assertEquals("bASE24TCPChannel.getName()", "", bASE24TCPChannel.getName());
        assertEquals("bASE24TCPChannel.getCounters().length", 3, bASE24TCPChannel.getCounters().length);
        assertNull("bASE24TCPChannel.getLogger()", bASE24TCPChannel.getLogger());
        assertNull("bASE24TCPChannel.getSocketFactory()", bASE24TCPChannel.getSocketFactory());
        assertNull("bASE24TCPChannel.getHeader()", bASE24TCPChannel.getHeader());
        assertEquals("bASE24TCPChannel.getOutgoingFilters().size()", 0, bASE24TCPChannel.getOutgoingFilters().size());
        assertNull("bASE24TCPChannel.getServerSocket()", bASE24TCPChannel.getServerSocket());
        assertEquals("bASE24TCPChannel.getOriginalRealm()", "org.jpos.iso.channel.BASE24TCPChannel",
                bASE24TCPChannel.getOriginalRealm());
        assertNull("bASE24TCPChannel.getRealm()", bASE24TCPChannel.getRealm());
        assertNull("bASE24TCPChannel.getHost()", bASE24TCPChannel.getHost());
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel(new ISO87APackager());
        try {
            bASE24TCPChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageTraillerThrowsNullPointerException() throws Throwable {
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel();

        try {
            bASE24TCPChannel.sendMessageTrailler(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
