package org.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ISOChannel implementation - Postilion Channel
 * Send packet len (2 bytes network byte order MSB/LSB) followed by
 * raw data. 
 *
 * @author salaman@teknos.com
 * @version Id: PostChannel.java,v 1.0 1999/05/14 19:00:00 may Exp 
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class PostChannel extends ISOChannel {
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public PostChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public PostChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @exception IOException
     * @see ISOPackager
     */
    public PostChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public PostChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (len >> 8);
        serverOut.write (len);
    }
    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[2];
        if (serverIn.read(b,0,2) != 2)
            throw new ISOException("error reading message length");
        return (int) (
            ((((int)b[0])&0xFF) << 8) | 
            (((int)b[1])&0xFF));
    }
}
