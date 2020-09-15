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
import org.jpos.iso.packager.ISO93BPackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.junit.jupiter.api.Test;

public class PADChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new ISOBaseValidatingPackager();
        PADChannel pADChannel = new PADChannel("testPADChannelHost", 100, p);
        assertEquals(0, pADChannel.getIncomingFilters().size(), "pADChannel.getIncomingFilters().size()");
        assertEquals(100000, pADChannel.getMaxPacketLength(), "pADChannel.getMaxPacketLength()");
        assertSame(p, pADChannel.getPackager(), "pADChannel.getPackager()");
        assertEquals(100, pADChannel.getPort(), "pADChannel.getPort()");
        assertEquals("", pADChannel.getName(), "pADChannel.getName()");
        assertEquals(3, pADChannel.getCounters().length, "pADChannel.getCounters().length");
        assertNull(pADChannel.getLogger(), "pADChannel.getLogger()");
        assertNull(pADChannel.getSocketFactory(), "pADChannel.getSocketFactory()");
        assertNull(pADChannel.getHeader(), "pADChannel.getHeader()");
        assertEquals(0, pADChannel.getOutgoingFilters().size(), "pADChannel.getOutgoingFilters().size()");
        assertNull(pADChannel.getServerSocket(), "pADChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm(), "pADChannel.getOriginalRealm()");
        assertNull(pADChannel.getRealm(), "pADChannel.getRealm()");
        assertEquals("testPADChannelHost", pADChannel.getHost(), "pADChannel.getHost()");
        assertNull(pADChannel.reader, "pADChannel.reader");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO93BPackager();
        ServerSocket serverSocket = new ServerSocket();
        PADChannel pADChannel = new PADChannel(p, serverSocket);
        assertEquals(0, pADChannel.getIncomingFilters().size(), "pADChannel.getIncomingFilters().size()");
        assertEquals(100000, pADChannel.getMaxPacketLength(), "pADChannel.getMaxPacketLength()");
        assertSame(p, pADChannel.getPackager(), "pADChannel.getPackager()");
        assertEquals(0, pADChannel.getPort(), "pADChannel.getPort()");
        assertEquals("", pADChannel.getName(), "pADChannel.getName()");
        assertEquals(3, pADChannel.getCounters().length, "pADChannel.getCounters().length");
        assertNull(pADChannel.getLogger(), "pADChannel.getLogger()");
        assertNull(pADChannel.getSocketFactory(), "pADChannel.getSocketFactory()");
        assertNull(pADChannel.getHeader(), "pADChannel.getHeader()");
        assertEquals(0, pADChannel.getOutgoingFilters().size(), "pADChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, pADChannel.getServerSocket(), "pADChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm(), "pADChannel.getOriginalRealm()");
        assertNull(pADChannel.getRealm(), "pADChannel.getRealm()");
        assertNull(pADChannel.getHost(), "pADChannel.getHost()");
        assertNull(pADChannel.reader, "pADChannel.reader");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new ISO93BPackager();
        PADChannel pADChannel = new PADChannel(p);
        assertEquals(0, pADChannel.getIncomingFilters().size(), "pADChannel.getIncomingFilters().size()");
        assertEquals(100000, pADChannel.getMaxPacketLength(), "pADChannel.getMaxPacketLength()");
        assertSame(p, pADChannel.getPackager(), "pADChannel.getPackager()");
        assertEquals(0, pADChannel.getPort(), "pADChannel.getPort()");
        assertEquals("", pADChannel.getName(), "pADChannel.getName()");
        assertEquals(3, pADChannel.getCounters().length, "pADChannel.getCounters().length");
        assertNull(pADChannel.getLogger(), "pADChannel.getLogger()");
        assertNull(pADChannel.getSocketFactory(), "pADChannel.getSocketFactory()");
        assertNull(pADChannel.getHeader(), "pADChannel.getHeader()");
        assertEquals(0, pADChannel.getOutgoingFilters().size(), "pADChannel.getOutgoingFilters().size()");
        assertNull(pADChannel.getServerSocket(), "pADChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm(), "pADChannel.getOriginalRealm()");
        assertNull(pADChannel.getRealm(), "pADChannel.getRealm()");
        assertNull(pADChannel.getHost(), "pADChannel.getHost()");
        assertNull(pADChannel.reader, "pADChannel.reader");
    }

    @Test
    public void testConstructor3() throws Throwable {
        PADChannel pADChannel = new PADChannel();
        assertEquals(0, pADChannel.getIncomingFilters().size(), "pADChannel.getIncomingFilters().size()");
        assertEquals(100000, pADChannel.getMaxPacketLength(), "pADChannel.getMaxPacketLength()");
        assertEquals(0, pADChannel.getPort(), "pADChannel.getPort()");
        assertEquals("", pADChannel.getName(), "pADChannel.getName()");
        assertEquals(3, pADChannel.getCounters().length, "pADChannel.getCounters().length");
        assertNull(pADChannel.getLogger(), "pADChannel.getLogger()");
        assertNull(pADChannel.getSocketFactory(), "pADChannel.getSocketFactory()");
        assertNull(pADChannel.getHeader(), "pADChannel.getHeader()");
        assertEquals(0, pADChannel.getOutgoingFilters().size(), "pADChannel.getOutgoingFilters().size()");
        assertNull(pADChannel.getServerSocket(), "pADChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm(), "pADChannel.getOriginalRealm()");
        assertNull(pADChannel.getRealm(), "pADChannel.getRealm()");
        assertNull(pADChannel.getHost(), "pADChannel.getHost()");
        assertNull(pADChannel.reader, "pADChannel.reader");
    }

    @Test
    public void testSetHeader() throws Throwable {
        PADChannel pADChannel = new PADChannel();
        pADChannel.setHeader("testPADChannelHeader");
        assertEquals(10, pADChannel.getHeader().length, "pADChannel.getHeader().length");
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        PADChannel pADChannel = new PADChannel();
        try {
            pADChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.getBytes()\" because \"header\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(pADChannel.getHeader(), "pADChannel.getHeader()");
        }
    }
}
