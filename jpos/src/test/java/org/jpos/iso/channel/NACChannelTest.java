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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.ServerSocket;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.filter.DelayFilter;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISO93BPackager;
import org.jpos.iso.packager.PostPackager;
import org.junit.jupiter.api.Test;

public class NACChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        assertEquals(0, nACChannel.getIncomingFilters().size(), "nACChannel.getIncomingFilters().size()");
        assertEquals(100000, nACChannel.getMaxPacketLength(), "nACChannel.getMaxPacketLength()");
        assertEquals(0, nACChannel.getPort(), "nACChannel.getPort()");
        assertEquals("", nACChannel.getName(), "nACChannel.getName()");
        assertEquals(3, nACChannel.getCounters().length, "nACChannel.getCounters().length");
        assertNull(nACChannel.getLogger(), "nACChannel.getLogger()");
        assertNull(nACChannel.getSocketFactory(), "nACChannel.getSocketFactory()");
        assertNull(nACChannel.getHeader(), "nACChannel.getHeader()");
        assertEquals(0, nACChannel.getOutgoingFilters().size(), "nACChannel.getOutgoingFilters().size()");
        assertNull(nACChannel.getServerSocket(), "nACChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm(), "nACChannel.getOriginalRealm()");
        assertNull(nACChannel.getRealm(), "nACChannel.getRealm()");
        assertNull(nACChannel.getHost(), "nACChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] TPDU = new byte[3];
        ISOPackager p = new ISO93APackager();
        NACChannel nACChannel = new NACChannel("testNACChannelHost", 100, p, TPDU);
        assertEquals(0, nACChannel.getIncomingFilters().size(), "nACChannel.getIncomingFilters().size()");
        assertEquals(100000, nACChannel.getMaxPacketLength(), "nACChannel.getMaxPacketLength()");
        assertSame(p, nACChannel.getPackager(), "nACChannel.getPackager()");
        assertEquals(100, nACChannel.getPort(), "nACChannel.getPort()");
        assertEquals("", nACChannel.getName(), "nACChannel.getName()");
        assertEquals(3, nACChannel.getCounters().length, "nACChannel.getCounters().length");
        assertNull(nACChannel.getLogger(), "nACChannel.getLogger()");
        assertNull(nACChannel.getSocketFactory(), "nACChannel.getSocketFactory()");
        assertSame(TPDU, nACChannel.getHeader(), "nACChannel.getHeader()");
        assertEquals(0, nACChannel.getOutgoingFilters().size(), "nACChannel.getOutgoingFilters().size()");
        assertNull(nACChannel.getServerSocket(), "nACChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm(), "nACChannel.getOriginalRealm()");
        assertNull(nACChannel.getRealm(), "nACChannel.getRealm()");
        assertEquals("testNACChannelHost", nACChannel.getHost(), "nACChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new CTCSubFieldPackager();
        byte[] TPDU = new byte[0];
        ServerSocket serverSocket = new ServerSocket();
        NACChannel nACChannel = new NACChannel(p, TPDU, serverSocket);
        assertEquals(0, nACChannel.getIncomingFilters().size(), "nACChannel.getIncomingFilters().size()");
        assertEquals(100000, nACChannel.getMaxPacketLength(), "nACChannel.getMaxPacketLength()");
        assertSame(p, nACChannel.getPackager(), "nACChannel.getPackager()");
        assertEquals(0, nACChannel.getPort(), "nACChannel.getPort()");
        assertEquals("", nACChannel.getName(), "nACChannel.getName()");
        assertEquals(3, nACChannel.getCounters().length, "nACChannel.getCounters().length");
        assertNull(nACChannel.getLogger(), "nACChannel.getLogger()");
        assertNull(nACChannel.getSocketFactory(), "nACChannel.getSocketFactory()");
        assertSame(TPDU, nACChannel.getHeader(), "nACChannel.getHeader()");
        assertEquals(0, nACChannel.getOutgoingFilters().size(), "nACChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, nACChannel.getServerSocket(), "nACChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm(), "nACChannel.getOriginalRealm()");
        assertNull(nACChannel.getRealm(), "nACChannel.getRealm()");
        assertNull(nACChannel.getHost(), "nACChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        byte[] TPDU = new byte[0];
        ISOPackager p = new PostPackager();
        NACChannel nACChannel = new NACChannel(p, TPDU);
        assertEquals(0, nACChannel.getIncomingFilters().size(), "nACChannel.getIncomingFilters().size()");
        assertEquals(100000, nACChannel.getMaxPacketLength(), "nACChannel.getMaxPacketLength()");
        assertSame(p, nACChannel.getPackager(), "nACChannel.getPackager()");
        assertEquals(0, nACChannel.getPort(), "nACChannel.getPort()");
        assertEquals("", nACChannel.getName(), "nACChannel.getName()");
        assertEquals(3, nACChannel.getCounters().length, "nACChannel.getCounters().length");
        assertNull(nACChannel.getLogger(), "nACChannel.getLogger()");
        assertNull(nACChannel.getSocketFactory(), "nACChannel.getSocketFactory()");
        assertSame(TPDU, nACChannel.getHeader(), "nACChannel.getHeader()");
        assertEquals(0, nACChannel.getOutgoingFilters().size(), "nACChannel.getOutgoingFilters().size()");
        assertNull(nACChannel.getServerSocket(), "nACChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm(), "nACChannel.getOriginalRealm()");
        assertNull(nACChannel.getRealm(), "nACChannel.getRealm()");
        assertNull(nACChannel.getHost(), "nACChannel.getHost()");
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        try {
            nACChannel.getMessageLength();
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
        NACChannel nACChannel = new NACChannel();
        ISOMsg m = new ISOMsg();
        nACChannel.sendMessageHeader(m, 100);
        assertTrue(true, "Execute without Exception");
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        try {
            nACChannel.sendMessageHeader(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOMsg.getHeader()\" because \"m\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException1() throws Throwable {
        byte[] TPDU = new byte[0];
        NACChannel nACChannel = new NACChannel(new PostPackager(), TPDU);
        ISOMsg m = new ISOMsg();
        try {
            nACChannel.sendMessageHeader(m, 100);
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
    public void testSendMessageHeaderThrowsNullPointerException2() throws Throwable {
        NACChannel nACChannel = new NACChannel(new ISO93APackager(), "testString".getBytes());
        ISOMsg m = new ISOMsg();
        byte[] b = new byte[0];
        m.setHeader(b);
        try {
            nACChannel.sendMessageHeader(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(byte[])\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, m.getDirection(), "m.getDirection()");
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[0];
        NACChannel nACChannel = new NACChannel("testNACChannelHost", 100, new ISO93APackager(), TPDU);
        try {
            nACChannel.sendMessageLength(100);
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
        NACChannel nACChannel = new NACChannel();
        nACChannel.setHeader("testNACChannelHeader");
        assertEquals(10, nACChannel.getHeader().length, "nACChannel.getHeader().length");
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[0];
        byte[] TPDU2 = new byte[1];
        NACChannel nACChannel = new NACChannel(new ISO93APackager(), TPDU2, new ServerSocket());
        new NACChannel(new ISO93BPackager(), TPDU).addFilter(new DelayFilter(), 100);
        try {
            nACChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertSame(TPDU2, nACChannel.getHeader(), "nACChannel.getHeader()");
        }
    }
}
