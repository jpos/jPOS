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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
     * @exception IOException
     * @see ISOPackager
     */
    public GZIPChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public GZIPChannel (ISOPackager p, ServerSocket serverSocket) 
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
    }
    /**
     * @return the Message len
     * @exception IOException, ISOException
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
    protected void getMessage (byte[] b, int offset, int len) throws IOException, ISOException { 
    	int total = 0;
        GZIPInputStream gzip = new GZIPInputStream(serverIn);
        while (total < len) {
        	int nread = gzip.read (b, offset, len - total);
        	if (nread == -1) {
        		throw new ISOException("End of compressed stream reached before all data was read"); 
        	}
        	total += nread;
        	offset += nread;
        }
    }
}

