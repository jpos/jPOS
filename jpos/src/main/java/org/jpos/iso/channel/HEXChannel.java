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
 * Sends a four ASCII hex characters indicating message length (up to 0xffff)
 *
 * @author Mladen Mrkic <mmrkic@arius.co.yu>
 * @author apr
 * @version $Revision$ $Date$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class HEXChannel extends BaseChannel {
    public HEXChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @param TPDU  an optional raw header (i.e. TPDU)
     * @see ISOPackager
     */
    public HEXChannel (String host, int port, ISOPackager p, byte[] TPDU) {
        super(host, port, p);
        this.header = TPDU;
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @param TPDU  an optional raw header (i.e. TPDU)
     * @exception IOException
     * @see ISOPackager
     */
    public HEXChannel (ISOPackager p, byte[] TPDU) throws IOException {
        super(p);
        this.header = TPDU;
    }
    /**
     * constructs server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param TPDU  an optional raw header (i.e. TPDU)
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public HEXChannel (ISOPackager p, byte[] TPDU, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
        this.header = TPDU;
    }
    protected void sendMessageLength(int len) throws IOException {
        if (len > 0xFFFF)
            throw new IOException (len + " exceeds maximum length");
        try {
            serverOut.write (
                ISOUtil.zeropad (Integer.toString (len % 0xFFFF,16), 4).getBytes()
            );
        } 
        catch (ISOException e) {
            Logger.log (new LogEvent (this, "send-message-length", e));
        }
    }
    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[4];
        serverIn.readFully(b,0,4);
        return Integer.parseInt (new String(b),16);
    }
}

