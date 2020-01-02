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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts &lt;isomsg&gt; blocks from standard jPOS log
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class LogChannel extends BaseChannel {
    BufferedReader reader = null;
    int timestampField=0;
    int realmField=0;
    private static Pattern logPattern = Pattern.compile("<log realm=\"(\\.|[^\"]*)\"\\sat=\"((\\.|[^\"])*)\"");
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public LogChannel () {
        super();
    }
    /**
     * Constructs client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public LogChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public LogChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public LogChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    /**
     * @return a byte array with the received message
     * @exception IOException
     */
    protected byte[] streamReceive() throws IOException {
        StringBuilder sb = new StringBuilder();
        String realm = null;
        String at= null;
        int inMsg = 0;
        while (reader != null) {
            String s = reader.readLine();
            if (s == null)
                throw new EOFException();
            if ((timestampField > 0 || realmField > 0) && s.contains("<log") && s.contains("at=")) {
                Matcher matcher = logPattern.matcher(s);
                if (matcher.find() && matcher.groupCount() > 1) {
                    if (realmField > 0)
                        realm = matcher.group(1);
                    if (timestampField > 0)
                        at = matcher.group(2);
                }
            }
            if (s.contains("<isomsg")) {
                inMsg++;
            }
            if (s.contains("</isomsg>") && --inMsg == 0) {
                if (at != null || realm != null && inMsg == 0) {
                    if (realm != null) {
                        sb.append("  <field id=\"" + realmField + "\" value=\"" + realm + "\" />");
                        realm = null;
                    }
                    if (at != null) {
                        sb.append("  <field id=\"" + timestampField + "\" value=\"" + at + "\" />");
                    }
                }
                sb.append (s);
                break;
            }
            if (inMsg > 0)
                sb.append (s);
        }
        return sb.toString().getBytes();
    }
    protected int getHeaderLength() { 
        return 0; 
    }
    protected void connect (Socket socket) throws IOException {
        super.connect (socket);
        reader = new BufferedReader (new InputStreamReader (serverIn));
    }
    public void disconnect () throws IOException {
        super.disconnect ();
        reader = null;
    }

    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        timestampField = cfg.getInt("timestamp-field", 0);
        realmField = cfg.getInt("realm-field", 0);
    }
}
