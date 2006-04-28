/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso.channel;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;

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
            l = ((((int)b[0])&0xFF) << 8) | (((int)b[1])&0xFF);
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
    }
    protected void getMessage (byte[] b, int offset, int len) throws IOException { 
        GZIPInputStream gzip = new GZIPInputStream(serverIn);
        gzip.read (b, offset, len);
    }
}

