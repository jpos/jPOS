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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.BASE24Packager;
import org.jpos.iso.packager.EuroPackager;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.jpos.iso.packager.XMLPackager;
import org.junit.jupiter.api.Test;

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
            assertEquals(SocketException.class, ex.getClass(), "ex.getClass()");
            assertEquals("org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm(), "logChannel.getOriginalRealm()");
            assertEquals(3, logChannel.getCounters().length, "logChannel.getCounters().length");
            assertNull(logChannel.getRealm(), "logChannel.getRealm()");
            assertSame(socket, logChannel.getSocket(), "logChannel.getSocket()");
            assertFalse(logChannel.isConnected(), "logChannel.isConnected()");
            assertNull(logChannel.getLogger(), "logChannel.getLogger()");
            assertNull(logChannel.reader, "logChannel.reader");
            assertNull(socket.getChannel(), "socket.getChannel()");
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        LogChannel logChannel = new LogChannel();
        assertEquals(0, logChannel.getIncomingFilters().size(), "logChannel.getIncomingFilters().size()");
        assertEquals(100000, logChannel.getMaxPacketLength(), "logChannel.getMaxPacketLength()");
        assertEquals(0, logChannel.getPort(), "logChannel.getPort()");
        assertEquals("", logChannel.getName(), "logChannel.getName()");
        assertEquals(3, logChannel.getCounters().length, "logChannel.getCounters().length");
        assertNull(logChannel.getLogger(), "logChannel.getLogger()");
        assertNull(logChannel.getSocketFactory(), "logChannel.getSocketFactory()");
        assertNull(logChannel.getHeader(), "logChannel.getHeader()");
        assertEquals(0, logChannel.getOutgoingFilters().size(), "logChannel.getOutgoingFilters().size()");
        assertNull(logChannel.getServerSocket(), "logChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm(), "logChannel.getOriginalRealm()");
        assertNull(logChannel.getRealm(), "logChannel.getRealm()");
        assertNull(logChannel.reader, "logChannel.reader");
        assertNull(logChannel.getHost(), "logChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new EuroPackager();
        LogChannel logChannel = new LogChannel("testLogChannelHost", 100, p);
        assertEquals(0, logChannel.getIncomingFilters().size(), "logChannel.getIncomingFilters().size()");
        assertEquals(100000, logChannel.getMaxPacketLength(), "logChannel.getMaxPacketLength()");
        assertSame(p, logChannel.getPackager(), "logChannel.getPackager()");
        assertEquals(100, logChannel.getPort(), "logChannel.getPort()");
        assertEquals("", logChannel.getName(), "logChannel.getName()");
        assertEquals(3, logChannel.getCounters().length, "logChannel.getCounters().length");
        assertNull(logChannel.getLogger(), "logChannel.getLogger()");
        assertNull(logChannel.getSocketFactory(), "logChannel.getSocketFactory()");
        assertNull(logChannel.getHeader(), "logChannel.getHeader()");
        assertEquals(0, logChannel.getOutgoingFilters().size(), "logChannel.getOutgoingFilters().size()");
        assertNull(logChannel.getServerSocket(), "logChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm(), "logChannel.getOriginalRealm()");
        assertNull(logChannel.getRealm(), "logChannel.getRealm()");
        assertNull(logChannel.reader, "logChannel.reader");
        assertEquals("testLogChannelHost", logChannel.getHost(), "logChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new XMLPackager();
        ServerSocket serverSocket = new ServerSocket();
        LogChannel logChannel = new LogChannel(p, serverSocket);
        assertEquals(0, logChannel.getIncomingFilters().size(), "logChannel.getIncomingFilters().size()");
        assertEquals(100000, logChannel.getMaxPacketLength(), "logChannel.getMaxPacketLength()");
        assertSame(p, logChannel.getPackager(), "logChannel.getPackager()");
        assertEquals(0, logChannel.getPort(), "logChannel.getPort()");
        assertEquals("", logChannel.getName(), "logChannel.getName()");
        assertEquals(3, logChannel.getCounters().length, "logChannel.getCounters().length");
        assertNull(logChannel.getLogger(), "logChannel.getLogger()");
        assertNull(logChannel.getSocketFactory(), "logChannel.getSocketFactory()");
        assertNull(logChannel.getHeader(), "logChannel.getHeader()");
        assertEquals(0, logChannel.getOutgoingFilters().size(), "logChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, logChannel.getServerSocket(), "logChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm(), "logChannel.getOriginalRealm()");
        assertNull(logChannel.getRealm(), "logChannel.getRealm()");
        assertNull(logChannel.reader, "logChannel.reader");
        assertNull(logChannel.getHost(), "logChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new XMLPackager();
        LogChannel logChannel = new LogChannel(p);
        assertEquals(0, logChannel.getIncomingFilters().size(), "logChannel.getIncomingFilters().size()");
        assertEquals(100000, logChannel.getMaxPacketLength(), "logChannel.getMaxPacketLength()");
        assertSame(p, logChannel.getPackager(), "logChannel.getPackager()");
        assertEquals(0, logChannel.getPort(), "logChannel.getPort()");
        assertEquals("", logChannel.getName(), "logChannel.getName()");
        assertEquals(3, logChannel.getCounters().length, "logChannel.getCounters().length");
        assertNull(logChannel.getLogger(), "logChannel.getLogger()");
        assertNull(logChannel.getSocketFactory(), "logChannel.getSocketFactory()");
        assertNull(logChannel.getHeader(), "logChannel.getHeader()");
        assertEquals(0, logChannel.getOutgoingFilters().size(), "logChannel.getOutgoingFilters().size()");
        assertNull(logChannel.getServerSocket(), "logChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.LogChannel", logChannel.getOriginalRealm(), "logChannel.getOriginalRealm()");
        assertNull(logChannel.getRealm(), "logChannel.getRealm()");
        assertNull(logChannel.reader, "logChannel.reader");
        assertNull(logChannel.getHost(), "logChannel.getHost()");
    }

    @Test
    public void testDisconnect() throws Throwable {
        LogChannel logChannel = new LogChannel("testLogChannelHost", 100, new BASE24Packager());
        logChannel.disconnect();
        assertNull(logChannel.getServerSocket(), "logChannel.getServerSocket()");
        assertFalse(logChannel.isConnected(), "logChannel.isConnected()");
        assertNull(logChannel.getSocket(), "logChannel.getSocket()");
        assertNull(logChannel.reader, "logChannel.reader");
    }

    @Test
    public void testGetHeaderLength() throws Throwable {
        LogChannel logChannel = new LogChannel();
        int result = logChannel.getHeaderLength();
        assertEquals(0, result, "result");
    }

    @Test
    public void testStreamReceive() throws Throwable {
        LogChannel logChannel = new LogChannel("testLogChannelHost", 100, new EuroPackager());
        byte[] result = logChannel.streamReceive();
        assertEquals(0, result.length, "result.length");
    }
}
