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

import java.io.IOException;
import java.net.ServerSocket;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.junit.Test;

public class ASCIIChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new ISO87APackagerBBitmap();
        ServerSocket serverSocket = new ServerSocket();
        ASCIIChannel aSCIIChannel = new ASCIIChannel(p, serverSocket);
        assertEquals("aSCIIChannel.getIncomingFilters().size()", 0, aSCIIChannel.getIncomingFilters().size());
        assertEquals("aSCIIChannel.getMaxPacketLength()", 100000, aSCIIChannel.getMaxPacketLength());
        assertSame("aSCIIChannel.getPackager()", p, aSCIIChannel.getPackager());
        assertEquals("aSCIIChannel.getPort()", 0, aSCIIChannel.getPort());
        assertEquals("aSCIIChannel.getName()", "", aSCIIChannel.getName());
        assertEquals("aSCIIChannel.getCounters().length", 3, aSCIIChannel.getCounters().length);
        assertNull("aSCIIChannel.getLogger()", aSCIIChannel.getLogger());
        assertNull("aSCIIChannel.getSocketFactory()", aSCIIChannel.getSocketFactory());
        assertNull("aSCIIChannel.getHeader()", aSCIIChannel.getHeader());
        assertEquals("aSCIIChannel.getOutgoingFilters().size()", 0, aSCIIChannel.getOutgoingFilters().size());
        assertSame("aSCIIChannel.getServerSocket()", serverSocket, aSCIIChannel.getServerSocket());
        assertEquals("aSCIIChannel.getOriginalRealm()", "org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm());
        assertNull("aSCIIChannel.getRealm()", aSCIIChannel.getRealm());
        assertNull("aSCIIChannel.getHost()", aSCIIChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO87APackagerBBitmap();
        ASCIIChannel aSCIIChannel = new ASCIIChannel(p);
        assertEquals("aSCIIChannel.getIncomingFilters().size()", 0, aSCIIChannel.getIncomingFilters().size());
        assertEquals("aSCIIChannel.getMaxPacketLength()", 100000, aSCIIChannel.getMaxPacketLength());
        assertSame("aSCIIChannel.getPackager()", p, aSCIIChannel.getPackager());
        assertEquals("aSCIIChannel.getPort()", 0, aSCIIChannel.getPort());
        assertEquals("aSCIIChannel.getName()", "", aSCIIChannel.getName());
        assertEquals("aSCIIChannel.getCounters().length", 3, aSCIIChannel.getCounters().length);
        assertNull("aSCIIChannel.getLogger()", aSCIIChannel.getLogger());
        assertNull("aSCIIChannel.getSocketFactory()", aSCIIChannel.getSocketFactory());
        assertNull("aSCIIChannel.getHeader()", aSCIIChannel.getHeader());
        assertEquals("aSCIIChannel.getOutgoingFilters().size()", 0, aSCIIChannel.getOutgoingFilters().size());
        assertNull("aSCIIChannel.getServerSocket()", aSCIIChannel.getServerSocket());
        assertEquals("aSCIIChannel.getOriginalRealm()", "org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm());
        assertNull("aSCIIChannel.getRealm()", aSCIIChannel.getRealm());
        assertNull("aSCIIChannel.getHost()", aSCIIChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel();
        assertEquals("aSCIIChannel.getIncomingFilters().size()", 0, aSCIIChannel.getIncomingFilters().size());
        assertEquals("aSCIIChannel.getMaxPacketLength()", 100000, aSCIIChannel.getMaxPacketLength());
        assertEquals("aSCIIChannel.getPort()", 0, aSCIIChannel.getPort());
        assertEquals("aSCIIChannel.getName()", "", aSCIIChannel.getName());
        assertEquals("aSCIIChannel.getCounters().length", 3, aSCIIChannel.getCounters().length);
        assertNull("aSCIIChannel.getLogger()", aSCIIChannel.getLogger());
        assertNull("aSCIIChannel.getSocketFactory()", aSCIIChannel.getSocketFactory());
        assertNull("aSCIIChannel.getHeader()", aSCIIChannel.getHeader());
        assertEquals("aSCIIChannel.getOutgoingFilters().size()", 0, aSCIIChannel.getOutgoingFilters().size());
        assertNull("aSCIIChannel.getServerSocket()", aSCIIChannel.getServerSocket());
        assertEquals("aSCIIChannel.getOriginalRealm()", "org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm());
        assertNull("aSCIIChannel.getRealm()", aSCIIChannel.getRealm());
        assertNull("aSCIIChannel.getHost()", aSCIIChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new ISO87APackagerBBitmap();
        ASCIIChannel aSCIIChannel = new ASCIIChannel("testASCIIChannelHost", 100, p);
        assertEquals("aSCIIChannel.getIncomingFilters().size()", 0, aSCIIChannel.getIncomingFilters().size());
        assertEquals("aSCIIChannel.getMaxPacketLength()", 100000, aSCIIChannel.getMaxPacketLength());
        assertSame("aSCIIChannel.getPackager()", p, aSCIIChannel.getPackager());
        assertEquals("aSCIIChannel.getPort()", 100, aSCIIChannel.getPort());
        assertEquals("aSCIIChannel.getName()", "", aSCIIChannel.getName());
        assertEquals("aSCIIChannel.getCounters().length", 3, aSCIIChannel.getCounters().length);
        assertNull("aSCIIChannel.getLogger()", aSCIIChannel.getLogger());
        assertNull("aSCIIChannel.getSocketFactory()", aSCIIChannel.getSocketFactory());
        assertNull("aSCIIChannel.getHeader()", aSCIIChannel.getHeader());
        assertEquals("aSCIIChannel.getOutgoingFilters().size()", 0, aSCIIChannel.getOutgoingFilters().size());
        assertNull("aSCIIChannel.getServerSocket()", aSCIIChannel.getServerSocket());
        assertEquals("aSCIIChannel.getOriginalRealm()", "org.jpos.iso.channel.ASCIIChannel", aSCIIChannel.getOriginalRealm());
        assertNull("aSCIIChannel.getRealm()", aSCIIChannel.getRealm());
        assertEquals("aSCIIChannel.getHost()", "testASCIIChannelHost", aSCIIChannel.getHost());
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel(new Base1Packager());
        try {
            aSCIIChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageLength() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel("testASCIIChannelHost", 100, new GenericPackager());
        aSCIIChannel.sendMessageLength(Integer.MIN_VALUE);
        assertTrue("Executed without Exception", true);
    }

    @Test
    public void testSendMessageLengthThrowsIOException() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel();
        try {
            aSCIIChannel.sendMessageLength(10000);
            fail("Expected IOException to be thrown");
        } catch (IOException ex) {
            assertEquals("ex.getClass()", IOException.class, ex.getClass());
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel();
        try {
            aSCIIChannel.sendMessageLength(9999);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException1() throws Throwable {
        ASCIIChannel aSCIIChannel = new ASCIIChannel(new Base1SubFieldPackager());
        try {
            aSCIIChannel.sendMessageLength(9998);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
