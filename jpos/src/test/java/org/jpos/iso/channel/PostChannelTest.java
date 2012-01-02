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

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.junit.Test;

public class PostChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        PostChannel postChannel = new PostChannel();
        assertEquals("postChannel.getIncomingFilters().size()", 0, postChannel.getIncomingFilters().size());
        assertEquals("postChannel.getMaxPacketLength()", 100000, postChannel.getMaxPacketLength());
        assertEquals("postChannel.getPort()", 0, postChannel.getPort());
        assertEquals("postChannel.getName()", "", postChannel.getName());
        assertEquals("postChannel.getCounters().length", 3, postChannel.getCounters().length);
        assertNull("postChannel.getLogger()", postChannel.getLogger());
        assertNull("postChannel.getSocketFactory()", postChannel.getSocketFactory());
        assertNull("postChannel.getHeader()", postChannel.getHeader());
        assertEquals("postChannel.getOutgoingFilters().size()", 0, postChannel.getOutgoingFilters().size());
        assertNull("postChannel.getServerSocket()", postChannel.getServerSocket());
        assertEquals("postChannel.getOriginalRealm()", "org.jpos.iso.channel.PostChannel", postChannel.getOriginalRealm());
        assertNull("postChannel.getRealm()", postChannel.getRealm());
        assertNull("postChannel.getHost()", postChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO93APackager();
        PostChannel postChannel = new PostChannel("testPostChannelHost", 100, p);
        assertEquals("postChannel.getIncomingFilters().size()", 0, postChannel.getIncomingFilters().size());
        assertEquals("postChannel.getMaxPacketLength()", 100000, postChannel.getMaxPacketLength());
        assertSame("postChannel.getPackager()", p, postChannel.getPackager());
        assertEquals("postChannel.getPort()", 100, postChannel.getPort());
        assertEquals("postChannel.getName()", "", postChannel.getName());
        assertEquals("postChannel.getCounters().length", 3, postChannel.getCounters().length);
        assertNull("postChannel.getLogger()", postChannel.getLogger());
        assertNull("postChannel.getSocketFactory()", postChannel.getSocketFactory());
        assertNull("postChannel.getHeader()", postChannel.getHeader());
        assertEquals("postChannel.getOutgoingFilters().size()", 0, postChannel.getOutgoingFilters().size());
        assertNull("postChannel.getServerSocket()", postChannel.getServerSocket());
        assertEquals("postChannel.getOriginalRealm()", "org.jpos.iso.channel.PostChannel", postChannel.getOriginalRealm());
        assertNull("postChannel.getRealm()", postChannel.getRealm());
        assertEquals("postChannel.getHost()", "testPostChannelHost", postChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new ISOBaseValidatingPackager();
        PostChannel postChannel = new PostChannel(p);
        assertEquals("postChannel.getIncomingFilters().size()", 0, postChannel.getIncomingFilters().size());
        assertEquals("postChannel.getMaxPacketLength()", 100000, postChannel.getMaxPacketLength());
        assertSame("postChannel.getPackager()", p, postChannel.getPackager());
        assertEquals("postChannel.getPort()", 0, postChannel.getPort());
        assertEquals("postChannel.getName()", "", postChannel.getName());
        assertEquals("postChannel.getCounters().length", 3, postChannel.getCounters().length);
        assertNull("postChannel.getLogger()", postChannel.getLogger());
        assertNull("postChannel.getSocketFactory()", postChannel.getSocketFactory());
        assertNull("postChannel.getHeader()", postChannel.getHeader());
        assertEquals("postChannel.getOutgoingFilters().size()", 0, postChannel.getOutgoingFilters().size());
        assertNull("postChannel.getServerSocket()", postChannel.getServerSocket());
        assertEquals("postChannel.getOriginalRealm()", "org.jpos.iso.channel.PostChannel", postChannel.getOriginalRealm());
        assertNull("postChannel.getRealm()", postChannel.getRealm());
        assertNull("postChannel.getHost()", postChannel.getHost());
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        PostChannel postChannel = new PostChannel();
        try {
            postChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        PostChannel postChannel = new PostChannel(new ISO93APackager());
        try {
            postChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetHeader() throws Throwable {
        PostChannel postChannel = new PostChannel();
        postChannel.setHeader("testPostChannelHeader");
        assertEquals("postChannel.getHeader().length", 10, postChannel.getHeader().length);
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        PostChannel postChannel = new PostChannel();
        try {
            postChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("postChannel.getHeader()", postChannel.getHeader());
        }
    }
}
