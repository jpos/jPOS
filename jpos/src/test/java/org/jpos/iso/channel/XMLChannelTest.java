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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.PostPackager;
import org.junit.jupiter.api.Test;

public class XMLChannelTest {

    @Test
    public void testConnectThrowsNullPointerException() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel();
        xMLChannel.setTimeout(1);
        Socket socket = new Socket();
        try {
            xMLChannel.connect(socket);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.net.InetAddress.getHostAddress()\" because the return value of \"java.net.Socket.getInetAddress()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(xMLChannel.getLogger(), "xMLChannel.getLogger()");
            assertEquals("org.jpos.iso.channel.XMLChannel", xMLChannel.getOriginalRealm(), "xMLChannel.getOriginalRealm()");
            assertNull(xMLChannel.reader, "xMLChannel.reader");
            assertSame(socket, xMLChannel.getSocket(), "xMLChannel.getSocket()");
            assertEquals(3, xMLChannel.getCounters().length, "xMLChannel.getCounters().length");
            assertNull(xMLChannel.getRealm(), "xMLChannel.getRealm()");
            assertFalse(xMLChannel.isConnected(), "xMLChannel.isConnected()");
            assertNull(socket.getChannel(), "socket.getChannel()");
        }
    }

    @Test
    public void testConnectThrowsNullPointerException1() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel();
        xMLChannel.setTimeout(-1);
        Socket socket = new Socket(Proxy.NO_PROXY);
        try {
            xMLChannel.connect(socket);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.net.InetAddress.getHostAddress()\" because the return value of \"java.net.Socket.getInetAddress()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(xMLChannel.getLogger(), "xMLChannel.getLogger()");
            assertEquals("org.jpos.iso.channel.XMLChannel", xMLChannel.getOriginalRealm(), "xMLChannel.getOriginalRealm()");
            assertNull(xMLChannel.reader, "xMLChannel.reader");
            assertSame(socket, xMLChannel.getSocket(), "xMLChannel.getSocket()");
            assertEquals(3, xMLChannel.getCounters().length, "xMLChannel.getCounters().length");
            assertNull(xMLChannel.getRealm(), "xMLChannel.getRealm()");
            assertFalse(xMLChannel.isConnected(), "xMLChannel.isConnected()");
            assertNull(socket.getChannel(), "socket.getChannel()");
        }
    }

    @Test
    public void testConnectThrowsSocketException() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel();
        Socket socket = new Socket(Proxy.NO_PROXY);
        socket.close();
        try {
            xMLChannel.connect(socket);
            fail("Expected SocketException to be thrown");
        } catch (SocketException ex) {
            assertEquals(SocketException.class, ex.getClass(), "ex.getClass()");
            assertNull(xMLChannel.getLogger(), "xMLChannel.getLogger()");
            assertEquals("org.jpos.iso.channel.XMLChannel", xMLChannel.getOriginalRealm(), "xMLChannel.getOriginalRealm()");
            assertNull(xMLChannel.reader, "xMLChannel.reader");
            assertSame(socket, xMLChannel.getSocket(), "xMLChannel.getSocket()");
            assertEquals(3, xMLChannel.getCounters().length, "xMLChannel.getCounters().length");
            assertNull(xMLChannel.getRealm(), "xMLChannel.getRealm()");
            assertFalse(xMLChannel.isConnected(), "xMLChannel.isConnected()");
            assertNull(socket.getChannel(), "socket.getChannel()");
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel();
        assertNull(xMLChannel.getLogger(), "xMLChannel.getLogger()");
        assertEquals(0, xMLChannel.getIncomingFilters().size(), "xMLChannel.getIncomingFilters().size()");
        assertEquals(0, xMLChannel.getPort(), "xMLChannel.getPort()");
        assertNull(xMLChannel.getHeader(), "xMLChannel.getHeader()");
        assertNull(xMLChannel.getSocketFactory(), "xMLChannel.getSocketFactory()");
        assertNull(xMLChannel.getServerSocket(), "xMLChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.XMLChannel", xMLChannel.getOriginalRealm(), "xMLChannel.getOriginalRealm()");
        assertNull(xMLChannel.reader, "xMLChannel.reader");
        assertNull(xMLChannel.getHost(), "xMLChannel.getHost()");
        assertEquals(100000, xMLChannel.getMaxPacketLength(), "xMLChannel.getMaxPacketLength()");
        assertEquals("", xMLChannel.getName(), "xMLChannel.getName()");
        assertEquals(0, xMLChannel.getOutgoingFilters().size(), "xMLChannel.getOutgoingFilters().size()");
        assertEquals(3, xMLChannel.getCounters().length, "xMLChannel.getCounters().length");
        assertNull(xMLChannel.getRealm(), "xMLChannel.getRealm()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ServerSocket serverSocket = new ServerSocket();
        ISOPackager p = new PostPackager();
        XMLChannel xMLChannel = new XMLChannel(p, serverSocket);
        assertSame(p, xMLChannel.getPackager(), "xMLChannel.getPackager()");
        assertNull(xMLChannel.getLogger(), "xMLChannel.getLogger()");
        assertEquals(0, xMLChannel.getIncomingFilters().size(), "xMLChannel.getIncomingFilters().size()");
        assertEquals(0, xMLChannel.getPort(), "xMLChannel.getPort()");
        assertNull(xMLChannel.getHeader(), "xMLChannel.getHeader()");
        assertNull(xMLChannel.getSocketFactory(), "xMLChannel.getSocketFactory()");
        assertSame(serverSocket, xMLChannel.getServerSocket(), "xMLChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.XMLChannel", xMLChannel.getOriginalRealm(), "xMLChannel.getOriginalRealm()");
        assertNull(xMLChannel.reader, "xMLChannel.reader");
        assertNull(xMLChannel.getHost(), "xMLChannel.getHost()");
        assertEquals(100000, xMLChannel.getMaxPacketLength(), "xMLChannel.getMaxPacketLength()");
        assertEquals("", xMLChannel.getName(), "xMLChannel.getName()");
        assertEquals(3, xMLChannel.getCounters().length, "xMLChannel.getCounters().length");
        assertEquals(0, xMLChannel.getOutgoingFilters().size(), "xMLChannel.getOutgoingFilters().size()");
        assertNull(xMLChannel.getRealm(), "xMLChannel.getRealm()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new Base1Packager();
        XMLChannel xMLChannel = new XMLChannel("testXMLChannelHost", 100, p);
        assertSame(p, xMLChannel.getPackager(), "xMLChannel.getPackager()");
        assertNull(xMLChannel.getLogger(), "xMLChannel.getLogger()");
        assertEquals(0, xMLChannel.getIncomingFilters().size(), "xMLChannel.getIncomingFilters().size()");
        assertEquals(100, xMLChannel.getPort(), "xMLChannel.getPort()");
        assertNull(xMLChannel.getHeader(), "xMLChannel.getHeader()");
        assertNull(xMLChannel.getSocketFactory(), "xMLChannel.getSocketFactory()");
        assertNull(xMLChannel.getServerSocket(), "xMLChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.XMLChannel", xMLChannel.getOriginalRealm(), "xMLChannel.getOriginalRealm()");
        assertNull(xMLChannel.reader, "xMLChannel.reader");
        assertEquals("testXMLChannelHost", xMLChannel.getHost(), "xMLChannel.getHost()");
        assertEquals(100000, xMLChannel.getMaxPacketLength(), "xMLChannel.getMaxPacketLength()");
        assertEquals("", xMLChannel.getName(), "xMLChannel.getName()");
        assertEquals(3, xMLChannel.getCounters().length, "xMLChannel.getCounters().length");
        assertEquals(0, xMLChannel.getOutgoingFilters().size(), "xMLChannel.getOutgoingFilters().size()");
        assertNull(xMLChannel.getRealm(), "xMLChannel.getRealm()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new Base1Packager();
        XMLChannel xMLChannel = new XMLChannel(p);
        assertSame(p, xMLChannel.getPackager(), "xMLChannel.getPackager()");
        assertNull(xMLChannel.getLogger(), "xMLChannel.getLogger()");
        assertEquals(0, xMLChannel.getIncomingFilters().size(), "xMLChannel.getIncomingFilters().size()");
        assertEquals(0, xMLChannel.getPort(), "xMLChannel.getPort()");
        assertNull(xMLChannel.getHeader(), "xMLChannel.getHeader()");
        assertNull(xMLChannel.getSocketFactory(), "xMLChannel.getSocketFactory()");
        assertNull(xMLChannel.getServerSocket(), "xMLChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.XMLChannel", xMLChannel.getOriginalRealm(), "xMLChannel.getOriginalRealm()");
        assertNull(xMLChannel.reader, "xMLChannel.reader");
        assertNull(xMLChannel.getHost(), "xMLChannel.getHost()");
        assertEquals(100000, xMLChannel.getMaxPacketLength(), "xMLChannel.getMaxPacketLength()");
        assertEquals("", xMLChannel.getName(), "xMLChannel.getName()");
        assertEquals(3, xMLChannel.getCounters().length, "xMLChannel.getCounters().length");
        assertEquals(0, xMLChannel.getOutgoingFilters().size(), "xMLChannel.getOutgoingFilters().size()");
        assertNull(xMLChannel.getRealm(), "xMLChannel.getRealm()");
    }

    @Test
    public void testDisconnect() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel();
        xMLChannel.disconnect();
        assertNull(xMLChannel.getServerSocket(), "xMLChannel.getServerSocket()");
        assertNull(xMLChannel.reader, "xMLChannel.reader");
        assertNull(xMLChannel.getSocket(), "xMLChannel.getSocket()");
        assertFalse(xMLChannel.isConnected(), "xMLChannel.isConnected()");
    }

    @Test
    public void testGetHeaderLength() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel();
        int result = xMLChannel.getHeaderLength();
        assertEquals(0, result, "result");
    }

    @Test
    public void testSendMessageHeader() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel(null, new ServerSocket());
        xMLChannel.sendMessageHeader(new ISOMsg("testXMLChannelMti"), 100);
        assertEquals(0, xMLChannel.getHeaderLength(), "xMLChannel.getHeaderLength()");
    }

    @Test
    public void testStreamReceive() throws Throwable {
        XMLChannel xMLChannel = new XMLChannel(new PostPackager(), new ServerSocket());
        try {
            xMLChannel.streamReceive();
        } catch (EOFException e) {
            return;
        }
        fail ("EOFException should have been raised");
    }
}
