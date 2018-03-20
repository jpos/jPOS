/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jpos.iso.packager.JSONPackager;

/**
 * Implements an ISOChannel able to exchange <b>jPOS generated</b>
 * (or compliant) XML based ISO-8583 messages
 *
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class JSONChannel extends BaseChannel {

    BufferedReader reader = null;

    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public JSONChannel() {
        super();
    }

    /**
     * Constructs client ISOChannel
     *
     * @param host server TCP Address
     * @param port server port number
     * @param p an ISOPackager
     * @see ISOPackager
     */
    public JSONChannel(String host, int port, ISOPackager p) {
        super(host, port, p);
    }

    /**
     * Construct server ISOChannel
     *
     * @param p an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public JSONChannel(ISOPackager p) throws IOException {
        super(p);
    }

    /**
     * constructs a server ISOChannel associated with a Server Socket
     *
     * @param p an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public JSONChannel(ISOPackager p, ServerSocket serverSocket)
            throws IOException {
        super(p, serverSocket);
    }

    /**
     * @return a byte array with the received message
     * @exception IOException
     */
    @Override
    protected byte[] streamReceive() throws IOException {
        String s, q = "";
        if (reader != null) {
            s = reader.readLine();
            if (s != null) {
                q = s.trim();
            }
        }
        if ("".equals(q)) {
            throw new EOFException();
        }
        return q.getBytes();
    }

    protected int getHeaderLength() {
        // JSON Channel does not support header
        return 0;
    }

    protected void sendMessageHeader(ISOMsg m, int len) {
        // JSON Channel does not support header
    }

    protected void connect(Socket socket) throws IOException {
        super.connect(socket);
        reader = new BufferedReader(new InputStreamReader(serverIn));
    }

    public void disconnect() throws IOException {
        super.disconnect();
        if (reader != null) {
            reader.close();
        }
        reader = null;
    }
}
