/**
 * Provee funciones basicas para transmitir y recibir ISOMsg(s)
 * a traves de una sesion TCP/IP.
 *
 * No es una clase 'synchronized', cuenta con que ISOMUX meneje threads
 * separados para transmision/recepcion y control
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOMUX
 * @see ISOException
 * @see "Internetworking with TCP/IP ISBN 0-13-474321-0"
 */

/*
 * $Log$
 * Revision 1.3  1998/12/14 22:48:23  apr
 * Added RawChannel support
 * Pruebas OK packaging POSNet
 *
 * Revision 1.2  1998/11/28 16:25:53  apr
 * *** empty log message ***
 *
 * Revision 1.1  1998/11/09 23:40:23  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class ISOChannel {
	private Socket socket;
	private String host;
	private int port;
	private boolean usable;
	protected DataInputStream serverIn;
	protected DataOutputStream serverOut;
	protected ISOPackager packager;

	public ISOChannel (String host, int port, ISOPackager p) {
		this.host = host;
		this.port = port;
		this.packager = p;
	}
	public boolean isConnected() {
		return socket != null && usable;
	}
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
	public void send (ISOMsg m) throws IOException, ISOException {
		m.setPackager (packager);
		byte[] b = m.pack();
		System.out.println (
			"--[pack]--\n"+ ISOUtil.hexString(b) + "\n--[end]--");
		sendMessageLength(b.length);
		sendMessageHeader(m);
		serverOut.write(b, 0, b.length);
		sendMessageTrailer(m);
		serverOut.flush ();
	}
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
				System.out.println ("reading header len="+hLen);
				serverIn.read(b,0,hLen);
				len -= hLen;
			}
			b = new byte[len];
			System.out.println ("reading message len=" +len);
			if ((l=serverIn.read(b,0,len)) != len)
				throw new ISOException(
					"receive error. expected " +len + " received " +l);
			System.out.println (
				"--[unpack]--\n"+ ISOUtil.hexString(b) + "\n--[end]--");
		}
		else
			throw new ISOException("receive length " +len + " seems extrange");

		ISOMsg m = new ISOMsg();
		m.setPackager (packager);
		if (b.length > 0)	// Ignore NULL messages (i.e. VAP/X.25 sync, etc.)
			m.unpack (b);
		return m;
	}
	public int getBytes (byte[] b) throws IOException {
		return serverIn.read (b);
	}
	public void disconnect () throws IOException {
		usable = false;
		serverIn  = null;
		serverOut = null;
		if (socket != null)
			socket.close ();
		socket = null;
	}	
	public void reconnect() throws IOException {
		disconnect();
		connect();
	}
}
