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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import java.net.ServerSocket;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.*;
import org.jpos.iso.header.BASE1Header;
import org.jpos.iso.header.BaseHeader;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.Base1SubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.iso.packager.ISO93APackager;
import org.jpos.iso.packager.XMLPackager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VAPChannelTest {

    @Mock
    ISOVMsg m;

    @Test
    public void testConstructor() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        assertEquals(0, vAPChannel.getIncomingFilters().size(), "vAPChannel.getIncomingFilters().size()");
        assertEquals(100000, vAPChannel.getMaxPacketLength(), "vAPChannel.getMaxPacketLength()");
        assertEquals(0, vAPChannel.getPort(), "vAPChannel.getPort()");
        assertEquals("", vAPChannel.getName(), "vAPChannel.getName()");
        assertEquals(3, vAPChannel.getCounters().length, "vAPChannel.getCounters().length");
        assertEquals("000000", vAPChannel.srcid, "vAPChannel.srcid");
        assertNull(vAPChannel.getLogger(), "vAPChannel.getLogger()");
        assertNull(vAPChannel.getSocketFactory(), "vAPChannel.getSocketFactory()");
        assertNull(vAPChannel.getHeader(), "vAPChannel.getHeader()");
        assertEquals(0, vAPChannel.getOutgoingFilters().size(), "vAPChannel.getOutgoingFilters().size()");
        assertNull(vAPChannel.getServerSocket(), "vAPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.VAPChannel", vAPChannel.getOriginalRealm(), "vAPChannel.getOriginalRealm()");
        assertNull(vAPChannel.getRealm(), "vAPChannel.getRealm()");
        assertEquals("000000", vAPChannel.dstid, "vAPChannel.dstid");
        assertNull(vAPChannel.getHost(), "vAPChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO93APackager();
        ServerSocket serverSocket = new ServerSocket();
        VAPChannel vAPChannel = new VAPChannel(p, serverSocket);
        assertEquals(0, vAPChannel.getIncomingFilters().size(), "vAPChannel.getIncomingFilters().size()");
        assertEquals(100000, vAPChannel.getMaxPacketLength(), "vAPChannel.getMaxPacketLength()");
        assertSame(p, vAPChannel.getPackager(), "vAPChannel.getPackager()");
        assertEquals(0, vAPChannel.getPort(), "vAPChannel.getPort()");
        assertEquals("", vAPChannel.getName(), "vAPChannel.getName()");
        assertEquals(3, vAPChannel.getCounters().length, "vAPChannel.getCounters().length");
        assertNull(vAPChannel.getLogger(), "vAPChannel.getLogger()");
        assertNull(vAPChannel.getSocketFactory(), "vAPChannel.getSocketFactory()");
        assertNull(vAPChannel.getHeader(), "vAPChannel.getHeader()");
        assertEquals(0, vAPChannel.getOutgoingFilters().size(), "vAPChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, vAPChannel.getServerSocket(), "vAPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.VAPChannel", vAPChannel.getOriginalRealm(), "vAPChannel.getOriginalRealm()");
        assertNull(vAPChannel.getRealm(), "vAPChannel.getRealm()");
        assertNull(vAPChannel.getHost(), "vAPChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new GenericPackager();
        VAPChannel vAPChannel = new VAPChannel(p);
        assertEquals(0, vAPChannel.getIncomingFilters().size(), "vAPChannel.getIncomingFilters().size()");
        assertEquals(100000, vAPChannel.getMaxPacketLength(), "vAPChannel.getMaxPacketLength()");
        assertSame(p, vAPChannel.getPackager(), "vAPChannel.getPackager()");
        assertEquals(0, vAPChannel.getPort(), "vAPChannel.getPort()");
        assertEquals("", vAPChannel.getName(), "vAPChannel.getName()");
        assertEquals(3, vAPChannel.getCounters().length, "vAPChannel.getCounters().length");
        assertNull(vAPChannel.getLogger(), "vAPChannel.getLogger()");
        assertNull(vAPChannel.getSocketFactory(), "vAPChannel.getSocketFactory()");
        assertNull(vAPChannel.getHeader(), "vAPChannel.getHeader()");
        assertEquals(0, vAPChannel.getOutgoingFilters().size(), "vAPChannel.getOutgoingFilters().size()");
        assertNull(vAPChannel.getServerSocket(), "vAPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.VAPChannel", vAPChannel.getOriginalRealm(), "vAPChannel.getOriginalRealm()");
        assertNull(vAPChannel.getRealm(), "vAPChannel.getRealm()");
        assertNull(vAPChannel.getHost(), "vAPChannel.getHost()");
    }

    @Test
    public void testGetDynamicHeader() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        byte[] image = new byte[0];
        BASE1Header result = (BASE1Header) vAPChannel.getDynamicHeader(image);
        assertNotNull(result);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        try {
            vAPChannel.getMessageLength();
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
    public void testReadHeaderThrowsNullPointerException() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        try {
            vAPChannel.readHeader(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataInputStream.read()\" because \"this.serverIn\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSendMessageHeaderThrowsArrayIndexOutOfBoundsException() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        final byte[] image = new byte[2];
        VAPChannel vAPChannel2 = new VAPChannel(new ISO87BPackager());
        final BASE1Header dynamicHeader = (BASE1Header) vAPChannel2.getDynamicHeader(image);

        given(m.getHeader()).willReturn(image);
        given(m.getISOHeader()).willReturn(dynamicHeader);

        try {
            vAPChannel.sendMessageHeader(m, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("3", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 3 out of bounds for length 2", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        ISOMsg m = new ISOMsg("testVAPChannelMti");
        byte[] b = new byte[0];
        m.setHeader(b);
        try {
            vAPChannel.sendMessageHeader(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(byte[])\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, m.getDirection(), "m.getDirection()");
        }
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException1() throws Throwable {
        ISOMsg m = new ISOMsg(100);
        VAPChannel vAPChannel = new VAPChannel(new Base1Packager());
        vAPChannel.setConfiguration(new SimpleConfiguration());
        try {
            vAPChannel.sendMessageHeader(m, 100);
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
    public void testSendMessageHeaderThrowsNullPointerException2() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel("testVAPChannelHost", 100, new XMLPackager());
        ISOMsg m = new ISOMsg("testVAPChannelMti");
        m.setHeader(new BaseHeader());
        try {
            vAPChannel.sendMessageHeader(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.DataOutputStream.write(byte[])\" because \"this.serverOut\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, m.getDirection(), "m.getDirection()");
        }
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException3() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        try {
            vAPChannel.sendMessageHeader(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ISOMsg.getHeader()\" because \"m\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel(new Base1Packager());
        vAPChannel.setSocketFactory(new GenericSSLSocketFactory());
        vAPChannel.setConfiguration(new SimpleConfiguration());
        assertEquals("000000", vAPChannel.srcid, "vAPChannel.srcid");
        assertEquals("000000", vAPChannel.dstid, "vAPChannel.dstid");
        assertEquals(100000, vAPChannel.getMaxPacketLength(), "vAPChannel.getMaxPacketLength()");
        assertEquals(0, vAPChannel.getPort(), "vAPChannel.getPort()");
        assertNull(vAPChannel.getSocket(), "vAPChannel.getSocket()");
        assertEquals(300000, vAPChannel.getTimeout(), "vAPChannel.getTimeout()");
        assertFalse(vAPChannel.isOverrideHeader(), "vAPChannel.isOverrideHeader()");
        assertNull(vAPChannel.getHost(), "vAPChannel.getHost()");
    }

    @Test
    public void testSetConfiguration1() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel(new Base1SubFieldPackager());
        vAPChannel.setConfiguration(new SimpleConfiguration());
        assertEquals("000000", vAPChannel.srcid, "vAPChannel.srcid");
        assertEquals("000000", vAPChannel.dstid, "vAPChannel.dstid");
        assertEquals(100000, vAPChannel.getMaxPacketLength(), "vAPChannel.getMaxPacketLength()");
        assertEquals(0, vAPChannel.getPort(), "vAPChannel.getPort()");
        assertNull(vAPChannel.getSocket(), "vAPChannel.getSocket()");
        assertEquals(300000, vAPChannel.getTimeout(), "vAPChannel.getTimeout()");
        assertFalse(vAPChannel.isOverrideHeader(), "vAPChannel.isOverrideHeader()");
        assertNull(vAPChannel.getHost(), "vAPChannel.getHost()");
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel(new GenericPackager());
        Configuration cfg = new SubConfiguration();
        try {
            vAPChannel.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(100000, vAPChannel.getMaxPacketLength(), "vAPChannel.getMaxPacketLength()");
            assertEquals(0, vAPChannel.getPort(), "vAPChannel.getPort()");
            assertNull(vAPChannel.getSocket(), "vAPChannel.getSocket()");
            assertEquals(0, vAPChannel.getTimeout(), "vAPChannel.getTimeout()");
            assertEquals("000000", vAPChannel.srcid, "vAPChannel.srcid");
            assertFalse(vAPChannel.isOverrideHeader(), "vAPChannel.isOverrideHeader()");
            assertNull(vAPChannel.getHost(), "vAPChannel.getHost()");
            assertEquals("000000", vAPChannel.dstid, "vAPChannel.dstid");
        }
    }

    @Test
    public void testShouldIgnore() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        byte[] b = new byte[3];
        b[2] = (byte) 3;
        boolean result = vAPChannel.shouldIgnore(b);
        assertTrue(result, "result");
    }

    @Test
    public void testShouldIgnore1() throws Throwable {
        byte[] b = new byte[3];
        VAPChannel vAPChannel = new VAPChannel();
        boolean result = vAPChannel.shouldIgnore(b);
        assertFalse(result, "result");
    }

    @Test
    public void testShouldIgnore2() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel(new GenericPackager());
        boolean result = vAPChannel.shouldIgnore(null);
        assertFalse(result, "result");
    }

    @Test
    public void testShouldIgnoreThrowsArrayIndexOutOfBoundsException() throws Throwable {
        VAPChannel vAPChannel = new VAPChannel();
        byte[] b = new byte[0];
        try {
            vAPChannel.shouldIgnore(b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 2 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
