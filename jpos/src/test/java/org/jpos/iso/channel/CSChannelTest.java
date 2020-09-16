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

import java.net.ServerSocket;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.GenericValidatingPackager;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.iso.packager.PostPackager;
import org.junit.jupiter.api.Test;

public class CSChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new Base1Packager();
        ServerSocket serverSocket = new ServerSocket();
        CSChannel cSChannel = new CSChannel(p, serverSocket);
        assertEquals(0, cSChannel.getIncomingFilters().size(), "cSChannel.getIncomingFilters().size()");
        assertEquals(100000, cSChannel.getMaxPacketLength(), "cSChannel.getMaxPacketLength()");
        assertSame(p, cSChannel.getPackager(), "cSChannel.getPackager()");
        assertEquals(0, cSChannel.getPort(), "cSChannel.getPort()");
        assertEquals("", cSChannel.getName(), "cSChannel.getName()");
        assertEquals(3, cSChannel.getCounters().length, "cSChannel.getCounters().length");
        assertNull(cSChannel.getLogger(), "cSChannel.getLogger()");
        assertNull(cSChannel.getSocketFactory(), "cSChannel.getSocketFactory()");
        assertNull(cSChannel.getHeader(), "cSChannel.getHeader()");
        assertEquals(0, cSChannel.getOutgoingFilters().size(), "cSChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, cSChannel.getServerSocket(), "cSChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.CSChannel", cSChannel.getOriginalRealm(), "cSChannel.getOriginalRealm()");
        assertNull(cSChannel.getRealm(), "cSChannel.getRealm()");
        assertNull(cSChannel.getHost(), "cSChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new GenericValidatingPackager();
        CSChannel cSChannel = new CSChannel("testCSChannelHost", 100, p);
        assertEquals(0, cSChannel.getIncomingFilters().size(), "cSChannel.getIncomingFilters().size()");
        assertEquals(100000, cSChannel.getMaxPacketLength(), "cSChannel.getMaxPacketLength()");
        assertSame(p, cSChannel.getPackager(), "cSChannel.getPackager()");
        assertEquals(100, cSChannel.getPort(), "cSChannel.getPort()");
        assertEquals("", cSChannel.getName(), "cSChannel.getName()");
        assertEquals(3, cSChannel.getCounters().length, "cSChannel.getCounters().length");
        assertNull(cSChannel.getLogger(), "cSChannel.getLogger()");
        assertNull(cSChannel.getSocketFactory(), "cSChannel.getSocketFactory()");
        assertNull(cSChannel.getHeader(), "cSChannel.getHeader()");
        assertEquals(0, cSChannel.getOutgoingFilters().size(), "cSChannel.getOutgoingFilters().size()");
        assertNull(cSChannel.getServerSocket(), "cSChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.CSChannel", cSChannel.getOriginalRealm(), "cSChannel.getOriginalRealm()");
        assertNull(cSChannel.getRealm(), "cSChannel.getRealm()");
        assertEquals("testCSChannelHost", cSChannel.getHost(), "cSChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        CSChannel cSChannel = new CSChannel();
        assertEquals(0, cSChannel.getIncomingFilters().size(), "cSChannel.getIncomingFilters().size()");
        assertEquals(100000, cSChannel.getMaxPacketLength(), "cSChannel.getMaxPacketLength()");
        assertEquals(0, cSChannel.getPort(), "cSChannel.getPort()");
        assertEquals("", cSChannel.getName(), "cSChannel.getName()");
        assertEquals(3, cSChannel.getCounters().length, "cSChannel.getCounters().length");
        assertNull(cSChannel.getLogger(), "cSChannel.getLogger()");
        assertNull(cSChannel.getSocketFactory(), "cSChannel.getSocketFactory()");
        assertNull(cSChannel.getHeader(), "cSChannel.getHeader()");
        assertEquals(0, cSChannel.getOutgoingFilters().size(), "cSChannel.getOutgoingFilters().size()");
        assertNull(cSChannel.getServerSocket(), "cSChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.CSChannel", cSChannel.getOriginalRealm(), "cSChannel.getOriginalRealm()");
        assertNull(cSChannel.getRealm(), "cSChannel.getRealm()");
        assertNull(cSChannel.getHost(), "cSChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new ISO87BPackager();
        CSChannel cSChannel = new CSChannel(p);
        assertEquals(0, cSChannel.getIncomingFilters().size(), "cSChannel.getIncomingFilters().size()");
        assertEquals(100000, cSChannel.getMaxPacketLength(), "cSChannel.getMaxPacketLength()");
        assertSame(p, cSChannel.getPackager(), "cSChannel.getPackager()");
        assertEquals(0, cSChannel.getPort(), "cSChannel.getPort()");
        assertEquals("", cSChannel.getName(), "cSChannel.getName()");
        assertEquals(3, cSChannel.getCounters().length, "cSChannel.getCounters().length");
        assertNull(cSChannel.getLogger(), "cSChannel.getLogger()");
        assertNull(cSChannel.getSocketFactory(), "cSChannel.getSocketFactory()");
        assertNull(cSChannel.getHeader(), "cSChannel.getHeader()");
        assertEquals(0, cSChannel.getOutgoingFilters().size(), "cSChannel.getOutgoingFilters().size()");
        assertNull(cSChannel.getServerSocket(), "cSChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.CSChannel", cSChannel.getOriginalRealm(), "cSChannel.getOriginalRealm()");
        assertNull(cSChannel.getRealm(), "cSChannel.getRealm()");
        assertNull(cSChannel.getHost(), "cSChannel.getHost()");
    }

    @Test
    public void testGetHeaderLength() throws Throwable {
        CSChannel cSChannel = new CSChannel("testCSChannelHost", 100, new PostPackager());
        int result = cSChannel.getHeaderLength();
        assertEquals(0, result, "result");
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        CSChannel cSChannel = new CSChannel();
        try {
            cSChannel.getMessageLength();
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
    public void testSendMessageHeader() throws Throwable {
        CSChannel cSChannel = new CSChannel();
        cSChannel.sendMessageHeader(new ISOMsg(), 100);
        assertEquals(0, cSChannel.getHeaderLength(), "cSChannel.getHeaderLength()");
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        CSChannel cSChannel = new CSChannel();
        try {
            cSChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(int)\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
