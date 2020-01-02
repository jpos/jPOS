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
import org.jpos.iso.packager.XMLPackager;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Implements an ISOChannel able to exchange <b>jPOS generated</b> (or
 * compliant) XML based ISO-8583 messages through a Telnet session the telnet
 * commands are simply ignored.
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:marksalter@talktalk.net">Mark Salter</a>
 * @version $Id: TelnetXMLChannel.java 2594 2008-01-22 16:41:31Z apr $
 * 
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class TelnetXMLChannel extends BaseChannel {
    BufferedReader reader = null;

    static final String isomsgStartTag = "<" + XMLPackager.ISOMSG_TAG + ">";
    static final String isomsgEndTag = "</" + XMLPackager.ISOMSG_TAG + ">";

    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public TelnetXMLChannel() {
        super();
    }

    /**
     * Constructs client ISOChannel
     * 
     * @param host
     *            server TCP Address
     * @param port
     *            server port number
     * @param p
     *            an ISOPackager
     * @see ISOPackager
     */
    public TelnetXMLChannel(String host, int port, ISOPackager p) {
        super(host, port, p);
    }

    /**
     * Construct server ISOChannel
     * 
     * @param p
     *            an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public TelnetXMLChannel(ISOPackager p) throws IOException {
        super(p);
    }

    /**
     * constructs a server ISOChannel associated with a Server Socket
     * 
     * @param p
     *            an ISOPackager
     * @param serverSocket
     *            where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public TelnetXMLChannel(ISOPackager p, ServerSocket serverSocket)
            throws IOException {
        super(p, serverSocket);
    }

    /**
     * @return a byte array with the received message
     * @exception IOException
     */
    protected byte[] streamReceive() throws IOException {
        int sp = 0;
        StringBuilder sb = new StringBuilder();
        while (reader != null) {
            /*
             * Throw away any telnet commands - each is 3 bytes first being
             * x'FF'...
             */
            reader.mark(3); // Mark the current position in case there are no
                            // telnet commands (FFnnmm)
            while (reader.read() == 255) {
                reader.skip(2);
                reader.mark(3);
            }
            reader.reset(); // Return to the first byte that was *not* a telnet
                            // command (IAC).

            // Now the commands are out of the way continue with the xml stream
            // until it closes with </isomsg>.
            String s = reader.readLine();
            if (s == null)
                throw new EOFException();
            int isomsgStart = s.indexOf(isomsgStartTag);
            if (isomsgStart >= 0) {
                sp++;
                sb.append(s, isomsgStart, s.length() - isomsgStart);
            } else {
                int isomsgEnd = s.indexOf(isomsgEndTag);
                if (isomsgEnd >= 0) {
                    sb.append(s,0,isomsgEnd + isomsgEndTag.length());
                    if (--sp <= 0)
                        break;
                } else {
                    if (sp > 0)
                        sb.append(s);
                }
            }

        }
        return sb.toString().getBytes();
    }

    protected int getHeaderLength() {
        // XML Channel does not support header
        return 0;
    }

    protected void sendMessageHeader(ISOMsg m, int len) {
        // XML Channel does not support header
    }

    protected void connect(Socket socket) throws IOException {
        super.connect(socket);
        reader = new BufferedReader(new InputStreamReader(serverIn));
    }

    public void disconnect() throws IOException {
        super.disconnect();
        if (reader != null)
            reader.close();
        reader = null;
    }
}
