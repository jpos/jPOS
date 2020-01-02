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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Implements an ISOChannel suitable to be used to connect to an X.25 PAD. 
 * It waits a limited amount of time to decide when a packet is ready
 * to be unpacked.
 *
 * This channel is based on PADChannel version 1.4. The new version
 * seems to have some problems dealing with ETXs (we're working on it).
 * Use this version _only_ if you have problems with current PADChannel
 * as it will be deprecated some time in the future.
 *
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
@SuppressWarnings("unchecked")
public class X25Channel extends BaseChannel {
    BufferedReader reader = null;
    protected byte[] header;
    /**
     * No-args constructor
     */
    public X25Channel () {
        super();
    }
    /**
     * Constructs client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public X25Channel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public X25Channel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public X25Channel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    /**
     * @return a byte array with the received message
     * @exception IOException
     */
    protected byte[] streamReceive() throws IOException {
        int c, k=0, len = 1;
        Vector v = new Vector();

        c = serverIn.read();
        if (c == -1)
            throw new EOFException ("connection closed");
        byte[] b = new byte[1];
        b[0] = (byte) c;
        v.addElement (b);

        // Wait for packets until timeout
        while ((c = serverIn.available()) > 0) {
            b = new byte[c];
            if (serverIn.read (b) != c)
                throw new EOFException ("connection closed");
            v.addElement (b);
            len += c;
            try {
                Thread.sleep (50);
            } catch (InterruptedException e) { }
        }

        byte[] d = new byte[len];
        for (int i=0; i<v.size(); i++) {
            b = (byte[]) v.elementAt(i);
            System.arraycopy (b, 0, d, k, b.length);
            k += b.length;
        }
        return d;
    }
    protected void connect (Socket socket) throws IOException {
        super.connect (socket);
        reader = new BufferedReader (new InputStreamReader (serverIn));
    }
    public void disconnect () throws IOException {
        super.disconnect ();
        reader = null;
    }
    protected int getHeaderLength() { 
        return header != null ? header.length : 0;
    }
    public void setHeader (byte[] header) {
        this.header = header;
    }
    /**
     * @param header Hex representation of header
     */
    public void setHeader (String header) {
        setHeader (
            ISOUtil.hex2byte (header.getBytes(), 0, header.getBytes().length / 2)
        );
    }
    public byte[] getHeader () {
        return header;
    }
    protected void sendMessageHeader(ISOMsg m, int len) throws IOException { 
        if (m.getHeader() != null)
            serverOut.write(m.getHeader());
        else if (header != null) 
            serverOut.write(header);
    }
}
