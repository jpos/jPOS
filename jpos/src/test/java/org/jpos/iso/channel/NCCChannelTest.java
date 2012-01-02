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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.header.BASE1Header;
import org.jpos.iso.packager.BASE24Packager;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.iso.packager.ISO93BPackager;
import org.junit.Test;

public class NCCChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new ISO87BPackager();
        byte[] TPDU = new byte[1];
        ServerSocket serverSocket = new ServerSocket();
        NCCChannel nCCChannel = new NCCChannel(p, TPDU, serverSocket);
        assertEquals("nCCChannel.getIncomingFilters().size()", 0, nCCChannel.getIncomingFilters().size());
        assertEquals("nCCChannel.getMaxPacketLength()", 100000, nCCChannel.getMaxPacketLength());
        assertSame("nCCChannel.getPackager()", p, nCCChannel.getPackager());
        assertEquals("nCCChannel.getPort()", 0, nCCChannel.getPort());
        assertEquals("nCCChannel.getName()", "", nCCChannel.getName());
        assertEquals("nCCChannel.getCounters().length", 3, nCCChannel.getCounters().length);
        assertNull("nCCChannel.getLogger()", nCCChannel.getLogger());
        assertNull("nCCChannel.getSocketFactory()", nCCChannel.getSocketFactory());
        assertSame("nCCChannel.getHeader()", TPDU, nCCChannel.getHeader());
        assertEquals("nCCChannel.getOutgoingFilters().size()", 0, nCCChannel.getOutgoingFilters().size());
        assertSame("nCCChannel.getServerSocket()", serverSocket, nCCChannel.getServerSocket());
        assertEquals("nCCChannel.getOriginalRealm()", "org.jpos.iso.channel.NCCChannel", nCCChannel.getOriginalRealm());
        assertNull("nCCChannel.getRealm()", nCCChannel.getRealm());
        assertNull("nCCChannel.getHost()", nCCChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] TPDU = new byte[2];
        ISOPackager p = new ISO87BPackager();
        NCCChannel nCCChannel = new NCCChannel(p, TPDU);
        assertEquals("nCCChannel.getIncomingFilters().size()", 0, nCCChannel.getIncomingFilters().size());
        assertEquals("nCCChannel.getMaxPacketLength()", 100000, nCCChannel.getMaxPacketLength());
        assertSame("nCCChannel.getPackager()", p, nCCChannel.getPackager());
        assertEquals("nCCChannel.getPort()", 0, nCCChannel.getPort());
        assertEquals("nCCChannel.getName()", "", nCCChannel.getName());
        assertEquals("nCCChannel.getCounters().length", 3, nCCChannel.getCounters().length);
        assertNull("nCCChannel.getLogger()", nCCChannel.getLogger());
        assertNull("nCCChannel.getSocketFactory()", nCCChannel.getSocketFactory());
        assertSame("nCCChannel.getHeader()", TPDU, nCCChannel.getHeader());
        assertEquals("nCCChannel.getOutgoingFilters().size()", 0, nCCChannel.getOutgoingFilters().size());
        assertNull("nCCChannel.getServerSocket()", nCCChannel.getServerSocket());
        assertEquals("nCCChannel.getOriginalRealm()", "org.jpos.iso.channel.NCCChannel", nCCChannel.getOriginalRealm());
        assertNull("nCCChannel.getRealm()", nCCChannel.getRealm());
        assertNull("nCCChannel.getHost()", nCCChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        NCCChannel nCCChannel = new NCCChannel();
        assertEquals("nCCChannel.getIncomingFilters().size()", 0, nCCChannel.getIncomingFilters().size());
        assertEquals("nCCChannel.getMaxPacketLength()", 100000, nCCChannel.getMaxPacketLength());
        assertEquals("nCCChannel.getPort()", 0, nCCChannel.getPort());
        assertEquals("nCCChannel.getName()", "", nCCChannel.getName());
        assertEquals("nCCChannel.getCounters().length", 3, nCCChannel.getCounters().length);
        assertNull("nCCChannel.getLogger()", nCCChannel.getLogger());
        assertNull("nCCChannel.getSocketFactory()", nCCChannel.getSocketFactory());
        assertNull("nCCChannel.getHeader()", nCCChannel.getHeader());
        assertEquals("nCCChannel.getOutgoingFilters().size()", 0, nCCChannel.getOutgoingFilters().size());
        assertNull("nCCChannel.getServerSocket()", nCCChannel.getServerSocket());
        assertEquals("nCCChannel.getOriginalRealm()", "org.jpos.iso.channel.NCCChannel", nCCChannel.getOriginalRealm());
        assertNull("nCCChannel.getRealm()", nCCChannel.getRealm());
        assertNull("nCCChannel.getHost()", nCCChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        byte[] TPDU = new byte[1];
        ISOPackager p = new CTCSubFieldPackager();
        NCCChannel nCCChannel = new NCCChannel("testNCCChannelHost", 100, p, TPDU);
        assertEquals("nCCChannel.getIncomingFilters().size()", 0, nCCChannel.getIncomingFilters().size());
        assertEquals("nCCChannel.getMaxPacketLength()", 100000, nCCChannel.getMaxPacketLength());
        assertSame("nCCChannel.getPackager()", p, nCCChannel.getPackager());
        assertEquals("nCCChannel.getPort()", 100, nCCChannel.getPort());
        assertEquals("nCCChannel.getName()", "", nCCChannel.getName());
        assertEquals("nCCChannel.getCounters().length", 3, nCCChannel.getCounters().length);
        assertNull("nCCChannel.getLogger()", nCCChannel.getLogger());
        assertNull("nCCChannel.getSocketFactory()", nCCChannel.getSocketFactory());
        assertSame("nCCChannel.getHeader()", TPDU, nCCChannel.getHeader());
        assertEquals("nCCChannel.getOutgoingFilters().size()", 0, nCCChannel.getOutgoingFilters().size());
        assertNull("nCCChannel.getServerSocket()", nCCChannel.getServerSocket());
        assertEquals("nCCChannel.getOriginalRealm()", "org.jpos.iso.channel.NCCChannel", nCCChannel.getOriginalRealm());
        assertNull("nCCChannel.getRealm()", nCCChannel.getRealm());
        assertEquals("nCCChannel.getHost()", "testNCCChannelHost", nCCChannel.getHost());
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        NCCChannel nCCChannel = new NCCChannel();
        try {
            nCCChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageHeader() throws Throwable {
        NCCChannel nCCChannel = new NCCChannel();
        ISOMsg m = new ISOMsg();
        nCCChannel.sendMessageHeader(m, 100);
        assertEquals("m.getDirection()", 0, m.getDirection());
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[1];
        NCCChannel nCCChannel = new NCCChannel(new ISO87BPackager(), TPDU, new ServerSocket());
        ISOMsg m = new ISOMsg();
        try {
            nCCChannel.sendMessageHeader(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException1() throws Throwable {
        byte[] TPDU = new byte[1];
        NCCChannel nCCChannel = new NCCChannel(new ISO87BPackager(), TPDU, new ServerSocket());
        ISOMsg m = new ISOMsg(100);
        m.setHeader(new BASE1Header("testNCCChannelSource", "testNCCChannelDestination"));
        try {
            nCCChannel.sendMessageHeader(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException2() throws Throwable {
        byte[] TPDU = new byte[16];
        NCCChannel nCCChannel = new NCCChannel(new ISO87APackager(), TPDU);
        try {
            nCCChannel.sendMessageHeader(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageLength() throws Throwable {
        NCCChannel nCCChannel = new NCCChannel(new BASE24Packager(), "testString".getBytes());
        nCCChannel.sendMessageLength(-2147483646);
        assertTrue("should execute without exception", true);
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[0];
        NCCChannel nCCChannel = new NCCChannel(new ISO87APackager(), TPDU);
        try {
            nCCChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetHeader() throws Throwable {
        byte[] TPDU = new byte[1];
        NCCChannel nCCChannel = new NCCChannel("testNCCChannelHost", 100, new ISO93BPackager(), TPDU);
        nCCChannel.setHeader("testNCCChannelHeader");
        assertEquals("nCCChannel.getHeader().length", 10, nCCChannel.getHeader().length);
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[1];
        NCCChannel nCCChannel = new NCCChannel(new ISO87BPackager(), TPDU, new ServerSocket());
        try {
            nCCChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("nCCChannel.getHeader()", TPDU, nCCChannel.getHeader());
        }
    }
}
