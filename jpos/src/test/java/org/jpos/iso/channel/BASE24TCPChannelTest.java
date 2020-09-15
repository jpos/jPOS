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

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.iso.packager.ISO93APackager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BASE24TCPChannelTest {
    @Mock
    ISOMsg m;

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new ISO87APackager();
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel(p);
        assertEquals(0, bASE24TCPChannel.getIncomingFilters().size(), "bASE24TCPChannel.getIncomingFilters().size()");
        assertEquals(100000, bASE24TCPChannel.getMaxPacketLength(), "bASE24TCPChannel.getMaxPacketLength()");
        assertSame(p, bASE24TCPChannel.getPackager(), "bASE24TCPChannel.getPackager()");
        assertEquals(0, bASE24TCPChannel.getPort(), "bASE24TCPChannel.getPort()");
        assertEquals("", bASE24TCPChannel.getName(), "bASE24TCPChannel.getName()");
        assertEquals(3, bASE24TCPChannel.getCounters().length, "bASE24TCPChannel.getCounters().length");
        assertNull(bASE24TCPChannel.getLogger(), "bASE24TCPChannel.getLogger()");
        assertNull(bASE24TCPChannel.getSocketFactory(), "bASE24TCPChannel.getSocketFactory()");
        assertNull(bASE24TCPChannel.getHeader(), "bASE24TCPChannel.getHeader()");
        assertEquals(0, bASE24TCPChannel.getOutgoingFilters().size(), "bASE24TCPChannel.getOutgoingFilters().size()");
        assertNull(bASE24TCPChannel.getServerSocket(), "bASE24TCPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.BASE24TCPChannel", bASE24TCPChannel.getOriginalRealm(),
                "bASE24TCPChannel.getOriginalRealm()");
        assertNull(bASE24TCPChannel.getRealm(), "bASE24TCPChannel.getRealm()");
        assertNull(bASE24TCPChannel.getHost(), "bASE24TCPChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO87APackager();
        ServerSocket serverSocket = new ServerSocket();
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel(p, serverSocket);
        assertEquals(0, bASE24TCPChannel.getIncomingFilters().size(), "bASE24TCPChannel.getIncomingFilters().size()");
        assertEquals(100000, bASE24TCPChannel.getMaxPacketLength(), "bASE24TCPChannel.getMaxPacketLength()");
        assertSame(p, bASE24TCPChannel.getPackager(), "bASE24TCPChannel.getPackager()");
        assertEquals(0, bASE24TCPChannel.getPort(), "bASE24TCPChannel.getPort()");
        assertEquals("", bASE24TCPChannel.getName(), "bASE24TCPChannel.getName()");
        assertEquals(3, bASE24TCPChannel.getCounters().length, "bASE24TCPChannel.getCounters().length");
        assertNull(bASE24TCPChannel.getLogger(), "bASE24TCPChannel.getLogger()");
        assertNull(bASE24TCPChannel.getSocketFactory(), "bASE24TCPChannel.getSocketFactory()");
        assertNull(bASE24TCPChannel.getHeader(), "bASE24TCPChannel.getHeader()");
        assertEquals(0, bASE24TCPChannel.getOutgoingFilters().size(), "bASE24TCPChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, bASE24TCPChannel.getServerSocket(), "bASE24TCPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.BASE24TCPChannel", bASE24TCPChannel.getOriginalRealm(),
                "bASE24TCPChannel.getOriginalRealm()");
        assertNull(bASE24TCPChannel.getRealm(), "bASE24TCPChannel.getRealm()");
        assertNull(bASE24TCPChannel.getHost(), "bASE24TCPChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new ISO93APackager();
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel("testBASE24TCPChannelHost", 100, p);
        assertEquals(0, bASE24TCPChannel.getIncomingFilters().size(), "bASE24TCPChannel.getIncomingFilters().size()");
        assertEquals(100000, bASE24TCPChannel.getMaxPacketLength(), "bASE24TCPChannel.getMaxPacketLength()");
        assertSame(p, bASE24TCPChannel.getPackager(), "bASE24TCPChannel.getPackager()");
        assertEquals(100, bASE24TCPChannel.getPort(), "bASE24TCPChannel.getPort()");
        assertEquals("", bASE24TCPChannel.getName(), "bASE24TCPChannel.getName()");
        assertEquals(3, bASE24TCPChannel.getCounters().length, "bASE24TCPChannel.getCounters().length");
        assertNull(bASE24TCPChannel.getLogger(), "bASE24TCPChannel.getLogger()");
        assertNull(bASE24TCPChannel.getSocketFactory(), "bASE24TCPChannel.getSocketFactory()");
        assertNull(bASE24TCPChannel.getHeader(), "bASE24TCPChannel.getHeader()");
        assertEquals(0, bASE24TCPChannel.getOutgoingFilters().size(), "bASE24TCPChannel.getOutgoingFilters().size()");
        assertNull(bASE24TCPChannel.getServerSocket(), "bASE24TCPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.BASE24TCPChannel", bASE24TCPChannel.getOriginalRealm(),
                "bASE24TCPChannel.getOriginalRealm()");
        assertNull(bASE24TCPChannel.getRealm(), "bASE24TCPChannel.getRealm()");
        assertEquals("testBASE24TCPChannelHost", bASE24TCPChannel.getHost(), "bASE24TCPChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel();
        assertEquals(0, bASE24TCPChannel.getIncomingFilters().size(), "bASE24TCPChannel.getIncomingFilters().size()");
        assertEquals(100000, bASE24TCPChannel.getMaxPacketLength(), "bASE24TCPChannel.getMaxPacketLength()");
        assertEquals(0, bASE24TCPChannel.getPort(), "bASE24TCPChannel.getPort()");
        assertEquals("", bASE24TCPChannel.getName(), "bASE24TCPChannel.getName()");
        assertEquals(3, bASE24TCPChannel.getCounters().length, "bASE24TCPChannel.getCounters().length");
        assertNull(bASE24TCPChannel.getLogger(), "bASE24TCPChannel.getLogger()");
        assertNull(bASE24TCPChannel.getSocketFactory(), "bASE24TCPChannel.getSocketFactory()");
        assertNull(bASE24TCPChannel.getHeader(), "bASE24TCPChannel.getHeader()");
        assertEquals(0, bASE24TCPChannel.getOutgoingFilters().size(), "bASE24TCPChannel.getOutgoingFilters().size()");
        assertNull(bASE24TCPChannel.getServerSocket(), "bASE24TCPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.BASE24TCPChannel", bASE24TCPChannel.getOriginalRealm(),
                "bASE24TCPChannel.getOriginalRealm()");
        assertNull(bASE24TCPChannel.getRealm(), "bASE24TCPChannel.getRealm()");
        assertNull(bASE24TCPChannel.getHost(), "bASE24TCPChannel.getHost()");
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel(new ISO87APackager());
        try {
            bASE24TCPChannel.sendMessageLength(100);
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
    public void testSendMessageTraillerThrowsNullPointerException() throws Throwable {
        BASE24TCPChannel bASE24TCPChannel = new BASE24TCPChannel();

        try {
            bASE24TCPChannel.sendMessageTrailler(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(int)\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
