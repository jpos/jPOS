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

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.jpos.iso.packager.PostPackager;
import org.junit.Test;

public class BASE24ChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new PostPackager();
        BASE24Channel bASE24Channel = new BASE24Channel(p);
        assertEquals("bASE24Channel.getIncomingFilters().size()", 0, bASE24Channel.getIncomingFilters().size());
        assertEquals("bASE24Channel.getMaxPacketLength()", 100000, bASE24Channel.getMaxPacketLength());
        assertSame("bASE24Channel.getPackager()", p, bASE24Channel.getPackager());
        assertEquals("bASE24Channel.getPort()", 0, bASE24Channel.getPort());
        assertEquals("bASE24Channel.getName()", "", bASE24Channel.getName());
        assertEquals("bASE24Channel.getCounters().length", 3, bASE24Channel.getCounters().length);
        assertNull("bASE24Channel.getLogger()", bASE24Channel.getLogger());
        assertNull("bASE24Channel.getSocketFactory()", bASE24Channel.getSocketFactory());
        assertNull("bASE24Channel.getHeader()", bASE24Channel.getHeader());
        assertEquals("bASE24Channel.getOutgoingFilters().size()", 0, bASE24Channel.getOutgoingFilters().size());
        assertNull("bASE24Channel.getServerSocket()", bASE24Channel.getServerSocket());
        assertEquals("bASE24Channel.getOriginalRealm()", "org.jpos.iso.channel.BASE24Channel", bASE24Channel.getOriginalRealm());
        assertNull("bASE24Channel.getRealm()", bASE24Channel.getRealm());
        assertNull("bASE24Channel.getHost()", bASE24Channel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        BASE24Channel bASE24Channel = new BASE24Channel();
        assertEquals("bASE24Channel.getIncomingFilters().size()", 0, bASE24Channel.getIncomingFilters().size());
        assertEquals("bASE24Channel.getMaxPacketLength()", 100000, bASE24Channel.getMaxPacketLength());
        assertEquals("bASE24Channel.getPort()", 0, bASE24Channel.getPort());
        assertEquals("bASE24Channel.getName()", "", bASE24Channel.getName());
        assertEquals("bASE24Channel.getCounters().length", 3, bASE24Channel.getCounters().length);
        assertNull("bASE24Channel.getLogger()", bASE24Channel.getLogger());
        assertNull("bASE24Channel.getSocketFactory()", bASE24Channel.getSocketFactory());
        assertNull("bASE24Channel.getHeader()", bASE24Channel.getHeader());
        assertEquals("bASE24Channel.getOutgoingFilters().size()", 0, bASE24Channel.getOutgoingFilters().size());
        assertNull("bASE24Channel.getServerSocket()", bASE24Channel.getServerSocket());
        assertEquals("bASE24Channel.getOriginalRealm()", "org.jpos.iso.channel.BASE24Channel", bASE24Channel.getOriginalRealm());
        assertNull("bASE24Channel.getRealm()", bASE24Channel.getRealm());
        assertNull("bASE24Channel.getHost()", bASE24Channel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new ISOBaseValidatingPackager();
        BASE24Channel bASE24Channel = new BASE24Channel("testBASE24ChannelHost", 100, p);
        assertEquals("bASE24Channel.getIncomingFilters().size()", 0, bASE24Channel.getIncomingFilters().size());
        assertEquals("bASE24Channel.getMaxPacketLength()", 100000, bASE24Channel.getMaxPacketLength());
        assertSame("bASE24Channel.getPackager()", p, bASE24Channel.getPackager());
        assertEquals("bASE24Channel.getPort()", 100, bASE24Channel.getPort());
        assertEquals("bASE24Channel.getName()", "", bASE24Channel.getName());
        assertEquals("bASE24Channel.getCounters().length", 3, bASE24Channel.getCounters().length);
        assertNull("bASE24Channel.getLogger()", bASE24Channel.getLogger());
        assertNull("bASE24Channel.getSocketFactory()", bASE24Channel.getSocketFactory());
        assertNull("bASE24Channel.getHeader()", bASE24Channel.getHeader());
        assertEquals("bASE24Channel.getOutgoingFilters().size()", 0, bASE24Channel.getOutgoingFilters().size());
        assertNull("bASE24Channel.getServerSocket()", bASE24Channel.getServerSocket());
        assertEquals("bASE24Channel.getOriginalRealm()", "org.jpos.iso.channel.BASE24Channel", bASE24Channel.getOriginalRealm());
        assertNull("bASE24Channel.getRealm()", bASE24Channel.getRealm());
        assertEquals("bASE24Channel.getHost()", "testBASE24ChannelHost", bASE24Channel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new Base1SubFieldPackager();
        ServerSocket serverSocket = new ServerSocket();
        BASE24Channel bASE24Channel = new BASE24Channel(p, serverSocket);
        assertEquals("bASE24Channel.getIncomingFilters().size()", 0, bASE24Channel.getIncomingFilters().size());
        assertEquals("bASE24Channel.getMaxPacketLength()", 100000, bASE24Channel.getMaxPacketLength());
        assertSame("bASE24Channel.getPackager()", p, bASE24Channel.getPackager());
        assertEquals("bASE24Channel.getPort()", 0, bASE24Channel.getPort());
        assertEquals("bASE24Channel.getName()", "", bASE24Channel.getName());
        assertEquals("bASE24Channel.getCounters().length", 3, bASE24Channel.getCounters().length);
        assertNull("bASE24Channel.getLogger()", bASE24Channel.getLogger());
        assertNull("bASE24Channel.getSocketFactory()", bASE24Channel.getSocketFactory());
        assertNull("bASE24Channel.getHeader()", bASE24Channel.getHeader());
        assertEquals("bASE24Channel.getOutgoingFilters().size()", 0, bASE24Channel.getOutgoingFilters().size());
        assertSame("bASE24Channel.getServerSocket()", serverSocket, bASE24Channel.getServerSocket());
        assertEquals("bASE24Channel.getOriginalRealm()", "org.jpos.iso.channel.BASE24Channel", bASE24Channel.getOriginalRealm());
        assertNull("bASE24Channel.getRealm()", bASE24Channel.getRealm());
        assertNull("bASE24Channel.getHost()", bASE24Channel.getHost());
    }

    @Test
    public void testSendMessageTraillerThrowsNullPointerException() throws Throwable {
        BASE24Channel bASE24Channel = new BASE24Channel(null);
        try {
            bASE24Channel.sendMessageTrailler(new ISOMsg(), 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStreamReceiveThrowsNullPointerException() throws Throwable {
        BASE24Channel bASE24Channel = new BASE24Channel(new ISO93APackager());
        try {
            bASE24Channel.streamReceive();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
