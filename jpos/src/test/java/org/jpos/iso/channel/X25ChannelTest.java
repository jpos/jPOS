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

import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;

import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.jpos.iso.packager.PostPackager;
import org.jpos.iso.packager.XMLPackager;
import org.junit.jupiter.api.Test;

public class X25ChannelTest {

    @Test
    public void testConnectThrowsNullPointerException() throws Throwable {
        X25Channel x25Channel = new X25Channel("testX25ChannelHost", 100, new ISO87APackagerBBitmap());
        x25Channel.setTimeout(1);
        Socket socket = new Socket(Proxy.NO_PROXY);
        try {
            x25Channel.connect(socket);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.net.InetAddress.getHostAddress()\" because the return value of \"java.net.Socket.getInetAddress()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(x25Channel.reader, "x25Channel.reader");
            assertEquals("org.jpos.iso.channel.X25Channel", x25Channel.getOriginalRealm(), "x25Channel.getOriginalRealm()");
            assertEquals(3, x25Channel.getCounters().length, "x25Channel.getCounters().length");
            assertNull(x25Channel.getRealm(), "x25Channel.getRealm()");
            assertSame(socket, x25Channel.getSocket(), "x25Channel.getSocket()");
            assertFalse(x25Channel.isConnected(), "x25Channel.isConnected()");
            assertNull(x25Channel.getLogger(), "x25Channel.getLogger()");
            assertNull(socket.getChannel(), "socket.getChannel()");
        }
    }

    @Test
    public void testConnectThrowsSocketException() throws Throwable {
        Socket socket = new Socket();
        X25Channel x25Channel = new X25Channel();
        socket.close();
        try {
            x25Channel.connect(socket);
            fail("Expected SocketException to be thrown");
        } catch (SocketException ex) {
            assertEquals(SocketException.class, ex.getClass(), "ex.getClass()");
            assertNull(x25Channel.reader, "x25Channel.reader");
            assertEquals("org.jpos.iso.channel.X25Channel", x25Channel.getOriginalRealm(), "x25Channel.getOriginalRealm()");
            assertEquals(3, x25Channel.getCounters().length, "x25Channel.getCounters().length");
            assertNull(x25Channel.getRealm(), "x25Channel.getRealm()");
            assertSame(socket, x25Channel.getSocket(), "x25Channel.getSocket()");
            assertFalse(x25Channel.isConnected(), "x25Channel.isConnected()");
            assertNull(x25Channel.getLogger(), "x25Channel.getLogger()");
            assertNull(socket.getChannel(), "socket.getChannel()");
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        X25Channel x25Channel = new X25Channel();
        assertEquals(0, x25Channel.getIncomingFilters().size(), "x25Channel.getIncomingFilters().size()");
        assertEquals(100000, x25Channel.getMaxPacketLength(), "x25Channel.getMaxPacketLength()");
        assertEquals(0, x25Channel.getPort(), "x25Channel.getPort()");
        assertEquals("", x25Channel.getName(), "x25Channel.getName()");
        assertNull(x25Channel.reader, "x25Channel.reader");
        assertEquals(3, x25Channel.getCounters().length, "x25Channel.getCounters().length");
        assertNull(x25Channel.getLogger(), "x25Channel.getLogger()");
        assertNull(x25Channel.getSocketFactory(), "x25Channel.getSocketFactory()");
        assertNull(x25Channel.getHeader(), "x25Channel.getHeader()");
        assertEquals(0, x25Channel.getOutgoingFilters().size(), "x25Channel.getOutgoingFilters().size()");
        assertNull(x25Channel.getServerSocket(), "x25Channel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.X25Channel", x25Channel.getOriginalRealm(), "x25Channel.getOriginalRealm()");
        assertNull(x25Channel.getRealm(), "x25Channel.getRealm()");
        assertNull(x25Channel.getHost(), "x25Channel.getHost()");
    }

    @Test
    public void testGetHeaderLength() throws Throwable {
        byte[] header = new byte[1];
        X25Channel x25Channel = new X25Channel("testX25ChannelHost", 100, null);
        x25Channel.setHeader(header);
        int result = x25Channel.getHeaderLength();
        assertEquals(1, result, "result");
    }

    @Test
    public void testGetHeaderLength1() throws Throwable {
        X25Channel x25Channel = new X25Channel();
        int result = x25Channel.getHeaderLength();
        assertEquals(0, result, "result");
    }

    @Test
    public void testSetHeader() throws Throwable {
        X25Channel x25Channel = new X25Channel(new PostPackager());
        x25Channel.setHeader("testX25ChannelHeader");
        assertEquals(10, x25Channel.header.length, "x25Channel.header.length");
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        X25Channel x25Channel = new X25Channel("testX25ChannelHost", 100, new XMLPackager());
        try {
            x25Channel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.getBytes()\" because \"header\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(x25Channel.header, "x25Channel.header");
        }
    }
}
