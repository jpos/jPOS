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
import static org.mockito.Mockito.mock;

import java.net.ServerSocket;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.BASE24Packager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.jpos.iso.packager.VISA1Packager;
import org.jpos.iso.packager.XMLPackager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RawChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        assertEquals("rawChannel.getIncomingFilters().size()", 0, rawChannel.getIncomingFilters().size());
        assertEquals("rawChannel.getMaxPacketLength()", 100000, rawChannel.getMaxPacketLength());
        assertEquals("rawChannel.getPort()", 0, rawChannel.getPort());
        assertEquals("rawChannel.getName()", "", rawChannel.getName());
        assertEquals("rawChannel.getCounters().length", 3, rawChannel.getCounters().length);
        assertNull("rawChannel.getLogger()", rawChannel.getLogger());
        assertNull("rawChannel.getSocketFactory()", rawChannel.getSocketFactory());
        assertNull("rawChannel.getHeader()", rawChannel.getHeader());
        assertEquals("rawChannel.getOutgoingFilters().size()", 0, rawChannel.getOutgoingFilters().size());
        assertNull("rawChannel.getServerSocket()", rawChannel.getServerSocket());
        assertEquals("rawChannel.getOriginalRealm()", "org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm());
        assertNull("rawChannel.getRealm()", rawChannel.getRealm());
        assertNull("rawChannel.getHost()", rawChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        byte[] header = new byte[3];
        ISOPackager p = new ISOBaseValidatingPackager();
        RawChannel rawChannel = new RawChannel("testRawChannelHost", 100, p, header);
        assertEquals("rawChannel.getIncomingFilters().size()", 0, rawChannel.getIncomingFilters().size());
        assertEquals("rawChannel.getMaxPacketLength()", 100000, rawChannel.getMaxPacketLength());
        assertSame("rawChannel.getPackager()", p, rawChannel.getPackager());
        assertEquals("rawChannel.getPort()", 100, rawChannel.getPort());
        assertEquals("rawChannel.getName()", "", rawChannel.getName());
        assertEquals("rawChannel.getCounters().length", 3, rawChannel.getCounters().length);
        assertNull("rawChannel.getLogger()", rawChannel.getLogger());
        assertNull("rawChannel.getSocketFactory()", rawChannel.getSocketFactory());
        assertSame("rawChannel.getHeader()", header, rawChannel.getHeader());
        assertEquals("rawChannel.getOutgoingFilters().size()", 0, rawChannel.getOutgoingFilters().size());
        assertNull("rawChannel.getServerSocket()", rawChannel.getServerSocket());
        assertEquals("rawChannel.getOriginalRealm()", "org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm());
        assertNull("rawChannel.getRealm()", rawChannel.getRealm());
        assertEquals("rawChannel.getHost()", "testRawChannelHost", rawChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        byte[] header = new byte[0];
        ISOPackager p = new BASE24Packager();
        RawChannel rawChannel = new RawChannel(p, header);
        assertEquals("rawChannel.getIncomingFilters().size()", 0, rawChannel.getIncomingFilters().size());
        assertEquals("rawChannel.getMaxPacketLength()", 100000, rawChannel.getMaxPacketLength());
        assertSame("rawChannel.getPackager()", p, rawChannel.getPackager());
        assertEquals("rawChannel.getPort()", 0, rawChannel.getPort());
        assertEquals("rawChannel.getName()", "", rawChannel.getName());
        assertEquals("rawChannel.getCounters().length", 3, rawChannel.getCounters().length);
        assertNull("rawChannel.getLogger()", rawChannel.getLogger());
        assertNull("rawChannel.getSocketFactory()", rawChannel.getSocketFactory());
        assertSame("rawChannel.getHeader()", header, rawChannel.getHeader());
        assertEquals("rawChannel.getOutgoingFilters().size()", 0, rawChannel.getOutgoingFilters().size());
        assertNull("rawChannel.getServerSocket()", rawChannel.getServerSocket());
        assertEquals("rawChannel.getOriginalRealm()", "org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm());
        assertNull("rawChannel.getRealm()", rawChannel.getRealm());
        assertNull("rawChannel.getHost()", rawChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        byte[] header = new byte[0];
        ISOPackager p = new XMLPackager();
        ServerSocket serverSocket = new ServerSocket();
        RawChannel rawChannel = new RawChannel(p, header, serverSocket);
        assertEquals("rawChannel.getIncomingFilters().size()", 0, rawChannel.getIncomingFilters().size());
        assertEquals("rawChannel.getMaxPacketLength()", 100000, rawChannel.getMaxPacketLength());
        assertSame("rawChannel.getPackager()", p, rawChannel.getPackager());
        assertEquals("rawChannel.getPort()", 0, rawChannel.getPort());
        assertEquals("rawChannel.getName()", "", rawChannel.getName());
        assertEquals("rawChannel.getCounters().length", 3, rawChannel.getCounters().length);
        assertNull("rawChannel.getLogger()", rawChannel.getLogger());
        assertNull("rawChannel.getSocketFactory()", rawChannel.getSocketFactory());
        assertSame("rawChannel.getHeader()", header, rawChannel.getHeader());
        assertEquals("rawChannel.getOutgoingFilters().size()", 0, rawChannel.getOutgoingFilters().size());
        assertSame("rawChannel.getServerSocket()", serverSocket, rawChannel.getServerSocket());
        assertEquals("rawChannel.getOriginalRealm()", "org.jpos.iso.channel.RawChannel", rawChannel.getOriginalRealm());
        assertNull("rawChannel.getRealm()", rawChannel.getRealm());
        assertNull("rawChannel.getHost()", rawChannel.getHost());
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        try {
            rawChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        try {
            rawChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetHeader() throws Throwable {
        RawChannel rawChannel = new RawChannel();
        rawChannel.setHeader("testRawChannelHeader");
        assertEquals("rawChannel.getHeader().length", 10, rawChannel.getHeader().length);
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        byte[] header = new byte[0];
        RawChannel rawChannel = new RawChannel(mock(VISA1Packager.class), header);
        try {
            rawChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertSame("rawChannel.getHeader()", header, rawChannel.getHeader());
        }
    }
}
