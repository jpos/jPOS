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

import java.io.IOException;
import java.net.ServerSocket;

/**
 * ISOChannel implementation - CS standard Channel<br>
 * We at <a href="http://www.cs.com.uy">CS</a>, have used
 * the so called ISOChannels for a long time. This class
 * talks with our legacy C++ based systems.<br>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class CSChannel extends BaseChannel {
    private boolean replyKeepAlive = true;
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public CSChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public CSChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @exception IOException
     * @see ISOPackager
     */
    public CSChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public CSChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    /**
     * @param len the packed Message len
     * @exception IOException
     */
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (len >> 8);
        serverOut.write (len);
        serverOut.write (0);
        serverOut.write (0);
    }
    /**
     * @return the Message len
     * @exception IOException, ISOException
     */
    protected int getMessageLength() throws IOException, ISOException {
        int l = 0;
        byte[] b = new byte[4];
        while (l == 0) {
            serverIn.readFully(b,0,4);
            l = ((int)b[0] &0xFF) << 8 | (int)b[1] &0xFF;
            if (replyKeepAlive && l == 0) {
                synchronized (serverOutLock) {
                    serverOut.write(b);
                    serverOut.flush();
                }
            }
        }
        return l;
    }
    protected int getHeaderLength() { 
        // CS Channel does not support header
        return 0; 
    }
    protected void sendMessageHeader(ISOMsg m, int len) {
        // CS Channel does not support header
    }

    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        replyKeepAlive = cfg.getBoolean("reply-keepalive", true);
    }
}
