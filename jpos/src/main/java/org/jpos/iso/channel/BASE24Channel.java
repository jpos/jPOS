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

import org.jpos.iso.*;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Implements an ISOChannel capable to exchange messages with
 * ACI's BASE24[tm] over an X.25 link.
 *
 * An instance of this class exchanges messages by means of an
 * intermediate 'port server' as described in the
 * <a href="/doc/javadoc/overview-summary.html">Overview</a> page.
 * @author apr@cs.com.uy
 *
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
@SuppressWarnings("deprecation")
public class BASE24Channel extends BaseChannel {
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public BASE24Channel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public BASE24Channel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public BASE24Channel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public BASE24Channel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    /**
     * @param m the Message to send (in this case it is unused)
     * @param len   message len (ignored)
     * @exception IOException
     */
    protected void sendMessageTrailler(ISOMsg m, int len) throws IOException {
        serverOut.write (3);
    }
    /**
     * @return a byte array with the received message
     * @exception IOException
     */
    protected byte[] streamReceive() throws IOException {
        int i;
        byte[] buf = new byte[4096];
        for (i=0; i<4096; i++) {
            int c = serverIn.read();
            if (c == 0x3)
                break;
            buf[i] = (byte) c;
        }
        if (i == 4096)
            throw new IOException("message too long");

        byte[] d = new byte[i];
        System.arraycopy(buf, 0, d, 0, i);
        return d;
    }
}
