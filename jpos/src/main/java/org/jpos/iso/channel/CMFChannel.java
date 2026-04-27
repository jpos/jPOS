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

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * {@link ISOChannel} implementation for the jPOS-CMF framing.
 * This channel uses a 3-byte big-endian length prefix (24-bit unsigned) and no
 * additional message header.
 *
 * <h2>Keep-alive handling</h2>
 * A zero-length frame is treated as a keep-alive. When a keep-alive is received and either
 * {@code replyKeepAlive} is enabled or {@link #isExpectKeepAlive()} returns {@code true}, the channel
 * echoes the keep-alive back to the peer.
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li>{@code reply-keepalive} (boolean, default {@code true}): Whether to echo a received
 *   keep-alive (zero-length frame) back to the peer.</li>
 *   <li>{@code max-keepalives-in-a-row} (integer, default {@code 0}): Maximum number of consecutive
 *   keep-alives allowed while waiting for a non-zero length frame. A value of {@code 0} disables
 *   the limit (unlimited), preserving legacy behavior.</li>
 * </ul>
 *
 * @author apr@jpos.org
 * @since 3.0.1
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 * @see ISOPackager
 */
public class CMFChannel extends BaseChannel {
    /**
     * Number of bytes used to encode the length prefix.
     */
    private static final int LENGTH_BYTES = 3;

    /**
     * Maximum payload length representable by a 24-bit unsigned length field (0xFFFFFF).
     */
    private static final int MAX_PACKET_LENGTH = 0x00FF_FFFF;

    private boolean replyKeepAlive = true;

    /**
     * Maximum number of consecutive keep-alives allowed while waiting for a non-zero length frame.
     * A value of {@code 0} means unlimited.
     */
    private int maxKeepAlivesInARow = 0;

    /**
     * Creates an unconfigured {@code CMFChannel}.
     *
     * The caller is expected to set the packager/connection parameters through the usual
     * {@link BaseChannel} configuration and/or setters before use.
     */
    public CMFChannel () {
        super();
    }

    /**
     * Constructs a client {@code CMFChannel}.
     *
     * @param host server TCP address or hostname.
     * @param port server TCP port number.
     * @param p    the {@link ISOPackager} used to pack/unpack {@link ISOMsg}s.
     *
     * @see ISOPackager
     */
    public CMFChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }

    /**
     * Constructs a server {@code CMFChannel}.
     *
     * This constructor creates a {@link java.net.ServerSocket} internally (as per {@link BaseChannel})
     * and waits for inbound connections when {@link BaseChannel#accept(java.net.ServerSocket)} is invoked.
     *
     * @param p the {@link ISOPackager} used to pack/unpack {@link ISOMsg}s.
     * @throws IOException if the underlying server socket cannot be created.
     *
     * @see ISOPackager
     */
    public CMFChannel (ISOPackager p) throws IOException {
        super(p);
    }

    /**
     * Constructs a server {@code CMFChannel} associated with an existing {@link java.net.ServerSocket}.
     *
     * @param p            the {@link ISOPackager} used to pack/unpack {@link ISOMsg}s.
     * @param serverSocket the server socket used to accept a connection.
     * @throws IOException if an I/O error occurs while initializing the channel.
     *
     * @see ISOPackager
     */
    public CMFChannel (ISOPackager p, ServerSocket serverSocket) throws IOException {
        super(p, serverSocket);
    }

    /**
     * Sends the CMF message length prefix.
     *
     * The CMF framing uses a 3-byte big-endian length prefix (24-bit unsigned).
     *
     * @param len the packed message length in bytes.
     * @throws IOException if an I/O error occurs writing to the socket output stream or if
     *                     {@code len} is outside the valid range ({@code 0..0xFFFFFF}).
     */
    @Override
    protected void sendMessageLength(int len) throws IOException {
        if (len < 0 || len > MAX_PACKET_LENGTH) {
            throw new IOException(
              "Invalid CMF packet length " + len + " (valid range: 0.." + MAX_PACKET_LENGTH + ")"
            );
        }
        serverOut.write((len >>> 16) & 0xFF);
        serverOut.write((len >>> 8) & 0xFF);
        serverOut.write(len & 0xFF);
    }

    /**
     * Reads the CMF message length prefix.
     *
     * Reads a 3-byte big-endian length value. A zero-length frame is treated as a keep-alive.
     * When a keep-alive is received and either {@code replyKeepAlive} is enabled or
     * {@link #isExpectKeepAlive()} is {@code true}, the keep-alive is echoed back to the peer.
     * The method continues reading until a non-zero length is obtained.
     *
     * <p>If {@code max-keepalives-in-a-row} is configured to a value greater than {@code 0},
     * the method will fail after receiving more than that number of consecutive keep-alives
     * without a subsequent non-zero length frame.</p>
     *
     * @return the packed message length in bytes (always {@code > 0}).
     * @throws IOException  if an I/O error occurs reading from the socket input stream, or if too
     *                      many consecutive keep-alives are received (when limited).
     * @throws ISOException if the channel detects an ISO-level framing/processing error.
     */
    @Override
    protected int getMessageLength() throws IOException, ISOException {
        int keepAlives = 0;

        for (;;) {
            int b0 = serverIn.readUnsignedByte();
            int b1 = serverIn.readUnsignedByte();
            int b2 = serverIn.readUnsignedByte();

            int len = (b0 << 16) | (b1 << 8) | b2;

            if (len != 0) {
                // Defensive check; with 3 bytes, len cannot exceed MAX_PACKET_LENGTH,
                // but this documents intent and protects future refactors.
                if (len > MAX_PACKET_LENGTH) {
                    throw new ISOException(
                      "Invalid CMF packet length " + len + " (max " + MAX_PACKET_LENGTH + ")"
                    );
                }
                return len;
            }

            // Keep-alive (0 length).
            if (maxKeepAlivesInARow > 0 && ++keepAlives > maxKeepAlivesInARow) {
                throw new IOException("Too many consecutive keep-alives (" + keepAlives + ")");
            }

            if (replyKeepAlive || isExpectKeepAlive()) {
                serverOutLock.lock();
                try {
                    // Echo the same 3-byte zero-length frame.
                    serverOut.write(0);
                    serverOut.write(0);
                    serverOut.write(0);
                    serverOut.flush();
                } finally {
                    serverOutLock.unlock();
                }
            }
        }
    }

    /**
     * Returns the channel header length.
     *
     * CMF framing does not include a separate message header beyond the 3-byte length prefix.
     *
     * @return {@code 0}.
     */
    @Override
    protected int getHeaderLength() {
        return 0;
    }

    /**
     * Sends the message header (no-op).
     *
     * CMF framing does not define a message header; only the length prefix is used.
     *
     * @param m   the message being sent.
     * @param len the packed message length in bytes.
     */
    @Override
    protected void sendMessageHeader(ISOMsg m, int len) {
        // CMF channel does not use a header.
    }

    /**
     * Applies configuration to this channel.
     *
     * In addition to the base {@link BaseChannel} configuration, this method reads:
     * <ul>
     *   <li>{@code reply-keepalive} (boolean, default {@code true}).</li>
     *   <li>{@code max-keepalives-in-a-row} (integer, default {@code 0}).</li>
     * </ul>
     *
     * @param cfg the configuration source.
     * @throws ConfigurationException if configuration cannot be applied.
     */
    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        replyKeepAlive = cfg.getBoolean("reply-keepalive", true);
        maxKeepAlivesInARow = cfg.getInt("max-keepalives-in-a-row", 0);
    }

    /**
     * Returns the maximum packet length supported by this channel.
     *
     * CMF framing uses a 3-byte unsigned length prefix (24-bit). Therefore, the maximum
     * representable non-zero payload length is {@code 0xFFFFFF} (16,777,215 bytes).
     *
     * @return the maximum packet length, in bytes.
     */
    @Override
    public int getMaxPacketLength() {
        return MAX_PACKET_LENGTH;
    }
}
