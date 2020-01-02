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
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Implements an ISOChannel able to exchange messages with
 * ACI's BASE24 over a TCP link, modified from BASE24ISOChannel 
 * by Victor A. Salaman (salaman@teknos.com) .<br>
 * An instance of this class exchanges messages by means of an
 * intermediate 'port server' as described in the
 * <a href="/doc/javadoc/overview-summary.html">Overview</a> page.
 * @author apr@cs.com.uy
 * @author salaman@teknos.com
 *
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */

@SuppressWarnings("deprecation")
public class BASE24TCPChannel extends BaseChannel {
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public BASE24TCPChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public BASE24TCPChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public BASE24TCPChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public BASE24TCPChannel (ISOPackager p, ServerSocket serverSocket) 
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
    protected void sendMessageLength(int len) throws IOException {
        len++;  // one byte trailler
        serverOut.write (len >> 8);
        serverOut.write (len);
    }
    protected int getMessageLength() throws IOException, ISOException {
        int l = 0;
        byte[] b = new byte[2];
        Logger.log (new LogEvent (this, "get-message-length"));
        while (l == 0) {
            serverIn.readFully(b,0,2);
            l = ((int)b[0] &0xFF) << 8 | (int)b[1] &0xFF;
            if (l == 0) {
                serverOut.write(b);
                serverOut.flush();
            }
        }
        Logger.log (new LogEvent (this, "got-message-length", Integer.toString(l)));
        return l - 1;   // trailler length
    }
    protected void getMessageTrailler() throws IOException {
        Logger.log (new LogEvent (this, "get-message-trailler"));
        byte[] b = new byte[1];
        serverIn.readFully(b,0,1);
        Logger.log (new LogEvent (this, "got-message-trailler", ISOUtil.hexString(b)));
    }
}

