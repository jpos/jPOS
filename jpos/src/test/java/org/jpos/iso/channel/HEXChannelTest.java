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

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.junit.jupiter.api.Test;

public class HEXChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new GenericPackager();
        byte[] TPDU = new byte[3];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, p, TPDU);
        assertEquals(0, hEXChannel.getIncomingFilters().size(), "hEXChannel.getIncomingFilters().size()");
        assertEquals(100000, hEXChannel.getMaxPacketLength(), "hEXChannel.getMaxPacketLength()");
        assertSame(p, hEXChannel.getPackager(), "hEXChannel.getPackager()");
        assertEquals(100, hEXChannel.getPort(), "hEXChannel.getPort()");
        assertEquals("", hEXChannel.getName(), "hEXChannel.getName()");
        assertEquals(3, hEXChannel.getCounters().length, "hEXChannel.getCounters().length");
        assertNull(hEXChannel.getLogger(), "hEXChannel.getLogger()");
        assertNull(hEXChannel.getSocketFactory(), "hEXChannel.getSocketFactory()");
        assertSame(TPDU, hEXChannel.getHeader(), "hEXChannel.getHeader()");
        assertEquals(0, hEXChannel.getOutgoingFilters().size(), "hEXChannel.getOutgoingFilters().size()");
        assertNull(hEXChannel.getServerSocket(), "hEXChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm(), "hEXChannel.getOriginalRealm()");
        assertNull(hEXChannel.getRealm(), "hEXChannel.getRealm()");
        assertEquals("testHEXChannelHost", hEXChannel.getHost(), "hEXChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] TPDU = new byte[1];
        ISOPackager p = new ISO93APackager();
        HEXChannel hEXChannel = new HEXChannel(p, TPDU);
        assertEquals(0, hEXChannel.getIncomingFilters().size(), "hEXChannel.getIncomingFilters().size()");
        assertEquals(100000, hEXChannel.getMaxPacketLength(), "hEXChannel.getMaxPacketLength()");
        assertSame(p, hEXChannel.getPackager(), "hEXChannel.getPackager()");
        assertEquals(0, hEXChannel.getPort(), "hEXChannel.getPort()");
        assertEquals("", hEXChannel.getName(), "hEXChannel.getName()");
        assertEquals(3, hEXChannel.getCounters().length, "hEXChannel.getCounters().length");
        assertNull(hEXChannel.getLogger(), "hEXChannel.getLogger()");
        assertNull(hEXChannel.getSocketFactory(), "hEXChannel.getSocketFactory()");
        assertSame(TPDU, hEXChannel.getHeader(), "hEXChannel.getHeader()");
        assertEquals(0, hEXChannel.getOutgoingFilters().size(), "hEXChannel.getOutgoingFilters().size()");
        assertNull(hEXChannel.getServerSocket(), "hEXChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm(), "hEXChannel.getOriginalRealm()");
        assertNull(hEXChannel.getRealm(), "hEXChannel.getRealm()");
        assertNull(hEXChannel.getHost(), "hEXChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        HEXChannel hEXChannel = new HEXChannel();
        assertEquals(0, hEXChannel.getIncomingFilters().size(), "hEXChannel.getIncomingFilters().size()");
        assertEquals(100000, hEXChannel.getMaxPacketLength(), "hEXChannel.getMaxPacketLength()");
        assertEquals(0, hEXChannel.getPort(), "hEXChannel.getPort()");
        assertEquals("", hEXChannel.getName(), "hEXChannel.getName()");
        assertEquals(3, hEXChannel.getCounters().length, "hEXChannel.getCounters().length");
        assertNull(hEXChannel.getLogger(), "hEXChannel.getLogger()");
        assertNull(hEXChannel.getSocketFactory(), "hEXChannel.getSocketFactory()");
        assertNull(hEXChannel.getHeader(), "hEXChannel.getHeader()");
        assertEquals(0, hEXChannel.getOutgoingFilters().size(), "hEXChannel.getOutgoingFilters().size()");
        assertNull(hEXChannel.getServerSocket(), "hEXChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm(), "hEXChannel.getOriginalRealm()");
        assertNull(hEXChannel.getRealm(), "hEXChannel.getRealm()");
        assertNull(hEXChannel.getHost(), "hEXChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new ISOBaseValidatingPackager();
        byte[] TPDU = new byte[2];
        ServerSocket serverSocket = new ServerSocket();
        HEXChannel hEXChannel = new HEXChannel(p, TPDU, serverSocket);
        assertEquals(0, hEXChannel.getIncomingFilters().size(), "hEXChannel.getIncomingFilters().size()");
        assertEquals(100000, hEXChannel.getMaxPacketLength(), "hEXChannel.getMaxPacketLength()");
        assertSame(p, hEXChannel.getPackager(), "hEXChannel.getPackager()");
        assertEquals(0, hEXChannel.getPort(), "hEXChannel.getPort()");
        assertEquals("", hEXChannel.getName(), "hEXChannel.getName()");
        assertEquals(3, hEXChannel.getCounters().length, "hEXChannel.getCounters().length");
        assertNull(hEXChannel.getLogger(), "hEXChannel.getLogger()");
        assertNull(hEXChannel.getSocketFactory(), "hEXChannel.getSocketFactory()");
        assertSame(TPDU, hEXChannel.getHeader(), "hEXChannel.getHeader()");
        assertEquals(0, hEXChannel.getOutgoingFilters().size(), "hEXChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, hEXChannel.getServerSocket(), "hEXChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm(), "hEXChannel.getOriginalRealm()");
        assertNull(hEXChannel.getRealm(), "hEXChannel.getRealm()");
        assertNull(hEXChannel.getHost(), "hEXChannel.getHost()");
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[1];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, null, TPDU);
        try {
            hEXChannel.getMessageLength();
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
        HEXChannel hEXChannel = new HEXChannel(new ISOBaseValidatingPackager(), null, new ServerSocket());
        try {
            hEXChannel.sendMessageLength(100);
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
    public void testSetHeader() throws Throwable {
        byte[] TPDU = new byte[1];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, null, TPDU);
        hEXChannel.setHeader("testHEXChannelHeader");
        assertEquals(20, hEXChannel.getHeader().length, "hEXChannel.getHeader().length");
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[1];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, null, TPDU);
        try {
            hEXChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.getBytes()\" because \"header\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertSame(TPDU, hEXChannel.getHeader(), "hEXChannel.getHeader()");
        }
    }
}
