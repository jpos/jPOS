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
import org.jpos.iso.filter.DelayFilter;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISO93BPackager;
import org.jpos.iso.packager.PostPackager;
import org.junit.Test;

public class NACChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        assertEquals("nACChannel.getIncomingFilters().size()", 0, nACChannel.getIncomingFilters().size());
        assertEquals("nACChannel.getMaxPacketLength()", 100000, nACChannel.getMaxPacketLength());
        assertEquals("nACChannel.getPort()", 0, nACChannel.getPort());
        assertEquals("nACChannel.getName()", "", nACChannel.getName());
        assertEquals("nACChannel.getCounters().length", 3, nACChannel.getCounters().length);
        assertNull("nACChannel.getLogger()", nACChannel.getLogger());
        assertNull("nACChannel.getSocketFactory()", nACChannel.getSocketFactory());
        assertNull("nACChannel.getHeader()", nACChannel.getHeader());
        assertEquals("nACChannel.getOutgoingFilters().size()", 0, nACChannel.getOutgoingFilters().size());
        assertNull("nACChannel.getServerSocket()", nACChannel.getServerSocket());
        assertEquals("nACChannel.getOriginalRealm()", "org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm());
        assertNull("nACChannel.getRealm()", nACChannel.getRealm());
        assertNull("nACChannel.getHost()", nACChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] TPDU = new byte[3];
        ISOPackager p = new ISO93APackager();
        NACChannel nACChannel = new NACChannel("testNACChannelHost", 100, p, TPDU);
        assertEquals("nACChannel.getIncomingFilters().size()", 0, nACChannel.getIncomingFilters().size());
        assertEquals("nACChannel.getMaxPacketLength()", 100000, nACChannel.getMaxPacketLength());
        assertSame("nACChannel.getPackager()", p, nACChannel.getPackager());
        assertEquals("nACChannel.getPort()", 100, nACChannel.getPort());
        assertEquals("nACChannel.getName()", "", nACChannel.getName());
        assertEquals("nACChannel.getCounters().length", 3, nACChannel.getCounters().length);
        assertNull("nACChannel.getLogger()", nACChannel.getLogger());
        assertNull("nACChannel.getSocketFactory()", nACChannel.getSocketFactory());
        assertSame("nACChannel.getHeader()", TPDU, nACChannel.getHeader());
        assertEquals("nACChannel.getOutgoingFilters().size()", 0, nACChannel.getOutgoingFilters().size());
        assertNull("nACChannel.getServerSocket()", nACChannel.getServerSocket());
        assertEquals("nACChannel.getOriginalRealm()", "org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm());
        assertNull("nACChannel.getRealm()", nACChannel.getRealm());
        assertEquals("nACChannel.getHost()", "testNACChannelHost", nACChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new CTCSubFieldPackager();
        byte[] TPDU = new byte[0];
        ServerSocket serverSocket = new ServerSocket();
        NACChannel nACChannel = new NACChannel(p, TPDU, serverSocket);
        assertEquals("nACChannel.getIncomingFilters().size()", 0, nACChannel.getIncomingFilters().size());
        assertEquals("nACChannel.getMaxPacketLength()", 100000, nACChannel.getMaxPacketLength());
        assertSame("nACChannel.getPackager()", p, nACChannel.getPackager());
        assertEquals("nACChannel.getPort()", 0, nACChannel.getPort());
        assertEquals("nACChannel.getName()", "", nACChannel.getName());
        assertEquals("nACChannel.getCounters().length", 3, nACChannel.getCounters().length);
        assertNull("nACChannel.getLogger()", nACChannel.getLogger());
        assertNull("nACChannel.getSocketFactory()", nACChannel.getSocketFactory());
        assertSame("nACChannel.getHeader()", TPDU, nACChannel.getHeader());
        assertEquals("nACChannel.getOutgoingFilters().size()", 0, nACChannel.getOutgoingFilters().size());
        assertSame("nACChannel.getServerSocket()", serverSocket, nACChannel.getServerSocket());
        assertEquals("nACChannel.getOriginalRealm()", "org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm());
        assertNull("nACChannel.getRealm()", nACChannel.getRealm());
        assertNull("nACChannel.getHost()", nACChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        byte[] TPDU = new byte[0];
        ISOPackager p = new PostPackager();
        NACChannel nACChannel = new NACChannel(p, TPDU);
        assertEquals("nACChannel.getIncomingFilters().size()", 0, nACChannel.getIncomingFilters().size());
        assertEquals("nACChannel.getMaxPacketLength()", 100000, nACChannel.getMaxPacketLength());
        assertSame("nACChannel.getPackager()", p, nACChannel.getPackager());
        assertEquals("nACChannel.getPort()", 0, nACChannel.getPort());
        assertEquals("nACChannel.getName()", "", nACChannel.getName());
        assertEquals("nACChannel.getCounters().length", 3, nACChannel.getCounters().length);
        assertNull("nACChannel.getLogger()", nACChannel.getLogger());
        assertNull("nACChannel.getSocketFactory()", nACChannel.getSocketFactory());
        assertSame("nACChannel.getHeader()", TPDU, nACChannel.getHeader());
        assertEquals("nACChannel.getOutgoingFilters().size()", 0, nACChannel.getOutgoingFilters().size());
        assertNull("nACChannel.getServerSocket()", nACChannel.getServerSocket());
        assertEquals("nACChannel.getOriginalRealm()", "org.jpos.iso.channel.NACChannel", nACChannel.getOriginalRealm());
        assertNull("nACChannel.getRealm()", nACChannel.getRealm());
        assertNull("nACChannel.getHost()", nACChannel.getHost());
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        try {
            nACChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageHeader() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        ISOMsg m = new ISOMsg();
        nACChannel.sendMessageHeader(m, 100);
        assertTrue("Execute without Exception", true);
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        try {
            nACChannel.sendMessageHeader(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("m.getDirection()", 0, m.getDirection());
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
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetHeader() throws Throwable {
        NACChannel nACChannel = new NACChannel();
        nACChannel.setHeader("testNACChannelHeader");
        assertEquals("nACChannel.getHeader().length", 10, nACChannel.getHeader().length);
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("nACChannel.getHeader()", TPDU2, nACChannel.getHeader());
        }
    }
}
