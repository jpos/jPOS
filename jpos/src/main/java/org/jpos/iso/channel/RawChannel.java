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
 * ISOChannel implementation - RAW Channel
 * Send packet len (4 bytes network byte order) followed by
 * raw data. Usefull when you need to send propietary headers
 * with ISOMsgs (such as NAC's TPDUs)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class RawChannel extends BaseChannel {
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public RawChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host   server TCP Address
     * @param port   server port number
     * @param p      an ISOPackager
     * @param header an optional raw header (i.e. TPDU)
     * @see ISOPackager
     */
    public RawChannel (String host, int port, ISOPackager p, byte[] header) {
        super(host, port, p);
        this.header = header;
    }
    /**
     * Construct server ISOChannel
     * @param p      an ISOPackager
     * @param header an optional raw header (i.e. TPDU)
     * @exception IOException
     * @see ISOPackager
     */
    public RawChannel (ISOPackager p, byte[] header) throws IOException {
        super(p);
        this.header = header;
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p      an ISOPackager
     * @param header an optional raw header (i.e. TPDU)
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public RawChannel (ISOPackager p, byte[] header, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
        this.header = header;
    }
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (len >> 24);
        serverOut.write (len >> 16);
        serverOut.write (len >> 8);
        serverOut.write (len);
    }
    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[4];
        serverIn.readFully(b,0,4);
        return ((int)b[0] &0xFF) << 24 |
                ((int)b[1] &0xFF) << 16 |
                ((int)b[2] &0xFF) << 8 |
                (int)b[3] &0xFF;
    }
    /**
     * New QSP compatible signature (see QSP's ConfigChannel)
     * @param header String as seen by QSP
     */
    public void setHeader (String header) {
        super.setHeader (ISOUtil.str2bcd(header, false));
    }
}
