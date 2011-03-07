package org.jpos.iso.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO93BPackager;
import org.jpos.iso.packager.ISOBaseValidatingPackager;
import org.junit.Test;

public class PADChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new ISOBaseValidatingPackager();
        PADChannel pADChannel = new PADChannel("testPADChannelHost", 100, p);
        assertEquals("pADChannel.getIncomingFilters().size()", 0, pADChannel.getIncomingFilters().size());
        assertEquals("pADChannel.getMaxPacketLength()", 100000, pADChannel.getMaxPacketLength());
        assertSame("pADChannel.getPackager()", p, pADChannel.getPackager());
        assertEquals("pADChannel.getPort()", 100, pADChannel.getPort());
        assertEquals("pADChannel.getName()", "", pADChannel.getName());
        assertEquals("pADChannel.getCounters().length", 3, pADChannel.getCounters().length);
        assertNull("pADChannel.getLogger()", pADChannel.getLogger());
        assertNull("pADChannel.getSocketFactory()", pADChannel.getSocketFactory());
        assertNull("pADChannel.getHeader()", pADChannel.getHeader());
        assertEquals("pADChannel.getOutgoingFilters().size()", 0, pADChannel.getOutgoingFilters().size());
        assertNull("pADChannel.getServerSocket()", pADChannel.getServerSocket());
        assertEquals("pADChannel.getOriginalRealm()", "org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm());
        assertNull("pADChannel.getRealm()", pADChannel.getRealm());
        assertEquals("pADChannel.getHost()", "testPADChannelHost", pADChannel.getHost());
        assertNull("pADChannel.reader", pADChannel.reader);
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOPackager p = new ISO93BPackager();
        ServerSocket serverSocket = new ServerSocket();
        PADChannel pADChannel = new PADChannel(p, serverSocket);
        assertEquals("pADChannel.getIncomingFilters().size()", 0, pADChannel.getIncomingFilters().size());
        assertEquals("pADChannel.getMaxPacketLength()", 100000, pADChannel.getMaxPacketLength());
        assertSame("pADChannel.getPackager()", p, pADChannel.getPackager());
        assertEquals("pADChannel.getPort()", 0, pADChannel.getPort());
        assertEquals("pADChannel.getName()", "", pADChannel.getName());
        assertEquals("pADChannel.getCounters().length", 3, pADChannel.getCounters().length);
        assertNull("pADChannel.getLogger()", pADChannel.getLogger());
        assertNull("pADChannel.getSocketFactory()", pADChannel.getSocketFactory());
        assertNull("pADChannel.getHeader()", pADChannel.getHeader());
        assertEquals("pADChannel.getOutgoingFilters().size()", 0, pADChannel.getOutgoingFilters().size());
        assertSame("pADChannel.getServerSocket()", serverSocket, pADChannel.getServerSocket());
        assertEquals("pADChannel.getOriginalRealm()", "org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm());
        assertNull("pADChannel.getRealm()", pADChannel.getRealm());
        assertNull("pADChannel.getHost()", pADChannel.getHost());
        assertNull("pADChannel.reader", pADChannel.reader);
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new ISO93BPackager();
        PADChannel pADChannel = new PADChannel(p);
        assertEquals("pADChannel.getIncomingFilters().size()", 0, pADChannel.getIncomingFilters().size());
        assertEquals("pADChannel.getMaxPacketLength()", 100000, pADChannel.getMaxPacketLength());
        assertSame("pADChannel.getPackager()", p, pADChannel.getPackager());
        assertEquals("pADChannel.getPort()", 0, pADChannel.getPort());
        assertEquals("pADChannel.getName()", "", pADChannel.getName());
        assertEquals("pADChannel.getCounters().length", 3, pADChannel.getCounters().length);
        assertNull("pADChannel.getLogger()", pADChannel.getLogger());
        assertNull("pADChannel.getSocketFactory()", pADChannel.getSocketFactory());
        assertNull("pADChannel.getHeader()", pADChannel.getHeader());
        assertEquals("pADChannel.getOutgoingFilters().size()", 0, pADChannel.getOutgoingFilters().size());
        assertNull("pADChannel.getServerSocket()", pADChannel.getServerSocket());
        assertEquals("pADChannel.getOriginalRealm()", "org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm());
        assertNull("pADChannel.getRealm()", pADChannel.getRealm());
        assertNull("pADChannel.getHost()", pADChannel.getHost());
        assertNull("pADChannel.reader", pADChannel.reader);
    }

    @Test
    public void testConstructor3() throws Throwable {
        PADChannel pADChannel = new PADChannel();
        assertEquals("pADChannel.getIncomingFilters().size()", 0, pADChannel.getIncomingFilters().size());
        assertEquals("pADChannel.getMaxPacketLength()", 100000, pADChannel.getMaxPacketLength());
        assertEquals("pADChannel.getPort()", 0, pADChannel.getPort());
        assertEquals("pADChannel.getName()", "", pADChannel.getName());
        assertEquals("pADChannel.getCounters().length", 3, pADChannel.getCounters().length);
        assertNull("pADChannel.getLogger()", pADChannel.getLogger());
        assertNull("pADChannel.getSocketFactory()", pADChannel.getSocketFactory());
        assertNull("pADChannel.getHeader()", pADChannel.getHeader());
        assertEquals("pADChannel.getOutgoingFilters().size()", 0, pADChannel.getOutgoingFilters().size());
        assertNull("pADChannel.getServerSocket()", pADChannel.getServerSocket());
        assertEquals("pADChannel.getOriginalRealm()", "org.jpos.iso.channel.PADChannel", pADChannel.getOriginalRealm());
        assertNull("pADChannel.getRealm()", pADChannel.getRealm());
        assertNull("pADChannel.getHost()", pADChannel.getHost());
        assertNull("pADChannel.reader", pADChannel.reader);
    }

    @Test
    public void testSetHeader() throws Throwable {
        PADChannel pADChannel = new PADChannel();
        pADChannel.setHeader("testPADChannelHeader");
        assertEquals("pADChannel.getHeader().length", 10, pADChannel.getHeader().length);
    }

    @Test
    public void testSetHeaderThrowsNullPointerException() throws Throwable {
        PADChannel pADChannel = new PADChannel();
        try {
            pADChannel.setHeader((String) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("pADChannel.getHeader()", pADChannel.getHeader());
        }
    }
}
