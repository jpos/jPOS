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
import static org.junit.Assert.fail;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.BASE24Packager;
import org.jpos.iso.packager.EuroPackager;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.jpos.iso.packager.XMLPackager;
import org.junit.Test;

public class LogChannelTest {

    @Test
    public void testConnectThrowsSocketException() throws Throwable {
        LogChannel logChannel = new LogChannel(new ISO87APackagerBBitmap());
        Socket socket = new Socket();
        socket.close();
        try {
            logChannel.connect(socket);
            fail("Expected SocketException to be thrown");
        } catch (SocketException ex) {
            assertEquals("ex.getClass()", SocketException.class, ex.getClass());
            assertEquals("logChannel.getOriginalRealm()", "org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm());
            assertEquals("logChannel.getCounters().length", 3, logChannel.getCounters().length);
            assertNull("logChannel.getRealm()", logChannel.getRealm());
            assertSame("logChannel.getSocket()", socket, logChannel.getSocket());
            assertFalse("logChannel.isConnected()", logChannel.isConnected());
            assertNull("logChannel.getLogger()", logChannel.getLogger());
            assertNull("logChannel.reader", logChannel.reader);
            assertNull("socket.getChannel()", socket.getChannel());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        LogChannel logChannel = new LogChannel();
        assertEquals("logChannel.getIncomingFilters().size()", 0, logChannel.getIncomingFilters().size());
        assertEquals("logChannel.getMaxPacketLength()", 100000, logChannel.getMaxPacketLength());
        assertEquals("logChannel.getPort()", 0, logChannel.getPort());
        assertEquals("logChannel.getName()", "", logChannel.getName());
        assertEquals("logChannel.getCounters().length", 3, logChannel.getCounters().length);
        assertNull("logChannel.getLogger()", logChannel.getLogger());
        assertNull("logChannel.getSocketFactory()", logChannel.getSocketFactory());
        assertNull("logChannel.getHeader()", logChannel.getHeader());
        assertEquals("logChannel.getOutgoingFilters().size()", 0, logChannel.getOutgoingFilters().size());
        assertNull("logChannel.getServerSocket()", logChannel.getServerSocket());
        assertEquals("logChannel.getOriginalRealm()", "org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm());
        assertNull("logChannel.getRealm()", logChannel.getRealm());
        assertNull("logChannel.reader", logChannel.reader);
        assertNull("logChannel.getHost()", logChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new EuroPackager();
        LogChannel logChannel = new LogChannel("testLogChannelHost", 100, p);
        assertEquals("logChannel.getIncomingFilters().size()", 0, logChannel.getIncomingFilters().size());
        assertEquals("logChannel.getMaxPacketLength()", 100000, logChannel.getMaxPacketLength());
        assertSame("logChannel.getPackager()", p, logChannel.getPackager());
        assertEquals("logChannel.getPort()", 100, logChannel.getPort());
        assertEquals("logChannel.getName()", "", logChannel.getName());
        assertEquals("logChannel.getCounters().length", 3, logChannel.getCounters().length);
        assertNull("logChannel.getLogger()", logChannel.getLogger());
        assertNull("logChannel.getSocketFactory()", logChannel.getSocketFactory());
        assertNull("logChannel.getHeader()", logChannel.getHeader());
        assertEquals("logChannel.getOutgoingFilters().size()", 0, logChannel.getOutgoingFilters().size());
        assertNull("logChannel.getServerSocket()", logChannel.getServerSocket());
        assertEquals("logChannel.getOriginalRealm()", "org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm());
        assertNull("logChannel.getRealm()", logChannel.getRealm());
        assertNull("logChannel.reader", logChannel.reader);
        assertEquals("logChannel.getHost()", "testLogChannelHost", logChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new XMLPackager();
        ServerSocket serverSocket = new ServerSocket();
        LogChannel logChannel = new LogChannel(p, serverSocket);
        assertEquals("logChannel.getIncomingFilters().size()", 0, logChannel.getIncomingFilters().size());
        assertEquals("logChannel.getMaxPacketLength()", 100000, logChannel.getMaxPacketLength());
        assertSame("logChannel.getPackager()", p, logChannel.getPackager());
        assertEquals("logChannel.getPort()", 0, logChannel.getPort());
        assertEquals("logChannel.getName()", "", logChannel.getName());
        assertEquals("logChannel.getCounters().length", 3, logChannel.getCounters().length);
        assertNull("logChannel.getLogger()", logChannel.getLogger());
        assertNull("logChannel.getSocketFactory()", logChannel.getSocketFactory());
        assertNull("logChannel.getHeader()", logChannel.getHeader());
        assertEquals("logChannel.getOutgoingFilters().size()", 0, logChannel.getOutgoingFilters().size());
        assertSame("logChannel.getServerSocket()", serverSocket, logChannel.getServerSocket());
        assertEquals("logChannel.getOriginalRealm()", "org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm());
        assertNull("logChannel.getRealm()", logChannel.getRealm());
        assertNull("logChannel.reader", logChannel.reader);
        assertNull("logChannel.getHost()", logChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new XMLPackager();
        LogChannel logChannel = new LogChannel(p);
        assertEquals("logChannel.getIncomingFilters().size()", 0, logChannel.getIncomingFilters().size());
        assertEquals("logChannel.getMaxPacketLength()", 100000, logChannel.getMaxPacketLength());
        assertSame("logChannel.getPackager()", p, logChannel.getPackager());
        assertEquals("logChannel.getPort()", 0, logChannel.getPort());
        assertEquals("logChannel.getName()", "", logChannel.getName());
        assertEquals("logChannel.getCounters().length", 3, logChannel.getCounters().length);
        assertNull("logChannel.getLogger()", logChannel.getLogger());
        assertNull("logChannel.getSocketFactory()", logChannel.getSocketFactory());
        assertNull("logChannel.getHeader()", logChannel.getHeader());
        assertEquals("logChannel.getOutgoingFilters().size()", 0, logChannel.getOutgoingFilters().size());
        assertNull("logChannel.getServerSocket()", logChannel.getServerSocket());
        assertEquals("logChannel.getOriginalRealm()", "org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm());
        assertNull("logChannel.getRealm()", logChannel.getRealm());
        assertNull("logChannel.reader", logChannel.reader);
        assertNull("logChannel.getHost()", logChannel.getHost());
    }

    @Test
    public void testDisconnect() throws Throwable {
        LogChannel logChannel = new LogChannel("testLogChannelHost", 100, new BASE24Packager());
        logChannel.disconnect();
        assertNull("logChannel.getServerSocket()", logChannel.getServerSocket());
        assertFalse("logChannel.isConnected()", logChannel.isConnected());
        assertNull("logChannel.getSocket()", logChannel.getSocket());
        assertNull("logChannel.reader", logChannel.reader);
    }

    @Test
    public void testGetHeaderLength() throws Throwable {
        LogChannel logChannel = new LogChannel();
        int result = logChannel.getHeaderLength();
        assertEquals("result", 0, result);
    }

    @Test
    public void testStreamReceive() throws Throwable {
        LogChannel logChannel = new LogChannel("testLogChannelHost", 100, new EuroPackager());
        byte[] result = logChannel.streamReceive();
        assertEquals("result.length", 0, result.length);
    }
}
