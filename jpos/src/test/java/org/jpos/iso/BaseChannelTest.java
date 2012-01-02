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

package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import org.jpos.bsh.BSHFilter;
import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.channel.BASE24Channel;
import org.jpos.iso.channel.BASE24TCPChannel;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.channel.GZIPChannel;
import org.jpos.iso.channel.HEXChannel;
import org.jpos.iso.channel.LogChannel;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.channel.RawChannel;
import org.jpos.iso.channel.X25Channel;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.filter.DelayFilter;
import org.jpos.iso.filter.MD5Filter;
import org.jpos.iso.filter.MacroFilter;
import org.jpos.iso.filter.StatefulFilter;
import org.jpos.iso.filter.XSLTFilter;
import org.jpos.iso.header.BaseHeader;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.GenericValidatingPackager;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.iso.packager.ISO87APackagerBBitmap;
import org.jpos.iso.packager.ISO93BPackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.jpos.iso.packager.PostPackager;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class BaseChannelTest {
    @Mock
    ISOMsg m;
    @Mock
    ISOFilter filter;

    @Test
    public void testAcceptThrowsNullPointerException() throws Throwable {
        BaseChannel xMLChannel = new XMLChannel(new PostPackager());
        try {
            xMLChannel.accept(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(XMLChannel) xMLChannel.logger", ((XMLChannel) xMLChannel).logger);
            assertNull("(XMLChannel) xMLChannel.originalRealm", ((XMLChannel) xMLChannel).originalRealm);
            assertNull("(XMLChannel) xMLChannel.serverIn", ((XMLChannel) xMLChannel).serverIn);
            assertNull("(XMLChannel) xMLChannel.serverOut", ((XMLChannel) xMLChannel).serverOut);
            assertNull("(XMLChannel) xMLChannel.getSocket()", xMLChannel.getSocket());
            assertEquals("(XMLChannel) xMLChannel.cnt.length", 3, ((XMLChannel) xMLChannel).cnt.length);
            assertNull("(XMLChannel) xMLChannel.realm", ((XMLChannel) xMLChannel).realm);
            assertFalse("(XMLChannel) xMLChannel.usable", ((XMLChannel) xMLChannel).usable);
        }
    }

    @Test
    public void testAcceptThrowsSocketException() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        ServerSocket s = new ServerSocket();
        try {
            gZIPChannel.accept(s);
            fail("Expected SocketException to be thrown");
        } catch (SocketException ex) {
            assertEquals("ex.getClass()", SocketException.class, ex.getClass());
            assertNull("(GZIPChannel) gZIPChannel.logger", ((GZIPChannel) gZIPChannel).logger);
            assertNull("(GZIPChannel) gZIPChannel.originalRealm", ((GZIPChannel) gZIPChannel).originalRealm);
            assertNull("(GZIPChannel) gZIPChannel.serverIn", ((GZIPChannel) gZIPChannel).serverIn);
            assertNull("(GZIPChannel) gZIPChannel.serverOut", ((GZIPChannel) gZIPChannel).serverOut);
            assertNull("(GZIPChannel) gZIPChannel.getSocket()", gZIPChannel.getSocket());
            assertEquals("(GZIPChannel) gZIPChannel.cnt.length", 3, ((GZIPChannel) gZIPChannel).cnt.length);
            assertNull("(GZIPChannel) gZIPChannel.realm", ((GZIPChannel) gZIPChannel).realm);
            assertFalse("(GZIPChannel) gZIPChannel.usable", ((GZIPChannel) gZIPChannel).usable);
            assertFalse("s.isClosed()", s.isClosed());
        }
    }

    @Test
    public void testAddFilter() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.addFilter(new MD5Filter());
        assertEquals("(X25Channel) x25Channel.incomingFilters.size()", 1, ((X25Channel) x25Channel).incomingFilters.size());
        assertEquals("(X25Channel) x25Channel.outgoingFilters.size()", 1, ((X25Channel) x25Channel).outgoingFilters.size());
    }

    @Test
    public void testAddFilter1() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.addFilter(new StatefulFilter(), 100);
        assertEquals("(X25Channel) x25Channel.incomingFilters.size()", 0, ((X25Channel) x25Channel).incomingFilters.size());
        assertEquals("(X25Channel) x25Channel.outgoingFilters.size()", 0, ((X25Channel) x25Channel).outgoingFilters.size());
    }

    @Test
    public void testAddFilter2() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.addFilter(new StatefulFilter(), 1);
        assertEquals("(GZIPChannel) gZIPChannel.incomingFilters.size()", 1, ((GZIPChannel) gZIPChannel).incomingFilters.size());
    }

    @Test
    public void testAddFilter3() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.addFilter(new DelayFilter(), 2);
        assertEquals("(GZIPChannel) gZIPChannel.outgoingFilters.size()", 1, ((GZIPChannel) gZIPChannel).outgoingFilters.size());
    }

    @Test
    public void testAddFilter4() throws Throwable {
        BaseChannel bASE24Channel = new BASE24Channel("testBaseChannelHost", 100, new ISO87APackagerBBitmap());
        bASE24Channel.addFilter(new MD5Filter(), 0);
        assertEquals("(BASE24Channel) bASE24Channel.incomingFilters.size()", 1,
                ((BASE24Channel) bASE24Channel).incomingFilters.size());
        assertEquals("(BASE24Channel) bASE24Channel.outgoingFilters.size()", 1,
                ((BASE24Channel) bASE24Channel).outgoingFilters.size());
    }

    @Test
    public void testAddIncomingFilter() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.addIncomingFilter(new MD5Filter());
        assertEquals("(X25Channel) x25Channel.incomingFilters.size()", 1, ((X25Channel) x25Channel).incomingFilters.size());
    }

    @Test
    public void testAddOutgoingFilter() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.addOutgoingFilter(new StatefulFilter());
        assertEquals("(X25Channel) x25Channel.outgoingFilters.size()", 1, ((X25Channel) x25Channel).outgoingFilters.size());
    }

    @Test
    public void testApplyIncomingFilters() throws Throwable {
        byte[] image = "testString".getBytes();
        final BaseChannel cSChannel = new CSChannel();
        cSChannel.addIncomingFilter(filter);
        final LogEvent evt = new LogEvent();
        byte[] header = new byte[2];
        when(filter.filter(cSChannel, m, evt)).thenReturn(m);
        ISOMsg result = cSChannel.applyIncomingFilters(m, header, image, evt);
        assertSame("result", m, result);
    }

    @Test
    public void testApplyIncomingFilters1() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel("testBaseChannelHost", 100, new ISOBaseValidatingPackager());
        LogEvent evt = new LogEvent();
        byte[] header = new byte[0];
        byte[] image = new byte[2];
        ISOMsg result = gZIPChannel.applyIncomingFilters(m, header, image, evt);
        assertSame("result", m, result);
    }

    @Test
    public void testApplyIncomingFilters2() throws Throwable {
        BaseChannel x25Channel = new X25Channel(null, new ServerSocket());
        byte[] header = new byte[1];
        byte[] image = new byte[1];
        ISOMsg result = x25Channel.applyIncomingFilters(null, header, image, new LogEvent("testBaseChannelTag"));
        assertNull("result", result);
    }

    @Test
    public void testApplyIncomingFilters3() throws Throwable {
        BaseChannel cSChannel = new CSChannel();
        ISOMsg result = cSChannel.applyIncomingFilters(null, null);
        assertNull("result", result);
    }

    @Test
    public void testApplyIncomingFiltersThrowsNullPointerException() throws Throwable {
        BaseChannel cSChannel = new CSChannel();
        cSChannel.addIncomingFilter(new MacroFilter());
        byte[] header = new byte[2];
        try {
            cSChannel.applyIncomingFilters(null, header, "testString".getBytes(), new LogEvent());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testApplyIncomingFiltersThrowsVetoException() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.addFilter(new MD5Filter());
        try {
            x25Channel.applyIncomingFilters(new ISOMsg(), new LogEvent(new CTCSubFieldPackager(), "testBaseChannelTag"));
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("ex.getMessage()", "MD5Filter not configured", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testApplyOutgoingFilters() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        LogEvent evt = new LogEvent(new CTCSubFieldPackager(), "testBaseChannelTag");
        ISOMsg result = x25Channel.applyOutgoingFilters(m, evt);
        assertSame("result", m, result);
    }

    @Test
    public void testApplyOutgoingFilters1() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        ISOMsg result = x25Channel.applyOutgoingFilters(null, new LogEvent(new CTCSubFieldPackager(), "testBaseChannelTag"));
        assertNull("result", result);
    }

    @Test
    public void testApplyOutgoingFiltersThrowsVetoException() throws Throwable {
        BaseChannel bASE24Channel = new BASE24Channel("testBaseChannelHost", 100, new ISO87APackagerBBitmap());
        bASE24Channel.addFilter(new MD5Filter(), 0);
        try {
            bASE24Channel.applyOutgoingFilters(new ISOMsg(), new LogEvent());
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("ex.getMessage()", "MD5Filter not configured", ex.getMessage());
            assertNull("ex.nested", ex.nested);
        }
    }

    @Test
    public void testApplyTimeout() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.applyTimeout();
        assertNull("(GZIPChannel) gZIPChannel.getSocket()", gZIPChannel.getSocket());
    }

    @Test
    public void testConnectThrowsNullPointerException() throws Throwable {
        BaseChannel bASE24TCPChannel = new BASE24TCPChannel();
        Socket socket = new Socket();
        bASE24TCPChannel.setTimeout(-1);
        try {
            bASE24TCPChannel.connect(socket);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(BASE24TCPChannel) bASE24TCPChannel.serverIn", ((BASE24TCPChannel) bASE24TCPChannel).serverIn);
            assertNull("(BASE24TCPChannel) bASE24TCPChannel.serverOut", ((BASE24TCPChannel) bASE24TCPChannel).serverOut);
            assertSame("(BASE24TCPChannel) bASE24TCPChannel.getSocket()", socket, bASE24TCPChannel.getSocket());
            assertEquals("(BASE24TCPChannel) bASE24TCPChannel.cnt.length", 3, ((BASE24TCPChannel) bASE24TCPChannel).cnt.length);
            assertFalse("(BASE24TCPChannel) bASE24TCPChannel.usable", ((BASE24TCPChannel) bASE24TCPChannel).usable);
            assertNull("(BASE24TCPChannel) bASE24TCPChannel.logger", ((BASE24TCPChannel) bASE24TCPChannel).logger);
            assertNull("(BASE24TCPChannel) bASE24TCPChannel.originalRealm", ((BASE24TCPChannel) bASE24TCPChannel).originalRealm);
            assertNull("(BASE24TCPChannel) bASE24TCPChannel.realm", ((BASE24TCPChannel) bASE24TCPChannel).realm);
            assertNull("socket.getChannel()", socket.getChannel());
        }
    }

    @Test
    public void testConnectThrowsNullPointerException1() throws Throwable {
        Socket socket = new Socket(Proxy.NO_PROXY);
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.setTimeout(1);
        try {
            gZIPChannel.connect(socket);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(GZIPChannel) gZIPChannel.serverIn", ((GZIPChannel) gZIPChannel).serverIn);
            assertNull("(GZIPChannel) gZIPChannel.serverOut", ((GZIPChannel) gZIPChannel).serverOut);
            assertSame("(GZIPChannel) gZIPChannel.getSocket()", socket, gZIPChannel.getSocket());
            assertEquals("(GZIPChannel) gZIPChannel.cnt.length", 3, ((GZIPChannel) gZIPChannel).cnt.length);
            assertFalse("(GZIPChannel) gZIPChannel.usable", ((GZIPChannel) gZIPChannel).usable);
            assertNull("(GZIPChannel) gZIPChannel.logger", ((GZIPChannel) gZIPChannel).logger);
            assertNull("(GZIPChannel) gZIPChannel.originalRealm", ((GZIPChannel) gZIPChannel).originalRealm);
            assertNull("(GZIPChannel) gZIPChannel.realm", ((GZIPChannel) gZIPChannel).realm);
            assertNull("socket.getChannel()", socket.getChannel());
        }
    }

    @Test
    public void testCreateISOMsg() throws Throwable {
        ISOPackager p = new GenericPackager();
        BaseChannel pADChannel = new PADChannel(p);
        ISOMsg result = pADChannel.createMsg();
        assertEquals("result.getDirection()", 0, result.getDirection());
        assertSame("(PADChannel) pADChannel.packager", p, ((PADChannel) pADChannel).packager);
    }

    @Test
    public void testCreateISOMsgThrowsNullPointerException() throws Throwable {
        BaseChannel cSChannel = new CSChannel(new ISO93BPackager());
        cSChannel.setPackager(null);
        try {
            cSChannel.createMsg();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(CSChannel) cSChannel.packager", ((CSChannel) cSChannel).packager);
        }
    }

    @Test
    public void testCreateMsg() throws Throwable {
        ISOPackager p = new ISO87APackagerBBitmap();
        BaseChannel rawChannel = new RawChannel();
        rawChannel.setPackager(p);
        ISOMsg result = rawChannel.createMsg();
        assertEquals("result.getDirection()", 0, result.getDirection());
        assertSame("(RawChannel) rawChannel.packager", p, ((RawChannel) rawChannel).packager);
    }

    @Test
    public void testCreateMsgThrowsNullPointerException() throws Throwable {
        BaseChannel nACChannel = new NACChannel();
        try {
            nACChannel.createMsg();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(NACChannel) nACChannel.packager", ((NACChannel) nACChannel).packager);
        }
    }

    @Test
    public void testDisconnect() throws Throwable {
        BaseChannel rawChannel = new RawChannel();
        rawChannel.disconnect();
        assertNull("(RawChannel) rawChannel.getSocket()", rawChannel.getSocket());
        assertFalse("(RawChannel) rawChannel.usable", ((RawChannel) rawChannel).usable);
    }

    @Test
    public void testDisconnect1() throws Throwable {
        ServerSocket sock = new ServerSocket();
        BaseChannel cSChannel = new CSChannel(new PostPackager());
        cSChannel.setServerSocket(sock);
        cSChannel.disconnect();
        assertSame("(CSChannel) cSChannel.serverSocket", sock, ((CSChannel) cSChannel).serverSocket);
        assertNull("(CSChannel) cSChannel.getSocket()", cSChannel.getSocket());
        assertFalse("(CSChannel) cSChannel.usable", ((CSChannel) cSChannel).usable);
    }

    @Test
    public void testGetBytesThrowsNullPointerException() throws Throwable {
        BaseChannel rawChannel = new RawChannel(new ISO87APackagerBBitmap(), "".getBytes());
        byte[] b = new byte[1];
        try {
            rawChannel.getBytes(b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(RawChannel) rawChannel.serverIn", ((RawChannel) rawChannel).serverIn);
        }
    }

    @Test
    public void testGetChannel() throws Throwable {
        BaseChannel aSCIIChannel = new ASCIIChannel(new GenericPackager(), new ServerSocket());
        aSCIIChannel.setName("channel.");
        BaseChannel result = (BaseChannel) BaseChannel.getChannel("channel.");
        assertSame("result", aSCIIChannel, result);
    }

    @Test
    public void testGetChannelThrowsNotFoundException() throws Throwable {
        try {
            BaseChannel.getChannel("testString");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertTrue("Test completed without Exception", true);
        }
    }

    @Test
    public void testGetCounters() throws Throwable {
        int[] result = new NACChannel().getCounters();
        assertEquals("result.length", 3, result.length);
        assertEquals("result[0]", 0, result[0]);
    }

    @Test
    public void testGetDynamicHeader() throws Throwable {
        BaseChannel rawChannel = new RawChannel();
        byte[] image = new byte[2];
        BaseHeader result = (BaseHeader) rawChannel.getDynamicHeader(image);
        assertEquals("result.getLength()", 2, result.getLength());
    }

    @Test
    public void testGetDynamicHeader1() throws Throwable {
        BaseChannel bASE24Channel = new BASE24Channel("testBaseChannelHost", 100, new ISO87APackagerBBitmap());
        ISOHeader result = bASE24Channel.getDynamicHeader((byte[]) null);
        assertNull("result", result);
    }

    @Test
    public void testGetDynamicPackager() throws Throwable {
        ISOPackager p = new PostPackager();
        BaseChannel xMLChannel = new XMLChannel();
        xMLChannel.setPackager(p);
        ISOPackager result = xMLChannel.getDynamicPackager(new ISOMsg());
        assertSame("result", p, result);
    }

    @Test
    public void testGetDynamicPackager1() throws Throwable {
        BaseChannel nACChannel = new NACChannel();
        ISOPackager p = new ISO87APackagerBBitmap();
        nACChannel.setPackager(p);
        byte[] image = new byte[1];
        ISOPackager result = nACChannel.getDynamicPackager(image);
        assertSame("result", p, result);
    }

    @Test
    public void testGetHeader() throws Throwable {
        byte[] header = new byte[0];
        BaseChannel bASE24Channel = new BASE24Channel(new ISO87APackager(), new ServerSocket());
        bASE24Channel.setHeader(header);
        byte[] result = bASE24Channel.getHeader();
        assertSame("result", header, result);
    }

    @Test
    public void testGetHeaderLength() throws Throwable {
        BaseChannel hEXChannel = new HEXChannel(new ISO87APackagerBBitmap(), "".getBytes(), new ServerSocket());
        byte[] b = new byte[3];
        int result = hEXChannel.getHeaderLength(b);
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetHeaderLength1() throws Throwable {
        BaseChannel logChannel = new LogChannel("testBaseChannelHost", 100, new GenericValidatingPackager());
        ISOMsg iSOVMsg = new ISOMsg();
        iSOVMsg.setHeader(new BaseHeader());
        int result = logChannel.getHeaderLength((ISOMsg) iSOVMsg.clone());
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetHeaderLength2() throws Throwable {
        BaseChannel xMLChannel = new XMLChannel();
        final byte[] bytes = new byte[12];
        bytes[0] = (byte) 22;
        bytes[1] = (byte) 1;
        bytes[2] = (byte) 2;
        bytes[3] = (byte) 0;
        bytes[4] = (byte) 0;
        bytes[5] = (byte) 0;
        bytes[6] = (byte) 0;
        bytes[7] = (byte) 0;
        bytes[8] = (byte) 0;
        bytes[9] = (byte) 0;
        bytes[10] = (byte) 0;
        bytes[11] = (byte) 0;
        when(m.getHeader()).thenReturn(bytes);
        int result = xMLChannel.getHeaderLength(m);
        assertEquals("result", 12, result);
    }

    @Test
    public void testGetHeaderLength3() throws Throwable {
        BaseChannel rawChannel = new RawChannel();
        int result = rawChannel.getHeaderLength();
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetHeaderLength4() throws Throwable {
        byte[] header = new byte[1];
        BaseChannel gZIPChannel = new GZIPChannel("testBaseChannelHost", 100, new ISOBaseValidatingPackager());
        gZIPChannel.setHeader(header);
        int result = gZIPChannel.getHeaderLength();
        assertEquals("result", 1, result);
    }

    @Test
    public void testGetHeaderLengthThrowsNullPointerException() throws Throwable {
        BaseChannel logChannel = new LogChannel("testBaseChannelHost", 100, new GenericValidatingPackager());
        try {
            logChannel.getHeaderLength((ISOMsg) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetIncomingFilters() throws Throwable {
        Vector result = (Vector) new CSChannel(new PostPackager()).getIncomingFilters();
        assertEquals("result.size()", 0, result.size());
    }

    @Test
    public void testGetMessageThrowsNullPointerException() throws Throwable {
        BaseChannel rawChannel = new RawChannel();
        byte[] b = new byte[3];
        try {
            rawChannel.getMessage(b, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(RawChannel) rawChannel.serverIn", ((RawChannel) rawChannel).serverIn);
        }
    }

    @Test
    public void testGetOriginalRealm() throws Throwable {
        BaseChannel rawChannel = new RawChannel(new ISO87APackagerBBitmap(), "".getBytes());
        rawChannel.setLogger(null, "testBaseChannelRealm");
        String result = rawChannel.getOriginalRealm();
        assertEquals("result", "testBaseChannelRealm", result);
    }

    @Test
    public void testGetOutgoingFilters() throws Throwable {
        Vector result = (Vector) new GZIPChannel().getOutgoingFilters();
        assertEquals("result.size()", 0, result.size());
    }

    @Test
    public void testGetPackager() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        ISOPackager p = new ISO87APackagerBBitmap();
        x25Channel.setPackager(p);
        ISOPackager result = x25Channel.getPackager();
        assertSame("result", p, result);
    }

    @Test
    public void testGetServerSocket() throws Throwable {
        ServerSocket sock = new ServerSocket();
        BaseChannel rawChannel = new RawChannel();
        rawChannel.setServerSocket(sock);
        ServerSocket result = rawChannel.getServerSocket();
        assertSame("result", sock, result);
    }

    @Test
    public void testGetSocket() throws Throwable {
        Socket result = new RawChannel().getSocket();
        assertNull("result", result);
    }

    @Test
    public void testGetSocketFactory() throws Throwable {
        ISOClientSocketFactory socketFactory = new SunJSSESocketFactory();
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.setSocketFactory(socketFactory);
        ISOClientSocketFactory result = gZIPChannel.getSocketFactory();
        assertSame("result", socketFactory, result);
    }

    @Test
    public void testIsConnected() throws Throwable {
        boolean result = new XMLChannel(new ISO87APackager()).isConnected();
        assertFalse("result", result);
    }

    @Test
    public void testIsOverrideHeader() throws Throwable {
        boolean result = new PADChannel(new GenericPackager()).isOverrideHeader();
        assertFalse("result", result);
    }

    @Test
    public void testIsOverrideHeader1() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setOverrideHeader(true);
        boolean result = x25Channel.isOverrideHeader();
        assertTrue("result", result);
    }

    @Test
    public void testIsRejected() throws Throwable {
        BaseChannel aSCIIChannel = new ASCIIChannel();
        byte[] b = new byte[3];
        boolean result = aSCIIChannel.isRejected(b);
        assertFalse("result", result);
    }

    @Test
    public void testNewSocketThrowsIllegalArgumentException() throws Throwable {
        byte[] TPDU = new byte[0];
        BaseChannel hEXChannel = new HEXChannel("testBaseChannelHost", -1, new GenericPackager(), TPDU);
        try {
            hEXChannel.newSocket("test", -1);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "port out of range:-1", ex.getMessage());
            assertNull("(HEXChannel) hEXChannel.socketFactory", ((HEXChannel) hEXChannel).socketFactory);
        }
    }

    @Test
    public void testReadHeaderThrowsNegativeArraySizeException() throws Throwable {
        BaseChannel nACChannel = new NACChannel();
        try {
            nACChannel.readHeader(-1);
            fail("Expected NegativeArraySizeException to be thrown");
        } catch (NegativeArraySizeException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(NACChannel) nACChannel.serverIn", ((NACChannel) nACChannel).serverIn);
        }
    }

    @Test
    public void testReadHeaderThrowsNullPointerException() throws Throwable {
        BaseChannel aSCIIChannel = new ASCIIChannel();
        try {
            aSCIIChannel.readHeader(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(ASCIIChannel) aSCIIChannel.serverIn", ((ASCIIChannel) aSCIIChannel).serverIn);
        }
    }

    @Test
    public void testReceiveThrowsNullPointerException() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        try {
            x25Channel.receive();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(X25Channel) x25Channel.packager", ((X25Channel) x25Channel).packager);
            assertNull("(X25Channel) x25Channel.serverIn", ((X25Channel) x25Channel).serverIn);
            assertNull("(X25Channel) x25Channel.getSocket()", x25Channel.getSocket());
            assertEquals("(X25Channel) x25Channel.cnt.length", 3, ((X25Channel) x25Channel).cnt.length);
        }
    }

    @Test
    public void testRemoveFilter() throws Throwable {
        BaseChannel bASE24TCPChannel = new BASE24TCPChannel();
        bASE24TCPChannel.removeFilter(new StatefulFilter(), 100);
        assertEquals("(BASE24TCPChannel) bASE24TCPChannel.incomingFilters.size()", 0,
                ((BASE24TCPChannel) bASE24TCPChannel).incomingFilters.size());
        assertEquals("(BASE24TCPChannel) bASE24TCPChannel.outgoingFilters.size()", 0,
                ((BASE24TCPChannel) bASE24TCPChannel).outgoingFilters.size());
    }

    @Test
    public void testRemoveFilter1() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.removeFilter(new BSHFilter(), 1);
        assertEquals("(GZIPChannel) gZIPChannel.incomingFilters.size()", 0, ((GZIPChannel) gZIPChannel).incomingFilters.size());
    }

    @Test
    public void testRemoveFilter2() throws Throwable {
        BaseChannel bASE24TCPChannel = new BASE24TCPChannel();
        bASE24TCPChannel.removeFilter(new DelayFilter(), 0);
        assertEquals("(BASE24TCPChannel) bASE24TCPChannel.incomingFilters.size()", 0,
                ((BASE24TCPChannel) bASE24TCPChannel).incomingFilters.size());
        assertEquals("(BASE24TCPChannel) bASE24TCPChannel.outgoingFilters.size()", 0,
                ((BASE24TCPChannel) bASE24TCPChannel).outgoingFilters.size());
    }

    @Test
    public void testRemoveFilter3() throws Throwable {
        BaseChannel pADChannel = new PADChannel("testBaseChannelHost", 100, null);
        pADChannel.removeFilter(new MD5Filter(), 2);
        assertEquals("(PADChannel) pADChannel.outgoingFilters.size()", 0, ((PADChannel) pADChannel).outgoingFilters.size());
    }

    @Test
    public void testRemoveFilter4() throws Throwable {
        BaseChannel nACChannel = new NACChannel();
        nACChannel.removeFilter(new DelayFilter());
        assertEquals("(NACChannel) nACChannel.incomingFilters.size()", 0, ((NACChannel) nACChannel).incomingFilters.size());
        assertEquals("(NACChannel) nACChannel.outgoingFilters.size()", 0, ((NACChannel) nACChannel).outgoingFilters.size());
    }

    @Test
    public void testRemoveIncomingFilter() throws Throwable {
        BaseChannel bASE24Channel = new BASE24Channel("testBaseChannelHost", 100, new ISO87APackagerBBitmap());
        bASE24Channel.removeIncomingFilter(new XSLTFilter());
        assertEquals("(BASE24Channel) bASE24Channel.incomingFilters.size()", 0,
                ((BASE24Channel) bASE24Channel).incomingFilters.size());
    }

    @Test
    public void testRemoveOutgoingFilter() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.removeOutgoingFilter(new StatefulFilter());
        assertEquals("(X25Channel) x25Channel.outgoingFilters.size()", 0, ((X25Channel) x25Channel).outgoingFilters.size());
    }

    @Test
    public void testResetCounters() throws Throwable {
        BaseChannel x25Channel = new X25Channel(new GenericValidatingPackager());
        x25Channel.resetCounters();
        assertEquals("(X25Channel) x25Channel.cnt.length", 3, ((X25Channel) x25Channel).cnt.length);
    }

    @Test
    public void testSendKeepAliveThrowsNullPointerException() throws Throwable {
        try {
            new X25Channel("testBaseChannelHost", 100, new XMLPackager()).sendKeepAlive();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendKeepAliveThrowsNullPointerException1() throws Throwable {
        try {
            new GZIPChannel().sendKeepAlive();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageHeader() throws Throwable {
        ISOMsg m = new ISOMsg();
        BaseChannel gZIPChannel = new GZIPChannel(new GenericValidatingPackager());
        gZIPChannel.sendMessageHeader(m, 100);
        assertNull("(GZIPChannel) gZIPChannel.serverOut", ((GZIPChannel) gZIPChannel).serverOut);
        assertEquals("m.getDirection()", 0, m.getDirection());
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException() throws Throwable {
        BaseChannel logChannel = new LogChannel();
        byte[] header = new byte[1];
        logChannel.setOverrideHeader(true);
        logChannel.setHeader(header);
        ISOMsg m = new ISOMsg();
        try {
            logChannel.sendMessageHeader(m, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(LogChannel) logChannel.serverOut", ((LogChannel) logChannel).serverOut);
            assertEquals("m.getDirection()", 0, m.getDirection());
        }
    }

    @Test
    public void testSendMessageHeaderThrowsNullPointerException1() throws Throwable {
        BaseChannel bASE24Channel = new BASE24Channel("testBaseChannelHost", 100, new ISO87APackagerBBitmap());
        try {
            bASE24Channel.sendMessageHeader(null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(BASE24Channel) bASE24Channel.serverOut", ((BASE24Channel) bASE24Channel).serverOut);
        }
    }

    @Test
    public void testSendMessageLength() throws Throwable {
        BaseChannel xMLChannel = new XMLChannel();
        xMLChannel.sendMessageLength(100);
        int actual = xMLChannel.getHeaderLength();
        assertEquals("(XMLChannel) xMLChannel.getHeaderLength()", 0, actual);
    }

    @Test
    public void testSendMessageThrowsNullPointerException() throws Throwable {
        BaseChannel logChannel = new LogChannel();
        byte[] b = new byte[3];
        try {
            logChannel.sendMessage(b, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(LogChannel) logChannel.serverOut", ((LogChannel) logChannel).serverOut);
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSendMessageTrailler() throws Throwable {
        BaseChannel xMLChannel = new XMLChannel(new PostPackager());
        xMLChannel.sendMessageTrailler(new ISOMsg(), 100);
        int actual = xMLChannel.getHeaderLength();
        assertEquals("(XMLChannel) xMLChannel.getHeaderLength()", 0, actual);
    }

    @Test
    public void testSendMessageTrailler1() throws Throwable {
        BaseChannel rawChannel = new RawChannel();
        rawChannel.sendMessageTrailler(new ISOMsg(), "testString".getBytes());
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testSendMessageTraillerThrowsNullPointerException() throws Throwable {
        byte[] b = new byte[3];
        BaseChannel bASE24TCPChannel = new BASE24TCPChannel();
        try {
            bASE24TCPChannel.sendMessageTrailler(new ISOMsg(), b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel(new ISO93BPackager(), null);
        gZIPChannel.setSocketFactory(new SunJSSESocketFactory());
        gZIPChannel.setConfiguration(new SimpleConfiguration());
        assertEquals("(GZIPChannel) gZIPChannel.getTimeout()", 0, gZIPChannel.getTimeout());
        assertEquals("(GZIPChannel) gZIPChannel.getMaxPacketLength()", 100000, gZIPChannel.getMaxPacketLength());
        assertFalse("(GZIPChannel) gZIPChannel.overrideHeader", ((GZIPChannel) gZIPChannel).overrideHeader);
    }

    @Test
    public void testSetConfiguration1() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.setConfiguration(new SimpleConfiguration());
        assertEquals("(GZIPChannel) gZIPChannel.getTimeout()", 0, gZIPChannel.getTimeout());
        assertEquals("(GZIPChannel) gZIPChannel.getMaxPacketLength()", 100000, gZIPChannel.getMaxPacketLength());
        assertFalse("(GZIPChannel) gZIPChannel.overrideHeader", ((GZIPChannel) gZIPChannel).overrideHeader);
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        Configuration cfg = new SimpleConfiguration((Properties) null);
        try {
            x25Channel.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(X25Channel) x25Channel.getMaxPacketLength()", 100000, x25Channel.getMaxPacketLength());
            assertEquals("(X25Channel) x25Channel.getPort()", 0, x25Channel.getPort());
            assertEquals("(X25Channel) x25Channel.getTimeout()", 0, x25Channel.getTimeout());
            assertNull("(X25Channel) x25Channel.getHost()", x25Channel.getHost());
            assertNull("(X25Channel) x25Channel.getSocket()", x25Channel.getSocket());
            assertFalse("(X25Channel) x25Channel.overrideHeader", ((X25Channel) x25Channel).overrideHeader);
        }
    }

    @Test
    public void testSetHeader() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        byte[] header = new byte[0];
        gZIPChannel.setHeader(header);
        assertSame("(GZIPChannel) gZIPChannel.header", header, ((GZIPChannel) gZIPChannel).header);
    }

    @Test
    public void testSetHeader1() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.setHeader("testBaseChannelHeader");
        assertEquals("(GZIPChannel) gZIPChannel.header.length", 21, ((GZIPChannel) gZIPChannel).header.length);
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        BaseChannel cSChannel = new CSChannel(new PostPackager());
        try {
            cSChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(CSChannel) cSChannel.header", ((CSChannel) cSChannel).header);
        }
    }

    @Test
    public void testSetHost() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setHost("testBaseChannelHost", 100);
        assertEquals("(X25Channel) x25Channel.getPort()", 100, x25Channel.getPort());
        assertEquals("(X25Channel) x25Channel.getHost()", "testBaseChannelHost", x25Channel.getHost());
    }

    @Test
    public void testSetHost1() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setHost("testBaseChannelHost");
        assertEquals("(X25Channel) x25Channel.getHost()", "testBaseChannelHost", x25Channel.getHost());
    }

    @Test
    public void testSetIncomingFilters() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        gZIPChannel.setIncomingFilters(new ArrayList());
        assertEquals("(GZIPChannel) gZIPChannel.incomingFilters.size()", 0, ((GZIPChannel) gZIPChannel).incomingFilters.size());
    }

    @Test
    public void testSetIncomingFiltersThrowsNullPointerException() throws Throwable {
        byte[] TPDU = new byte[0];
        BaseChannel hEXChannel = new HEXChannel("testBaseChannelHost", 100, new GenericPackager(), TPDU);
        try {
            hEXChannel.setIncomingFilters(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(HEXChannel) hEXChannel.incomingFilters.size()", 0, ((HEXChannel) hEXChannel).incomingFilters.size());
        }
    }

    @Test
    public void testSetLocalAddress() throws Throwable {
        BaseChannel nACChannel = new NACChannel();
        nACChannel.setLocalAddress("testBaseChannelIface", 100);
        // assertEquals("(NACChannel) nACChannel.localIface",
        // "testBaseChannelIface", nACChannel.getSocket()
        // .getLocalAddress());
        // assertEquals("(NACChannel) nACChannel.localPort", 100, nACChannel
        // .getSocket().getLocalAddress());
    }

    @Test
    public void testSetLogger() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        Logger logger = new Logger();
        gZIPChannel.setLogger(logger, "testBaseChannelRealm");
        assertSame("(GZIPChannel) gZIPChannel.logger", logger, ((GZIPChannel) gZIPChannel).logger);
        assertEquals("(GZIPChannel) gZIPChannel.originalRealm", "testBaseChannelRealm", ((GZIPChannel) gZIPChannel).originalRealm);
        assertEquals("(GZIPChannel) gZIPChannel.realm", "testBaseChannelRealm", ((GZIPChannel) gZIPChannel).realm);
    }

    @Test
    public void testSetLogger1() throws Throwable {
        BaseChannel gZIPChannel = new GZIPChannel();
        Logger logger = new Logger();
        gZIPChannel.setLogger(logger, "testBaseChannelRealm");
        gZIPChannel.setLogger(logger, "testBaseChannelRealm");
        assertSame("(GZIPChannel) gZIPChannel.logger", logger, ((GZIPChannel) gZIPChannel).logger);
        assertEquals("(GZIPChannel) gZIPChannel.realm", "testBaseChannelRealm", ((GZIPChannel) gZIPChannel).realm);
    }

    @Test
    public void testSetMaxPacketLength() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setMaxPacketLength(100);
        assertEquals("(X25Channel) x25Channel.getMaxPacketLength()", 100, x25Channel.getMaxPacketLength());
    }

    @Test
    public void testSetName() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setName("testBaseChannelName");
        assertEquals("(X25Channel) x25Channel.getName()", "testBaseChannelName", x25Channel.getName());
    }

    @Test
    public void testSetOutgoingFilters() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setOutgoingFilters(new ArrayList());
        assertEquals("(X25Channel) x25Channel.outgoingFilters.size()", 0, ((X25Channel) x25Channel).outgoingFilters.size());
    }

    @Test
    public void testSetOutgoingFiltersThrowsNullPointerException() throws Throwable {
        BaseChannel cSChannel = new CSChannel("testBaseChannelHost", 100, null);
        try {
            cSChannel.setOutgoingFilters(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(CSChannel) cSChannel.outgoingFilters.size()", 0, ((CSChannel) cSChannel).outgoingFilters.size());
        }
    }

    @Test
    public void testSetOverrideHeader() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setOverrideHeader(true);
        assertTrue("(X25Channel) x25Channel.overrideHeader", ((X25Channel) x25Channel).overrideHeader);
    }

    @Test
    public void testSetPackager() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        ISOPackager p = new ISO87APackagerBBitmap();
        x25Channel.setPackager(p);
        assertSame("(X25Channel) x25Channel.packager", p, ((X25Channel) x25Channel).packager);
    }

    @Test
    public void testSetPort() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setPort(100);
        assertEquals("(X25Channel) x25Channel.getPort()", 100, x25Channel.getPort());
    }

    @Test
    public void testSetServerSocket() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        ServerSocket sock = new ServerSocket();
        x25Channel.setServerSocket(sock);
        assertSame("(X25Channel) x25Channel.serverSocket", sock, ((X25Channel) x25Channel).serverSocket);
        assertEquals("(X25Channel) x25Channel.getPort()", 0, x25Channel.getPort());
        assertNull("(X25Channel) x25Channel.getHost()", x25Channel.getHost());
        assertEquals("(X25Channel) x25Channel.getName()", "", x25Channel.getName());
    }

    @Test
    public void testSetSocketFactory() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        ISOClientSocketFactory socketFactory = new SunJSSESocketFactory();
        x25Channel.setSocketFactory(socketFactory);
        assertSame("(X25Channel) x25Channel.socketFactory", socketFactory, ((X25Channel) x25Channel).socketFactory);
    }

    @Test
    public void testSetTimeout() throws Throwable {
        BaseChannel x25Channel = new X25Channel();
        x25Channel.setTimeout(1);
        assertEquals("(X25Channel) x25Channel.getTimeout()", 1, x25Channel.getTimeout());
    }

    @Test
    public void testSetTimeout1() throws Throwable {
        BaseChannel cSChannel = new CSChannel(new PostPackager());
        cSChannel.setTimeout(0);
        assertEquals("(CSChannel) cSChannel.getTimeout()", 0, cSChannel.getTimeout());
    }

    @Test
    public void testSetTimeout2() throws Throwable {
        BaseChannel nACChannel = new NACChannel();
        nACChannel.setTimeout(-1);
        assertEquals("(NACChannel) nACChannel.getTimeout()", -1, nACChannel.getTimeout());
    }

    @Test
    public void testShouldIgnore() throws Throwable {
        BaseChannel xMLChannel = new XMLChannel();
        byte[] b = new byte[0];
        boolean result = xMLChannel.shouldIgnore(b);
        assertFalse("result", result);
    }

    @Test
    public void testStreamReceive() throws Throwable {
        BaseChannel aSCIIChannel = new ASCIIChannel();
        byte[] result = aSCIIChannel.streamReceive();
        assertEquals("result.length", 0, result.length);
    }
}
