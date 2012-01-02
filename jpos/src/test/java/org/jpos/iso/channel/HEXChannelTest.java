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
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.junit.Test;

public class HEXChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new GenericPackager();
        byte[] TPDU = new byte[3];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, p, TPDU);
        assertEquals("hEXChannel.getIncomingFilters().size()", 0, hEXChannel.getIncomingFilters().size());
        assertEquals("hEXChannel.getMaxPacketLength()", 100000, hEXChannel.getMaxPacketLength());
        assertSame("hEXChannel.getPackager()", p, hEXChannel.getPackager());
        assertEquals("hEXChannel.getPort()", 100, hEXChannel.getPort());
        assertEquals("hEXChannel.getName()", "", hEXChannel.getName());
        assertEquals("hEXChannel.getCounters().length", 3, hEXChannel.getCounters().length);
        assertNull("hEXChannel.getLogger()", hEXChannel.getLogger());
        assertNull("hEXChannel.getSocketFactory()", hEXChannel.getSocketFactory());
        assertSame("hEXChannel.getHeader()", TPDU, hEXChannel.getHeader());
        assertEquals("hEXChannel.getOutgoingFilters().size()", 0, hEXChannel.getOutgoingFilters().size());
        assertNull("hEXChannel.getServerSocket()", hEXChannel.getServerSocket());
        assertEquals("hEXChannel.getOriginalRealm()", "org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm());
        assertNull("hEXChannel.getRealm()", hEXChannel.getRealm());
        assertEquals("hEXChannel.getHost()", "testHEXChannelHost", hEXChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] TPDU = new byte[1];
        ISOPackager p = new ISO93APackager();
        HEXChannel hEXChannel = new HEXChannel(p, TPDU);
        assertEquals("hEXChannel.getIncomingFilters().size()", 0, hEXChannel.getIncomingFilters().size());
        assertEquals("hEXChannel.getMaxPacketLength()", 100000, hEXChannel.getMaxPacketLength());
        assertSame("hEXChannel.getPackager()", p, hEXChannel.getPackager());
        assertEquals("hEXChannel.getPort()", 0, hEXChannel.getPort());
        assertEquals("hEXChannel.getName()", "", hEXChannel.getName());
        assertEquals("hEXChannel.getCounters().length", 3, hEXChannel.getCounters().length);
        assertNull("hEXChannel.getLogger()", hEXChannel.getLogger());
        assertNull("hEXChannel.getSocketFactory()", hEXChannel.getSocketFactory());
        assertSame("hEXChannel.getHeader()", TPDU, hEXChannel.getHeader());
        assertEquals("hEXChannel.getOutgoingFilters().size()", 0, hEXChannel.getOutgoingFilters().size());
        assertNull("hEXChannel.getServerSocket()", hEXChannel.getServerSocket());
        assertEquals("hEXChannel.getOriginalRealm()", "org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm());
        assertNull("hEXChannel.getRealm()", hEXChannel.getRealm());
        assertNull("hEXChannel.getHost()", hEXChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        HEXChannel hEXChannel = new HEXChannel();
        assertEquals("hEXChannel.getIncomingFilters().size()", 0, hEXChannel.getIncomingFilters().size());
        assertEquals("hEXChannel.getMaxPacketLength()", 100000, hEXChannel.getMaxPacketLength());
        assertEquals("hEXChannel.getPort()", 0, hEXChannel.getPort());
        assertEquals("hEXChannel.getName()", "", hEXChannel.getName());
        assertEquals("hEXChannel.getCounters().length", 3, hEXChannel.getCounters().length);
        assertNull("hEXChannel.getLogger()", hEXChannel.getLogger());
        assertNull("hEXChannel.getSocketFactory()", hEXChannel.getSocketFactory());
        assertNull("hEXChannel.getHeader()", hEXChannel.getHeader());
        assertEquals("hEXChannel.getOutgoingFilters().size()", 0, hEXChannel.getOutgoingFilters().size());
        assertNull("hEXChannel.getServerSocket()", hEXChannel.getServerSocket());
        assertEquals("hEXChannel.getOriginalRealm()", "org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm());
        assertNull("hEXChannel.getRealm()", hEXChannel.getRealm());
        assertNull("hEXChannel.getHost()", hEXChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new ISOBaseValidatingPackager();
        byte[] TPDU = new byte[2];
        ServerSocket serverSocket = new ServerSocket();
        HEXChannel hEXChannel = new HEXChannel(p, TPDU, serverSocket);
        assertEquals("hEXChannel.getIncomingFilters().size()", 0, hEXChannel.getIncomingFilters().size());
        assertEquals("hEXChannel.getMaxPacketLength()", 100000, hEXChannel.getMaxPacketLength());
        assertSame("hEXChannel.getPackager()", p, hEXChannel.getPackager());
        assertEquals("hEXChannel.getPort()", 0, hEXChannel.getPort());
        assertEquals("hEXChannel.getName()", "", hEXChannel.getName());
        assertEquals("hEXChannel.getCounters().length", 3, hEXChannel.getCounters().length);
        assertNull("hEXChannel.getLogger()", hEXChannel.getLogger());
        assertNull("hEXChannel.getSocketFactory()", hEXChannel.getSocketFactory());
        assertSame("hEXChannel.getHeader()", TPDU, hEXChannel.getHeader());
        assertEquals("hEXChannel.getOutgoingFilters().size()", 0, hEXChannel.getOutgoingFilters().size());
        assertSame("hEXChannel.getServerSocket()", serverSocket, hEXChannel.getServerSocket());
        assertEquals("hEXChannel.getOriginalRealm()", "org.jpos.iso.channel.HEXChannel", hEXChannel.getOriginalRealm());
        assertNull("hEXChannel.getRealm()", hEXChannel.getRealm());
        assertNull("hEXChannel.getHost()", hEXChannel.getHost());
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[1];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, null, TPDU);
        try {
            hEXChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        HEXChannel hEXChannel = new HEXChannel(new ISOBaseValidatingPackager(), null, new ServerSocket());
        try {
            hEXChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetHeader() throws Throwable {
        byte[] TPDU = new byte[1];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, null, TPDU);
        hEXChannel.setHeader("testHEXChannelHeader");
        assertEquals("hEXChannel.getHeader().length", 20, hEXChannel.getHeader().length);
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[1];
        HEXChannel hEXChannel = new HEXChannel("testHEXChannelHost", 100, null, TPDU);
        try {
            hEXChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("hEXChannel.getHeader()", TPDU, hEXChannel.getHeader());
        }
    }
}
