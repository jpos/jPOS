package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ISOChannel implementation - RAW Channel
 * Send packet len (4 bytes network byte order) followed by
 * raw data. Usefull when you need to send propietary headers
 * with ISOMsgs (such as NAC's TPDUs)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class RawChannel extends ISOChannel {
	byte[] TPDU;

	/**
	 * Public constructor (used by Class.forName("...").newInstance())
	 */
	public RawChannel () {
		super();
	}
	/**
	 * Construct client ISOChannel
	 * @param host	server TCP Address
	 * @param port  server port number
	 * @param p     an ISOPackager
	 * @param TPDU  an optional raw header (i.e. TPDU)
	 * @see ISOPackager
	 */
	public RawChannel (String host, int port, ISOPackager p, byte[] TPDU) {
		super(host, port, p);
		this.TPDU = TPDU;
	}
	/**
	 * Construct server ISOChannel
	 * @param p     an ISOPackager
	 * @param TPDU  an optional raw header (i.e. TPDU)
	 * @exception IOException
	 * @see ISOPackager
	 */
	public RawChannel (ISOPackager p, byte[] TPDU) throws IOException {
		super(p);
		this.TPDU = TPDU;
	}
	/**
	 * constructs a server ISOChannel associated with a Server Socket
	 * @param p     an ISOPackager
	 * @param TPDU  an optional raw header (i.e. TPDU)
	 * @param serverSocket where to accept a connection
	 * @exception IOException
	 * @see ISOPackager
	 */
	public RawChannel (ISOPackager p, byte[] TPDU, ServerSocket serverSocket) 
		throws IOException
	{
		super(p, serverSocket);
		this.TPDU = TPDU;
	}
	protected void sendMessageLength(int len) throws IOException {
		if (TPDU != null)
			len += TPDU.length;
		serverOut.write (len >> 24);
		serverOut.write (len >> 16);
		serverOut.write (len >> 8);
		serverOut.write (len);
	}
	protected int getMessageLength() throws IOException, ISOException {
		byte[] b = new byte[4];
		if (serverIn.read(b,0,4) != 4)
			throw new ISOException("error reading message length");
		return (int) (
			((((int)b[0])&0xFF) << 24) | 
			((((int)b[1])&0xFF) << 16) | 
			((((int)b[2])&0xFF) << 8) | 
			(((int)b[3])&0xFF));
	}
	protected void sendMessageHeader(ISOMsg m) throws IOException { 
		if (TPDU != null)
			serverOut.write(TPDU);
	}
	protected int getHeaderLength() { 
		return TPDU != null ? TPDU.length : 0;
	}
}
