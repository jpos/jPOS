package org.jpos.iso.channel;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.jpos.iso.*;
import org.jpos.iso.packager.XMLPackager;

/**
 * Implements an ISOChannel able to exchange <b>jPOS generated</b> 
 * (or compliant) XML based ISO-8583 messages 
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class XMLChannel extends BaseChannel {
    BufferedReader reader = null;
    /**
     * Public constructor (used by Class.forName("...").newInstance())
     */
    public XMLChannel () {
        super();
    }
    /**
     * Constructs client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public XMLChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }
    /**
     * Construct server ISOChannel
     * @param p     an ISOPackager
     * @see ISOPackager
     * @exception IOException
     */
    public XMLChannel (ISOPackager p) throws IOException {
        super(p);
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public XMLChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException
    {
        super(p, serverSocket);
    }
    /**
     * @return a byte array with the received message
     * @exception IOException
     */
    protected byte[] streamReceive() throws IOException {
	StringBuffer sb = new StringBuffer();
	while (reader != null) {
	    String s = reader.readLine();
	    if (s == null)
		throw new EOFException();
	    sb.append (s);
	    if (s.indexOf ("</" + XMLPackager.ISOMSG_TAG + ">") == 0)
		break;
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
}
