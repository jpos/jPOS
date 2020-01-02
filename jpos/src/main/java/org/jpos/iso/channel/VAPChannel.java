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

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.iso.header.BASE1Header;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * ISOChannel implementation - VISA's VAP framing
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
@SuppressWarnings("unused")
public class VAPChannel extends BaseChannel {
    String srcid = "000000";
    String dstid = "000000";
    boolean debugPoll;
    int headerFormat = 2;
    private boolean replyKeepAlive = true;
    private boolean swapDirection = false;

    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public VAPChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager (should be ISO87BPackager)
     * @see org.jpos.iso.packager.ISO87BPackager
     */
    public VAPChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager (should be ISO87BPackager)
     * @exception IOException
     * @see org.jpos.iso.packager.ISO87BPackager
     */
    public VAPChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public VAPChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    public void setSrcId (String srcid) {
        this.srcid = srcid;
    }
    public String getSrcId () {
        return srcid;
    }
    public void setDstId (String dstid) {
        this.dstid = dstid;
    }
    public String getDstId () {
        return dstid;
    }
    /**
     * The default header for VAPChannel is BASE1Header
     */
    protected ISOHeader getDynamicHeader (byte[] image) {
        return new BASE1Header(image);
    }
	
    /**
     * This method reads in a Base 1 Header.
     *
     * @param hLen
     * @throws IOException
     */
    protected byte[] readHeader(int hLen)
        throws IOException {
        // Read the first byte of the header (header length)
        int b = serverIn.read();
        int bytesRead = b;

        if (b != -1)
        {
            // Create am array to read the header into
            byte bytes[] = new byte[b];
            // Stick the first byte in
            bytes[0] = (byte) b;
            // and read the rest of the header
            serverIn.readFully(bytes, 1, b - 1);

            // Is there another header following on
            if ((bytes[1] & 0x80) == 0x80)
            {
                b = serverIn.read();
                bytesRead += b;

                // Create an array big enough for both headers
                byte tmp[] = new byte[bytesRead];
				
                // Copy in the original
                System.arraycopy(bytes, 0, tmp, 0, bytes.length);

                // And this one
                tmp[bytes.length] = (byte) b;
                serverIn.readFully(tmp, bytes.length + 1, b - 1);
                bytes = tmp;
            }
            return bytes;
        }
        else
        {
            throw new IOException("Error reading header");
        }
    }
	
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (len >> 8);
        serverOut.write (len);
        serverOut.write (0);
        serverOut.write (0);
    }
    /**
     * @param   m   the message
     * @param   len already packed message len (to avoid re-pack)
     * @exception IOException
     */
    protected void sendMessageHeader(ISOMsg m, int len) 
        throws IOException
    {
        ISOHeader h = !isOverrideHeader() && m.getHeader() != null ?
                m.getISOHeader() :
                new BASE1Header (srcid, dstid, headerFormat);

        if (h instanceof BASE1Header)
            ((BASE1Header)h).setLen(len);

        serverOut.write(h.pack());
    }
    protected int getMessageLength() throws IOException, ISOException {
        int l = 0;
        byte[] b = new byte[4];
        // ignore VAP polls (0 message length)
        while (l == 0) {
            serverIn.readFully(b,0,4);
            l = ((int)b[0] &0xFF) << 8 | (int)b[1] &0xFF;

            if (replyKeepAlive && l == 0) {
                synchronized (serverOutLock) {
                    serverOut.write(b);
                    serverOut.flush();
                    if (debugPoll)
                        Logger.log(new LogEvent(this, "poll"));
                }
            }
        }
        return l;
    }

    protected int getHeaderLength() {
        return BASE1Header.LENGTH;
    }
        
    protected boolean isRejected(byte[] b) {
        BASE1Header h = new BASE1Header(b);
        return h.isRejected() || h.getHLen() != BASE1Header.LENGTH;
    }

    protected boolean shouldIgnore (byte[] b) {
        if (b != null) {
            BASE1Header h = new BASE1Header(b);
            return h.getFormat() > 2;
        }
        return false;
    }


    /**
     * sends an ISOMsg over the TCP/IP session.
     *
     * swap source/destination addresses in BASE1Header if
     * a reply message is detected.<br>
     * Sending an incoming message is seen as a reply.
     *
     * @param m the Message to be sent
     * @exception IOException
     * @exception ISOException
     * @see ISOChannel#send
     */
    public void send (ISOMsg m) throws IOException, ISOException
    {
        if (m.isIncoming() && m.getHeader() != null && swapDirection) {
            m.getISOHeader().swapDirection();
        }
        super.send(m);
    }

    public void setConfiguration (Configuration cfg)
        throws ConfigurationException 
    {
        super.setConfiguration (cfg);
        srcid = cfg.get ("srcid", "000000");
        dstid = cfg.get ("dstid", "000000");
        debugPoll = cfg.getBoolean("debug-poll", false);
        headerFormat = cfg.getInt("header-format", 2);
        replyKeepAlive = cfg.getBoolean("reply-keepalive", true);
        swapDirection = cfg.getBoolean("swap-direction", true);
    }
}
