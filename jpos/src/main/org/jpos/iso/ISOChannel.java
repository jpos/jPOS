package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ISOChannel is an abstract class that provides functionality that
 * allows the transmision and reception of ISO 8583 Messages
 * over a TCP/IP session.<br>
 *
 * It is not thread-safe, ISOMUX takes care of the
 * synchronization details
 *
 * @see ISOMUX
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOMUX
 * @see ISOException
 * @see CSChannel
 * @see Internetworking_with_TCP/IP_ISBN_0-13-474321-0
 *
 */
public abstract class ISOChannel {
	private Socket socket;
	private String host;
	private int port;
	private boolean usable;
	protected DataInputStream serverIn;
	protected DataOutputStream serverOut;
	protected ISOPackager packager;

	/**
	 * @param host	server TCP Address
	 * @param port  server port number
	 * @param p     an ISOPackager
	 * @see ISOPackager
	 */
	public ISOChannel (String host, int port, ISOPackager p) {
		this.host = host;
		this.port = port;
		this.packager = p;
	}
	/**
	 * @return the connection state
	 */
	public boolean isConnected() {
		return socket != null && usable;
	}
	/**
	 * Actually connects to the server
	 * @exception IOException
	 */
    public void connect () throws IOException {
       	socket =  new Socket (host, port);
		serverIn = new DataInputStream (
			new BufferedInputStream (socket.getInputStream ())
		);
		serverOut = new DataOutputStream(
			new BufferedOutputStream(socket.getOutputStream())
		);
		usable = true;
    }
	/**
	 * @param b - new Usable state (used by ISOMUX internals to
	 * flag as unusable in order to force a reconnection)
	 */
	public void setUsable(boolean b) {
		usable = b;
	}
	protected void sendMessageLength(int len) throws IOException { }
	protected void sendMessageHeader(ISOMsg m) throws IOException { }
	protected void sendMessageTrailer(ISOMsg m) throws IOException { }
	protected int getMessageLength() throws IOException, ISOException {
		return -1;
	}
	protected int getHeaderLength() { return 0; }
	protected byte[] streamReceive() throws IOException {
		return new byte[0];
	}
	/**
	 * sends an ISOMsg over the TCP/IP session
	 * @param m the Message to be sent
	 * @exception IOException
	 * @exception ISOException
	 */
	public void send (ISOMsg m) throws IOException, ISOException {
		m.setPackager (packager);
		byte[] b = m.pack();
		// 
		// System.out.println (
		//	"--[pack]--\n"+ ISOUtil.hexString(b) + "\n--[end]--");
		// 
		sendMessageLength(b.length);
		sendMessageHeader(m);
		serverOut.write(b, 0, b.length);
		sendMessageTrailer(m);
		serverOut.flush ();
	}
	/**
	 * Waits and receive an ISOMsg over the TCP/IP session
	 * @return the Message received
	 * @exception IOException
	 * @exception ISOException
	 */
	public ISOMsg receive() throws IOException, ISOException {
		byte[] b;
		int len  = getMessageLength();
		int hLen = getHeaderLength();
		if (len == -1) 
			b = streamReceive();
		else if (len > 10 && len <= 4096) {
			int l;
			if (hLen > 0) {
				// ignore message header (TPDU)
				b = new byte [hLen];
				serverIn.read(b,0,hLen);
				len -= hLen;
			}
			b = new byte[len];
			if ((l=serverIn.read(b,0,len)) != len)
				throw new ISOException(
					"receive error. expected " +len + " received " +l);
			//
			// System.out.println (
			// 	"--[unpack]--\n"+ ISOUtil.hexString(b) + "\n--[end]--");
			//
		}
		else
			throw new ISOException("receive length " +len + " seems extrange");

		ISOMsg m = new ISOMsg();
		m.setPackager (packager);
		if (b.length > 0)	// Ignore NULL messages (i.e. VAP/X.25 sync, etc.)
			m.unpack (b);
		return m;
	}
	/**
	 * Low level receive
	 * @param b byte array
	 * @exception IOException
	 */
	public int getBytes (byte[] b) throws IOException {
		return serverIn.read (b);
	}
	/**
	 * disconnects the TCP/IP session. The instance is ready for
	 * a reconnection. There is no need to create a new ISOChannel<br>
	 * @exception IOException
	 */
	public void disconnect () throws IOException {
		usable = false;
		serverIn  = null;
		serverOut = null;
		if (socket != null)
			socket.close ();
		socket = null;
	}	
	/**
	 * Issues a disconnect followed by a connect
	 * @exception IOException
	 */
	public void reconnect() throws IOException {
		disconnect();
		connect();
	}
}
