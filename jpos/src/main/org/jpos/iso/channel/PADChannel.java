package org.jpos.iso.channel;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.jpos.iso.*;
import org.jpos.iso.packager.XMLPackager;

/**
 * Implements an ISOChannel suitable to be used to connect to an X.25 PAD. 
 * It waits a limited amount of time to decide when a packet is ready
 * to be unpacked.
 *
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class PADChannel extends BaseChannel {
    BufferedReader reader = null;
    protected byte[] header;
    /**
     * No-args constructor
     */
    public PADChannel () {
        super();
    }
    /**
     * Constructs client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public PADChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public PADChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public PADChannel (ISOPackager p, ServerSocket serverSocket) 
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
	    ISOUtil.hex2byte (header.getBytes(), 0, header.getBytes().length)
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
