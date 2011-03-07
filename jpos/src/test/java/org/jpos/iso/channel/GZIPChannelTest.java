package org.jpos.iso.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.GenericValidatingPackager;
import org.junit.Test;

public class GZIPChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new CTCSubFieldPackager();
        ServerSocket serverSocket = new ServerSocket();
        GZIPChannel gZIPChannel = new GZIPChannel(p, serverSocket);
        assertEquals("gZIPChannel.getIncomingFilters().size()", 0, gZIPChannel.getIncomingFilters().size());
        assertEquals("gZIPChannel.getMaxPacketLength()", 100000, gZIPChannel.getMaxPacketLength());
        assertSame("gZIPChannel.getPackager()", p, gZIPChannel.getPackager());
        assertEquals("gZIPChannel.getPort()", 0, gZIPChannel.getPort());
        assertEquals("gZIPChannel.getName()", "", gZIPChannel.getName());
        assertEquals("gZIPChannel.getCounters().length", 3, gZIPChannel.getCounters().length);
        assertNull("gZIPChannel.getLogger()", gZIPChannel.getLogger());
        assertNull("gZIPChannel.getSocketFactory()", gZIPChannel.getSocketFactory());
        assertNull("gZIPChannel.getHeader()", gZIPChannel.getHeader());
        assertEquals("gZIPChannel.getOutgoingFilters().size()", 0, gZIPChannel.getOutgoingFilters().size());
        assertSame("gZIPChannel.getServerSocket()", serverSocket, gZIPChannel.getServerSocket());
        assertEquals("gZIPChannel.getOriginalRealm()", "org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm());
        assertNull("gZIPChannel.getRealm()", gZIPChannel.getRealm());
        assertNull("gZIPChannel.getHost()", gZIPChannel.getHost());
    }

    @Test
    public void testConstructor1() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel();
        assertEquals("gZIPChannel.getIncomingFilters().size()", 0, gZIPChannel.getIncomingFilters().size());
        assertEquals("gZIPChannel.getMaxPacketLength()", 100000, gZIPChannel.getMaxPacketLength());
        assertEquals("gZIPChannel.getPort()", 0, gZIPChannel.getPort());
        assertEquals("gZIPChannel.getName()", "", gZIPChannel.getName());
        assertEquals("gZIPChannel.getCounters().length", 3, gZIPChannel.getCounters().length);
        assertNull("gZIPChannel.getLogger()", gZIPChannel.getLogger());
        assertNull("gZIPChannel.getSocketFactory()", gZIPChannel.getSocketFactory());
        assertNull("gZIPChannel.getHeader()", gZIPChannel.getHeader());
        assertEquals("gZIPChannel.getOutgoingFilters().size()", 0, gZIPChannel.getOutgoingFilters().size());
        assertNull("gZIPChannel.getServerSocket()", gZIPChannel.getServerSocket());
        assertEquals("gZIPChannel.getOriginalRealm()", "org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm());
        assertNull("gZIPChannel.getRealm()", gZIPChannel.getRealm());
        assertNull("gZIPChannel.getHost()", gZIPChannel.getHost());
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new GenericValidatingPackager();
        GZIPChannel gZIPChannel = new GZIPChannel("testGZIPChannelHost", 100, p);
        assertEquals("gZIPChannel.getIncomingFilters().size()", 0, gZIPChannel.getIncomingFilters().size());
        assertEquals("gZIPChannel.getMaxPacketLength()", 100000, gZIPChannel.getMaxPacketLength());
        assertSame("gZIPChannel.getPackager()", p, gZIPChannel.getPackager());
        assertEquals("gZIPChannel.getPort()", 100, gZIPChannel.getPort());
        assertEquals("gZIPChannel.getName()", "", gZIPChannel.getName());
        assertEquals("gZIPChannel.getCounters().length", 3, gZIPChannel.getCounters().length);
        assertNull("gZIPChannel.getLogger()", gZIPChannel.getLogger());
        assertNull("gZIPChannel.getSocketFactory()", gZIPChannel.getSocketFactory());
        assertNull("gZIPChannel.getHeader()", gZIPChannel.getHeader());
        assertEquals("gZIPChannel.getOutgoingFilters().size()", 0, gZIPChannel.getOutgoingFilters().size());
        assertNull("gZIPChannel.getServerSocket()", gZIPChannel.getServerSocket());
        assertEquals("gZIPChannel.getOriginalRealm()", "org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm());
        assertNull("gZIPChannel.getRealm()", gZIPChannel.getRealm());
        assertEquals("gZIPChannel.getHost()", "testGZIPChannelHost", gZIPChannel.getHost());
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new GenericPackager();
        GZIPChannel gZIPChannel = new GZIPChannel(p);
        assertEquals("gZIPChannel.getIncomingFilters().size()", 0, gZIPChannel.getIncomingFilters().size());
        assertEquals("gZIPChannel.getMaxPacketLength()", 100000, gZIPChannel.getMaxPacketLength());
        assertSame("gZIPChannel.getPackager()", p, gZIPChannel.getPackager());
        assertEquals("gZIPChannel.getPort()", 0, gZIPChannel.getPort());
        assertEquals("gZIPChannel.getName()", "", gZIPChannel.getName());
        assertEquals("gZIPChannel.getCounters().length", 3, gZIPChannel.getCounters().length);
        assertNull("gZIPChannel.getLogger()", gZIPChannel.getLogger());
        assertNull("gZIPChannel.getSocketFactory()", gZIPChannel.getSocketFactory());
        assertNull("gZIPChannel.getHeader()", gZIPChannel.getHeader());
        assertEquals("gZIPChannel.getOutgoingFilters().size()", 0, gZIPChannel.getOutgoingFilters().size());
        assertNull("gZIPChannel.getServerSocket()", gZIPChannel.getServerSocket());
        assertEquals("gZIPChannel.getOriginalRealm()", "org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm());
        assertNull("gZIPChannel.getRealm()", gZIPChannel.getRealm());
        assertNull("gZIPChannel.getHost()", gZIPChannel.getHost());
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel();
        try {
            gZIPChannel.getMessageLength();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetMessageThrowsNullPointerException() throws Throwable {
        byte[] b = new byte[2];
        GZIPChannel gZIPChannel = new GZIPChannel();
        try {
            gZIPChannel.getMessage(b, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel("testGZIPChannelHost", 100, new Base1Packager());
        try {
            gZIPChannel.sendMessageLength(100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSendMessageThrowsNullPointerException() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel("testGZIPChannelHost", 100, new CTCSubFieldPackager());
        byte[] b = new byte[3];
        try {
            gZIPChannel.sendMessage(b, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
