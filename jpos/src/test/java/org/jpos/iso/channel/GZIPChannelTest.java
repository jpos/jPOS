/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.Base1Packager;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.GenericValidatingPackager;
import org.junit.jupiter.api.Test;

public class GZIPChannelTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager p = new CTCSubFieldPackager();
        ServerSocket serverSocket = new ServerSocket();
        GZIPChannel gZIPChannel = new GZIPChannel(p, serverSocket);
        assertEquals(0, gZIPChannel.getIncomingFilters().size(), "gZIPChannel.getIncomingFilters().size()");
        assertEquals(100000, gZIPChannel.getMaxPacketLength(), "gZIPChannel.getMaxPacketLength()");
        assertSame(p, gZIPChannel.getPackager(), "gZIPChannel.getPackager()");
        assertEquals(0, gZIPChannel.getPort(), "gZIPChannel.getPort()");
        assertEquals("", gZIPChannel.getName(), "gZIPChannel.getName()");
        assertEquals(3, gZIPChannel.getCounters().length, "gZIPChannel.getCounters().length");
        assertNull(gZIPChannel.getLogger(), "gZIPChannel.getLogger()");
        assertNull(gZIPChannel.getSocketFactory(), "gZIPChannel.getSocketFactory()");
        assertNull(gZIPChannel.getHeader(), "gZIPChannel.getHeader()");
        assertEquals(0, gZIPChannel.getOutgoingFilters().size(), "gZIPChannel.getOutgoingFilters().size()");
        assertSame(serverSocket, gZIPChannel.getServerSocket(), "gZIPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm(), "gZIPChannel.getOriginalRealm()");
        assertNull(gZIPChannel.getRealm(), "gZIPChannel.getRealm()");
        assertNull(gZIPChannel.getHost(), "gZIPChannel.getHost()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel();
        assertEquals(0, gZIPChannel.getIncomingFilters().size(), "gZIPChannel.getIncomingFilters().size()");
        assertEquals(100000, gZIPChannel.getMaxPacketLength(), "gZIPChannel.getMaxPacketLength()");
        assertEquals(0, gZIPChannel.getPort(), "gZIPChannel.getPort()");
        assertEquals("", gZIPChannel.getName(), "gZIPChannel.getName()");
        assertEquals(3, gZIPChannel.getCounters().length, "gZIPChannel.getCounters().length");
        assertNull(gZIPChannel.getLogger(), "gZIPChannel.getLogger()");
        assertNull(gZIPChannel.getSocketFactory(), "gZIPChannel.getSocketFactory()");
        assertNull(gZIPChannel.getHeader(), "gZIPChannel.getHeader()");
        assertEquals(0, gZIPChannel.getOutgoingFilters().size(), "gZIPChannel.getOutgoingFilters().size()");
        assertNull(gZIPChannel.getServerSocket(), "gZIPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm(), "gZIPChannel.getOriginalRealm()");
        assertNull(gZIPChannel.getRealm(), "gZIPChannel.getRealm()");
        assertNull(gZIPChannel.getHost(), "gZIPChannel.getHost()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOPackager p = new GenericValidatingPackager();
        GZIPChannel gZIPChannel = new GZIPChannel("testGZIPChannelHost", 100, p);
        assertEquals(0, gZIPChannel.getIncomingFilters().size(), "gZIPChannel.getIncomingFilters().size()");
        assertEquals(100000, gZIPChannel.getMaxPacketLength(), "gZIPChannel.getMaxPacketLength()");
        assertSame(p, gZIPChannel.getPackager(), "gZIPChannel.getPackager()");
        assertEquals(100, gZIPChannel.getPort(), "gZIPChannel.getPort()");
        assertEquals("", gZIPChannel.getName(), "gZIPChannel.getName()");
        assertEquals(3, gZIPChannel.getCounters().length, "gZIPChannel.getCounters().length");
        assertNull(gZIPChannel.getLogger(), "gZIPChannel.getLogger()");
        assertNull(gZIPChannel.getSocketFactory(), "gZIPChannel.getSocketFactory()");
        assertNull(gZIPChannel.getHeader(), "gZIPChannel.getHeader()");
        assertEquals(0, gZIPChannel.getOutgoingFilters().size(), "gZIPChannel.getOutgoingFilters().size()");
        assertNull(gZIPChannel.getServerSocket(), "gZIPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm(), "gZIPChannel.getOriginalRealm()");
        assertNull(gZIPChannel.getRealm(), "gZIPChannel.getRealm()");
        assertEquals("testGZIPChannelHost", gZIPChannel.getHost(), "gZIPChannel.getHost()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        ISOPackager p = new GenericPackager();
        GZIPChannel gZIPChannel = new GZIPChannel(p);
        assertEquals(0, gZIPChannel.getIncomingFilters().size(), "gZIPChannel.getIncomingFilters().size()");
        assertEquals(100000, gZIPChannel.getMaxPacketLength(), "gZIPChannel.getMaxPacketLength()");
        assertSame(p, gZIPChannel.getPackager(), "gZIPChannel.getPackager()");
        assertEquals(0, gZIPChannel.getPort(), "gZIPChannel.getPort()");
        assertEquals("", gZIPChannel.getName(), "gZIPChannel.getName()");
        assertEquals(3, gZIPChannel.getCounters().length, "gZIPChannel.getCounters().length");
        assertNull(gZIPChannel.getLogger(), "gZIPChannel.getLogger()");
        assertNull(gZIPChannel.getSocketFactory(), "gZIPChannel.getSocketFactory()");
        assertNull(gZIPChannel.getHeader(), "gZIPChannel.getHeader()");
        assertEquals(0, gZIPChannel.getOutgoingFilters().size(), "gZIPChannel.getOutgoingFilters().size()");
        assertNull(gZIPChannel.getServerSocket(), "gZIPChannel.getServerSocket()");
        assertEquals("org.jpos.iso.channel.GZIPChannel", gZIPChannel.getOriginalRealm(), "gZIPChannel.getOriginalRealm()");
        assertNull(gZIPChannel.getRealm(), "gZIPChannel.getRealm()");
        assertNull(gZIPChannel.getHost(), "gZIPChannel.getHost()");
    }

    @Test
    public void testGetMessageLengthThrowsNullPointerException() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel();
        try {
            gZIPChannel.getMessageLength();
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
    public void testGetMessageThrowsNullPointerException() throws Throwable {
        byte[] b = new byte[2];
        GZIPChannel gZIPChannel = new GZIPChannel();
        try {
            gZIPChannel.getMessage(b, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSendMessageLengthThrowsNullPointerException() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel("testGZIPChannelHost", 100, new Base1Packager());
        try {
            gZIPChannel.sendMessageLength(100);
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
    public void testSendMessageThrowsNullPointerException() throws Throwable {
        GZIPChannel gZIPChannel = new GZIPChannel("testGZIPChannelHost", 100, new CTCSubFieldPackager());
        byte[] b = new byte[3];
        try {
            gZIPChannel.sendMessage(b, 100, 1000);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testGetMessageReadsCompleteMember() throws Exception {
        byte[] payload = "hello, gzip".getBytes(StandardCharsets.UTF_8);
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(frame(payload.length, payload));

        int length = channel.getMessageLength();
        byte[] received = new byte[length];
        channel.getMessage(received, 0, length);

        assertArrayEquals(payload, received);
        assertFalse(channel.isInputClosed());
    }

    @Test
    public void testGetMessagePreservesNextFrame() throws Exception {
        byte[] first = "first".getBytes(StandardCharsets.UTF_8);
        byte[] second = "second".getBytes(StandardCharsets.UTF_8);
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(concat(frame(first.length, first), frame(second.length, second)));

        assertArrayEquals(first, receive(channel));
        assertArrayEquals(second, receive(channel));
    }

    @Test
    public void testGetMessageReadsFragmentedMemberAndTrailer() throws Exception {
        byte[] payload = "fragmented".getBytes(StandardCharsets.UTF_8);
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(new FragmentedInputStream(new ByteArrayInputStream(frame(payload.length, payload))));

        assertArrayEquals(payload, receive(channel));
    }

    @Test
    public void testGetMessageRejectsInvalidCRC() throws Exception {
        byte[] payload = "crc".getBytes(StandardCharsets.UTF_8);
        byte[] frame = frame(payload.length, payload);
        frame[frame.length - 8] ^= 0x01;
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(frame);

        int length = channel.getMessageLength();
        assertThrows(IOException.class, () -> channel.getMessage(new byte[length], 0, length));
    }

    @Test
    public void testGetMessageRejectsAdvertisedLengthSmallerThanPayload() throws Exception {
        byte[] payload = "three".getBytes(StandardCharsets.UTF_8);
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(frame(payload.length - 1, payload));

        int length = channel.getMessageLength();
        assertThrows(org.jpos.iso.ISOException.class,
          () -> channel.getMessage(new byte[length], 0, length));
    }

    @Test
    public void testGetMessageRejectsAdvertisedLengthLargerThanPayload() throws Exception {
        byte[] payload = "three".getBytes(StandardCharsets.UTF_8);
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(frame(payload.length + 1, payload));

        int length = channel.getMessageLength();
        assertThrows(org.jpos.iso.ISOException.class,
          () -> channel.getMessage(new byte[length], 0, length));
    }

    @Test
    public void testGetMessageReadsMemberWithOptionalHeaderFields() throws Exception {
        byte[] first = "first".getBytes(StandardCharsets.UTF_8);
        byte[] second = "second".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream input = new ByteArrayOutputStream();
        input.write(first.length >> 8);
        input.write(first.length);
        input.write(memberWithOptionalHeaderFields(first, false));
        input.write(frame(second.length, second));
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(input.toByteArray());

        assertArrayEquals(first, receive(channel));
        assertArrayEquals(second, receive(channel));
    }

    @Test
    public void testGetMessageRejectsCorruptHeaderCRC() throws Exception {
        byte[] payload = "header".getBytes(StandardCharsets.UTF_8);
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput(memberWithOptionalHeaderFields(payload, true));

        assertThrows(IOException.class,
          () -> channel.getMessage(new byte[payload.length], 0, payload.length));
    }

    @Test
    public void testGetMessageRejectsNonGZIPData() throws Exception {
        TestGZIPChannel channel = new TestGZIPChannel();
        channel.setInput("not gzip data".getBytes(StandardCharsets.UTF_8));

        assertThrows(IOException.class, () -> channel.getMessage(new byte[4], 0, 4));
    }

    private static byte[] receive(TestGZIPChannel channel) throws Exception {
        int length = channel.getMessageLength();
        byte[] message = new byte[length];
        channel.getMessage(message, 0, length);
        return message;
    }

    private static byte[] frame(int advertisedLength, byte[] payload) throws IOException {
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(advertisedLength >> 8);
        frame.write(advertisedLength);
        try (GZIPOutputStream gzip = new GZIPOutputStream(frame)) {
            gzip.write(payload);
        }
        return frame.toByteArray();
    }

    /*
     * GZIPOutputStream never writes the optional header fields, so build a member
     * with FEXTRA, FNAME, FCOMMENT and FHCRC by hand.
     */
    private static byte[] memberWithOptionalHeaderFields(byte[] payload, boolean corruptHeaderCRC) {
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.writeBytes(new byte[] { 0x1F, (byte) 0x8B, 8, 2 | 4 | 8 | 16, 0, 0, 0, 0, 0, (byte) 0xFF });
        header.writeBytes(new byte[] { 3, 0, 'x', 'y', 'z' });
        header.writeBytes("name\0".getBytes(StandardCharsets.ISO_8859_1));
        header.writeBytes("comment\0".getBytes(StandardCharsets.ISO_8859_1));
        CRC32 headerCRC = new CRC32();
        headerCRC.update(header.toByteArray());
        int hcrc = (int) headerCRC.getValue() & 0xFFFF;
        if (corruptHeaderCRC)
            hcrc ^= 1;

        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(payload);
        deflater.finish();
        ByteArrayOutputStream deflated = new ByteArrayOutputStream();
        byte[] buffer = new byte[64];
        while (!deflater.finished())
            deflated.write(buffer, 0, deflater.deflate(buffer));
        deflater.end();
        CRC32 dataCRC = new CRC32();
        dataCRC.update(payload);

        ByteArrayOutputStream member = new ByteArrayOutputStream();
        member.writeBytes(header.toByteArray());
        member.write(hcrc);
        member.write(hcrc >> 8);
        member.writeBytes(deflated.toByteArray());
        writeUInt(member, dataCRC.getValue());
        writeUInt(member, payload.length);
        return member.toByteArray();
    }

    private static void writeUInt(ByteArrayOutputStream out, long value) {
        for (int i = 0; i < 4; i++)
            out.write((int) (value >> (8 * i)));
    }

    private static byte[] concat(byte[] first, byte[] second) throws IOException {
        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        combined.write(first);
        combined.write(second);
        return combined.toByteArray();
    }

    private static class TestGZIPChannel extends GZIPChannel {
        private CloseTrackingInputStream input;

        private void setInput(byte[] bytes) {
            setInput(new ByteArrayInputStream(bytes));
        }

        private void setInput(InputStream input) {
            this.input = new CloseTrackingInputStream(input);
            serverIn = new DataInputStream(this.input);
        }

        private boolean isInputClosed() {
            return input.closed;
        }
    }

    private static class CloseTrackingInputStream extends FilterInputStream {
        private boolean closed;

        CloseTrackingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    private static class FragmentedInputStream extends FilterInputStream {
        FragmentedInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, Math.min(len, 1));
        }
    }
}
