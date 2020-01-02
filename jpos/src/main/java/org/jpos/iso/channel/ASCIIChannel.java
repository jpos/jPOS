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
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.io.IOException;
import java.net.ServerSocket;
import java.math.BigInteger;

/**
 * ISOChannel implementation suitable for OASIS Ltd &copy; hosts<br>
 * Message length header: n ASCII digits, configurable by setLengthDigits() (default: 4)
 * or the 'length-digits' Configuration property.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class ASCIIChannel extends BaseChannel {

    /** Number of digits for the message length header */
    protected int lengthDigits= 4;                                      // 4 is default

    private static final BigInteger ten= BigInteger.valueOf(10L);     // just a static 10

    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public ASCIIChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public ASCIIChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @exception IOException
     * @see ISOPackager
     */
    public ASCIIChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public ASCIIChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }


    public void setLengthDigits(int len) { lengthDigits= len; }
    public int getLengthDigits() { return lengthDigits; }


    /**
     * @param len the packed Message len
     * @exception IOException
     */
    protected void sendMessageLength(int len) throws IOException {
        int maxLen= ten.pow(lengthDigits).intValue() - 1;       // 10^lengthDigits - 1

        if (len > maxLen)
            throw new IOException ("len exceeded ("+len+" > "+maxLen+")");
        else if (len < 0)
            throw new IOException ("invalid negative length ("+len+")");
        serverOut.write(ISOUtil.zeropad(len, lengthDigits).getBytes());
    }
    /**
     * @return the Message len
     * @exception IOException, ISOException
     */
    protected int getMessageLength() throws IOException, ISOException {
        int l = 0;
        byte[] b = new byte[lengthDigits];
        while (l == 0) {
            serverIn.readFully(b, 0, lengthDigits);
            try {
                if ((l=Integer.parseInt(new String(b))) == 0) {
                    serverOut.write(b);
                    serverOut.flush();
                }
            } catch (NumberFormatException e) { 
                throw new ISOException ("Invalid message length "+new String(b));
            }
        }
        return l;
    }


    /**
     *
     * Calls super.setConfiguration() and then reads the 'length-digits' property,
     * defaulting to 4
     *
     * @param cfg Configuration
     * @throws ConfigurationException
     */
    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException
    {
        super.setConfiguration(cfg);
        setLengthDigits(cfg.getInt("length-digits", 4));
    }
}

