package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import uy.com.cs.jpos.util.Logger;
import uy.com.cs.jpos.util.LogEvent;

/**
 * ISOChannel implementation - VISA's VAP framing
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class VAPChannel extends ISOChannel {
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public VAPChannel () {
        super();
    }
    /**
     * Construct client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager (should be ISO87BPackager)
     * @param hlen  the header len
     * @see ISO87BPackager
     */
    public VAPChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager (should be ISO87BPackager)
     * @param hlen  the header len
     * @exception IOException
     * @see ISO87BPackager
     */
    public VAPChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public VAPChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    protected void sendMessageLength(int len) throws IOException {
        serverOut.write (len >> 8);
        serverOut.write (len);
        serverOut.write (0);
        serverOut.write (0);
    }
    /**
     * @param   m   the message
     * @param   len already packed message len (to avoid re-pack)
     * @exception IOException
     */
    protected void sendMessageHeader(ISOMsg m, int len) 
        throws IOException
    {
        BASE1Header h = new BASE1Header(m.getHeader());
        h.setLen(len);
        serverOut.write(h.getBytes());
    }
    protected int getMessageLength() throws IOException, ISOException {
        int l = 0;
        byte[] b = new byte[4];
        // ignore VAP polls (0 message length)
        while (l == 0) {
            serverIn.readFully(b,0,4);
            l = ((((int)b[0])&0xFF) << 8) | (((int)b[1])&0xFF);
            if (l == 0) {
                serverOut.write(b);
                serverOut.flush();
		Logger.log (new LogEvent (this, "poll"));
            }
        }
        return l;
    }
    protected int getHeaderLength() {
        return BASE1Header.LENGTH;
    }
    protected boolean isRejected(byte[] b) {
        BASE1Header h = new BASE1Header(b);
        return h.isRejected() || (h.getHLen() != BASE1Header.LENGTH);
    }

    /**
     * sends an ISOMsg over the TCP/IP session<br>
     * swap source/destination addresses in BASE1Header if
     * a reply message is detected.<br>
     * Sending an incoming message is seen as a reply.
     *
     * @param m the Message to be sent
     * @exception IOException
     * @exception ISOException
     * @see ISOChannel#send
     */
    public void send (ISOMsg m) throws IOException, ISOException
    {
        if (m.isIncoming()) {
            BASE1Header h = new BASE1Header(m.getHeader());
            h.swapDirection();
        }
        super.send(m);
    }
}
