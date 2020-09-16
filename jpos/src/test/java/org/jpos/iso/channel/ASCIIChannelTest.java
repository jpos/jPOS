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

import java.io.IOException;
import java.net.ServerSocket;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.junit.jupiter.api.Test;

public class ASCIIChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new ISO87APackagerBBitmap();
        ServerSocket serverSocket = new ServerSocket();
        ASCIIChannel aSCIIChannel = new ASCIIChannel(p, serverSocket);
        assertEquals(0, aSCIIChannel.getIncomingFilters().size(), "aSCIIChannel.getIncomingFilters().size()");
        assertEquals(100000, aSCIIChannel.getMaxPacketLength(), "aSCIIChannel.getMaxPacketLength()");
        assertSame(p, aSCIIChannel.getPackager(), "aSCIIChannel.getPackager()");
        assertEquals(0, aSCIIChannel.getPort(), "aSCIIChannel.getPort()");
        assertEquals("", aSCIIChannel.getName(), "aSCIIChannel.getName()");
        assertEquals(3, aSCIIChannel.getCounters().length, "aSCIIChannel.getCounters().length");
        assertNull(aSCIIChannel.getLogger(), "aSCIIChannel.getLogger()");
        assertNull(aSCIIChannel.getSocketFactory(), "aSCIIChannel.getSocketFactory()");
        assertNull(aSCIIChannel.getHeader(), "aSCIIChannel.getHeader()");
        assertEquals(0, aSCIIChannel.getOutgoingFilters().size(), "aSCIIChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, aSCIIChannel.getServerSocket(), "aSCIIChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm(), "aSCIIChannel.getOriginalRealm()");
        assertNull(aSCIIChannel.getRealm(), "aSCIIChannel.getRealm()");
        assertNull(aSCIIChannel.getHost(), "aSCIIChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO87APackagerBBitmap();
        ASCIIChannel aSCIIChannel = new ASCIIChannel(p);
        assertEquals(0, aSCIIChannel.getIncomingFilters().size(), "aSCIIChannel.getIncomingFilters().size()");
        assertEquals(100000, aSCIIChannel.getMaxPacketLength(), "aSCIIChannel.getMaxPacketLength()");
        assertSame(p, aSCIIChannel.getPackager(), "aSCIIChannel.getPackager()");
        assertEquals(0, aSCIIChannel.getPort(), "aSCIIChannel.getPort()");
        assertEquals("", aSCIIChannel.getName(), "aSCIIChannel.getName()");
        assertEquals(3, aSCIIChannel.getCounters().length, "aSCIIChannel.getCounters().length");
        assertNull(aSCIIChannel.getLogger(), "aSCIIChannel.getLogger()");
        assertNull(aSCIIChannel.getSocketFactory(), "aSCIIChannel.getSocketFactory()");
        assertNull(aSCIIChannel.getHeader(), "aSCIIChannel.getHeader()");
        assertEquals(0, aSCIIChannel.getOutgoingFilters().size(), "aSCIIChannel.getOutgoingFilters().size()");
        assertNull(aSCIIChannel.getServerSocket(), "aSCIIChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm(), "aSCIIChannel.getOriginalRealm()");
        assertNull(aSCIIChannel.getRealm(), "aSCIIChannel.getRealm()");
        assertNull(aSCIIChannel.getHost(), "aSCIIChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel();
        assertEquals(0, aSCIIChannel.getIncomingFilters().size(), "aSCIIChannel.getIncomingFilters().size()");
        assertEquals(100000, aSCIIChannel.getMaxPacketLength(), "aSCIIChannel.getMaxPacketLength()");
        assertEquals(0, aSCIIChannel.getPort(), "aSCIIChannel.getPort()");
        assertEquals("", aSCIIChannel.getName(), "aSCIIChannel.getName()");
        assertEquals(3, aSCIIChannel.getCounters().length, "aSCIIChannel.getCounters().length");
        assertNull(aSCIIChannel.getLogger(), "aSCIIChannel.getLogger()");
        assertNull(aSCIIChannel.getSocketFactory(), "aSCIIChannel.getSocketFactory()");
        assertNull(aSCIIChannel.getHeader(), "aSCIIChannel.getHeader()");
        assertEquals(0, aSCIIChannel.getOutgoingFilters().size(), "aSCIIChannel.getOutgoingFilters().size()");
        assertNull(aSCIIChannel.getServerSocket(), "aSCIIChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm(), "aSCIIChannel.getOriginalRealm()");
        assertNull(aSCIIChannel.getRealm(), "aSCIIChannel.getRealm()");
        assertNull(aSCIIChannel.getHost(), "aSCIIChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new ISO87APackagerBBitmap();
        ASCIIChannel aSCIIChannel = new ASCIIChannel("testASCIIChannelHost", 100, p);
        assertEquals(0, aSCIIChannel.getIncomingFilters().size(), "aSCIIChannel.getIncomingFilters().size()");
        assertEquals(100000, aSCIIChannel.getMaxPacketLength(), "aSCIIChannel.getMaxPacketLength()");
        assertSame(p, aSCIIChannel.getPackager(), "aSCIIChannel.getPackager()");
        assertEquals(100, aSCIIChannel.getPort(), "aSCIIChannel.getPort()");
        assertEquals("", aSCIIChannel.getName(), "aSCIIChannel.getName()");
        assertEquals(3, aSCIIChannel.getCounters().length, "aSCIIChannel.getCounters().length");
        assertNull(aSCIIChannel.getLogger(), "aSCIIChannel.getLogger()");
        assertNull(aSCIIChannel.getSocketFactory(), "aSCIIChannel.getSocketFactory()");
        assertNull(aSCIIChannel.getHeader(), "aSCIIChannel.getHeader()");
        assertEquals(0, aSCIIChannel.getOutgoingFilters().size(), "aSCIIChannel.getOutgoingFilters().size()");
        assertNull(aSCIIChannel.getServerSocket(), "aSCIIChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm(), "aSCIIChannel.getOriginalRealm()");
        assertNull(aSCIIChannel.getRealm(), "aSCIIChannel.getRealm()");
        assertEquals("testASCIIChannelHost", aSCIIChannel.getHost(), "aSCIIChannel.getHost()");
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel(new Base1Packager());
        try {
            aSCIIChannel.getMessageLength();
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
    public void testSendMessageLength() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel("testASCIIChannelHost", 100, new GenericPackager());
        try {
            aSCIIChannel.sendMessageLength(Integer.MIN_VALUE);
            fail("IOException expected");
        } catch (IOException ex) {
            assertEquals("invalid negative length ("+Integer.MIN_VALUE+")", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSendMessageLengthThrowsIOException() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel();
        try {
            aSCIIChannel.sendMessageLength(10000);
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals(IOException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel();
        try {
            aSCIIChannel.sendMessageLength(9999);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(byte[])\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException1() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel(new Base1SubFieldPackager());
        try {
            aSCIIChannel.sendMessageLength(9998);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(byte[])\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
