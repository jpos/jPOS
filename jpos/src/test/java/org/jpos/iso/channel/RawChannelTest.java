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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.net.ServerSocket;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.BASE24Packager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.jpos.iso.packager.VISA1Packager;
import org.jpos.iso.packager.XMLPackager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RawChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        assertEquals(0, rawChannel.getIncomingFilters().size(), "rawChannel.getIncomingFilters().size()");
        assertEquals(100000, rawChannel.getMaxPacketLength(), "rawChannel.getMaxPacketLength()");
        assertEquals(0, rawChannel.getPort(), "rawChannel.getPort()");
        assertEquals("", rawChannel.getName(), "rawChannel.getName()");
        assertEquals(3, rawChannel.getCounters().length, "rawChannel.getCounters().length");
        assertNull(rawChannel.getLogger(), "rawChannel.getLogger()");
        assertNull(rawChannel.getSocketFactory(), "rawChannel.getSocketFactory()");
        assertNull(rawChannel.getHeader(), "rawChannel.getHeader()");
        assertEquals(0, rawChannel.getOutgoingFilters().size(), "rawChannel.getOutgoingFilters().size()");
        assertNull(rawChannel.getServerSocket(), "rawChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm(), "rawChannel.getOriginalRealm()");
        assertNull(rawChannel.getRealm(), "rawChannel.getRealm()");
        assertNull(rawChannel.getHost(), "rawChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] header = new byte[3];
        ISOPackager p = new ISOBaseValidatingPackager();
        RawChannel rawChannel = new RawChannel("testRawChannelHost", 100, p, header);
        assertEquals(0, rawChannel.getIncomingFilters().size(), "rawChannel.getIncomingFilters().size()");
        assertEquals(100000, rawChannel.getMaxPacketLength(), "rawChannel.getMaxPacketLength()");
        assertSame(p, rawChannel.getPackager(), "rawChannel.getPackager()");
        assertEquals(100, rawChannel.getPort(), "rawChannel.getPort()");
        assertEquals("", rawChannel.getName(), "rawChannel.getName()");
        assertEquals(3, rawChannel.getCounters().length, "rawChannel.getCounters().length");
        assertNull(rawChannel.getLogger(), "rawChannel.getLogger()");
        assertNull(rawChannel.getSocketFactory(), "rawChannel.getSocketFactory()");
        assertSame(header, rawChannel.getHeader(), "rawChannel.getHeader()");
        assertEquals(0, rawChannel.getOutgoingFilters().size(), "rawChannel.getOutgoingFilters().size()");
        assertNull(rawChannel.getServerSocket(), "rawChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm(), "rawChannel.getOriginalRealm()");
        assertNull(rawChannel.getRealm(), "rawChannel.getRealm()");
        assertEquals("testRawChannelHost", rawChannel.getHost(), "rawChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        byte[] header = new byte[0];
        ISOPackager p = new BASE24Packager();
        RawChannel rawChannel = new RawChannel(p, header);
        assertEquals(0, rawChannel.getIncomingFilters().size(), "rawChannel.getIncomingFilters().size()");
        assertEquals(100000, rawChannel.getMaxPacketLength(), "rawChannel.getMaxPacketLength()");
        assertSame(p, rawChannel.getPackager(), "rawChannel.getPackager()");
        assertEquals(0, rawChannel.getPort(), "rawChannel.getPort()");
        assertEquals("", rawChannel.getName(), "rawChannel.getName()");
        assertEquals(3, rawChannel.getCounters().length, "rawChannel.getCounters().length");
        assertNull(rawChannel.getLogger(), "rawChannel.getLogger()");
        assertNull(rawChannel.getSocketFactory(), "rawChannel.getSocketFactory()");
        assertSame(header, rawChannel.getHeader(), "rawChannel.getHeader()");
        assertEquals(0, rawChannel.getOutgoingFilters().size(), "rawChannel.getOutgoingFilters().size()");
        assertNull(rawChannel.getServerSocket(), "rawChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm(), "rawChannel.getOriginalRealm()");
        assertNull(rawChannel.getRealm(), "rawChannel.getRealm()");
        assertNull(rawChannel.getHost(), "rawChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        byte[] header = new byte[0];
        ISOPackager p = new XMLPackager();
        ServerSocket serverSocket = new ServerSocket();
        RawChannel rawChannel = new RawChannel(p, header, serverSocket);
        assertEquals(0, rawChannel.getIncomingFilters().size(), "rawChannel.getIncomingFilters().size()");
        assertEquals(100000, rawChannel.getMaxPacketLength(), "rawChannel.getMaxPacketLength()");
        assertSame(p, rawChannel.getPackager(), "rawChannel.getPackager()");
        assertEquals(0, rawChannel.getPort(), "rawChannel.getPort()");
        assertEquals("", rawChannel.getName(), "rawChannel.getName()");
        assertEquals(3, rawChannel.getCounters().length, "rawChannel.getCounters().length");
        assertNull(rawChannel.getLogger(), "rawChannel.getLogger()");
        assertNull(rawChannel.getSocketFactory(), "rawChannel.getSocketFactory()");
        assertSame(header, rawChannel.getHeader(), "rawChannel.getHeader()");
        assertEquals(0, rawChannel.getOutgoingFilters().size(), "rawChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, rawChannel.getServerSocket(), "rawChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm(), "rawChannel.getOriginalRealm()");
        assertNull(rawChannel.getRealm(), "rawChannel.getRealm()");
        assertNull(rawChannel.getHost(), "rawChannel.getHost()");
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        try {
            rawChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataInputStream.readFully(byte[], int, int)\" because \"this.serverIn\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        try {
            rawChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(int)\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetHeader() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        rawChannel.setHeader("testRawChannelHeader");
        assertEquals(10, rawChannel.getHeader().length, "rawChannel.getHeader().length");
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        byte[] header = new byte[0];
        RawChannel rawChannel = new RawChannel(mock(VISA1Packager.class), header);
        try {
            rawChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertSame(header, rawChannel.getHeader(), "rawChannel.getHeader()");
        }
    }
}
