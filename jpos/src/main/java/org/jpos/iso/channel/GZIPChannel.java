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

import org.jpos.iso.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 * ISOChannel implementation.
 *
 * @author apr@jpos.org
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class GZIPChannel extends BaseChannel {
    /** Default constructor. */
    public GZIPChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public GZIPChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @exception IOException on I/O error
     * @see ISOPackager
     */
    public GZIPChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException on I/O error
     * @see ISOPackager
     */
    public GZIPChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    /**
     * Constructs a packager with the given length and description.
     * @param len the packed Message len
     * @exception IOException on I/O error
     */
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (len >> 8);
        serverOut.write (len);
    }
    /**
     * @return the Message len
     * @exception IOException on I/O error
     * @exception ISOException on ISO processing error
     */
    protected int getMessageLength() throws IOException, ISOException {
        int l = 0;
        byte[] b = new byte[4];
        while (l == 0) {
            serverIn.readFully(b,0,2);
            l = ((int)b[0] &0xFF) << 8 | (int)b[1] &0xFF;
            if (l == 0) {
                serverOut.write(b);
                serverOut.flush();
            }
        }
        return l;
    }
    protected void sendMessage (byte[] b, int offset, int len) 
        throws IOException
    {
        GZIPOutputStream gzip = new GZIPOutputStream(serverOut);
        gzip.write(b, offset, len);
        gzip.finish();
        gzip.flush();
    }
    /*
     * Reads exactly one GZIP member (header, deflate data and trailer) and nothing
     * past it, so the length prefix of the next frame stays unread. The Inflater is
     * fed one byte at a time because GZIPInputStream may read past the trailer,
     * depending on the JDK version.
     */
    protected void getMessage (byte[] b, int offset, int len) throws IOException, ISOException {
        DataInputStream in = Objects.requireNonNull(serverIn);
        readHeader(in);
        Inflater inflater = new Inflater(true);
        try {
            CRC32 crc = new CRC32();
            byte[] input = new byte[1];
            byte[] extra = new byte[1];
            int total = 0;
            while (!inflater.finished()) {
                if (inflater.needsInput()) {
                    input[0] = in.readByte();
                    inflater.setInput(input, 0, 1);
                }
                if (total < len) {
                    int nread = inflater.inflate(b, offset + total, len - total);
                    crc.update(b, offset + total, nread);
                    total += nread;
                } else if (inflater.inflate(extra) > 0) {
                    throw new ISOException("Compressed stream contains more data than expected");
                }
                if (inflater.needsDictionary())
                    throw new ZipException("Unsupported GZIP preset dictionary");
            }
            if (total < len)
                throw new ISOException("End of compressed stream reached before all data was read");

            // The inflater may still hold the first trailer byte.
            byte[] trailer = new byte[8];
            int remaining = inflater.getRemaining();
            System.arraycopy(input, input.length - remaining, trailer, 0, remaining);
            in.readFully(trailer, remaining, trailer.length - remaining);
            if (readUInt(trailer, 0) != crc.getValue()
              || readUInt(trailer, 4) != (inflater.getBytesWritten() & 0xFFFFFFFFL))
                throw new ZipException("Corrupt GZIP trailer");
        } catch (DataFormatException e) {
            throw new ZipException(e.getMessage() != null ? e.getMessage() : "Invalid ZLIB data format");
        } finally {
            inflater.end();
        }
    }

    private static final int FHCRC = 2;
    private static final int FEXTRA = 4;
    private static final int FNAME = 8;
    private static final int FCOMMENT = 16;

    private static void readHeader (DataInputStream in) throws IOException {
        CRC32 crc = new CRC32();
        byte[] header = new byte[10];
        in.readFully(header);
        crc.update(header);
        if ((header[0] & 0xFF) != 0x1F || (header[1] & 0xFF) != 0x8B)
            throw new ZipException("Not in GZIP format");
        if (header[2] != 8)
            throw new ZipException("Unsupported compression method");
        int flags = header[3] & 0xFF;
        if ((flags & FEXTRA) != 0) {
            byte[] xlen = new byte[2];
            in.readFully(xlen);
            crc.update(xlen);
            byte[] extra = new byte[(xlen[0] & 0xFF) | (xlen[1] & 0xFF) << 8];
            in.readFully(extra);
            crc.update(extra);
        }
        if ((flags & FNAME) != 0)
            skipZeroTerminated(in, crc);
        if ((flags & FCOMMENT) != 0)
            skipZeroTerminated(in, crc);
        if ((flags & FHCRC) != 0) {
            int expected = (int) crc.getValue() & 0xFFFF;
            int actual = in.readUnsignedByte() | in.readUnsignedByte() << 8;
            if (actual != expected)
                throw new ZipException("Corrupt GZIP header");
        }
    }

    private static void skipZeroTerminated (DataInputStream in, CRC32 crc) throws IOException {
        int c;
        do {
            c = in.readUnsignedByte();
            crc.update(c);
        } while (c != 0);
    }

    private static long readUInt (byte[] b, int offset) {
        return (b[offset] & 0xFFL)
          | (b[offset + 1] & 0xFFL) << 8
          | (b[offset + 2] & 0xFFL) << 16
          | (b[offset + 3] & 0xFFL) << 24;
    }
}
