package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Implements an ISOChannel able to exchange messages with
 * ACI's BASE24 over an X.25 link.<br>
 * An instance of this class exchange messages by means of an
 * intermediate 'port server' as described in the
 * <a href="API_users_guide.html#LowLevelAdapters">API Users Guide</a>
 * @author apr@cs.com.uy
 *
 * @version $Id$
 *
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class BASE24Channel extends ISOChannel {
	/**
	 * @param host	server TCP Address
	 * @param port  server port number
	 * @param p     an ISOPackager
	 * @see ISOPackager
	 */
	public BASE24Channel (String host, int port, ISOPackager p) {
		super(host, port, p);
	}
	/**
	 * @param m	the Message to send (in this case it is unused)
	 * @exception IOException
	 */
	protected void sendMessageTrailer(ISOMsg m) throws IOException {
		serverOut.write (3);
	}
	/**
	 * @return a byte array with the received message
	 * @exception IOException
	 */
	protected byte[] streamReceive() throws IOException {
		int i;
		byte[] buf = new byte[4096];
		for (i=0; i<4096; i++) {
			int c = -1;
			try {
				c = serverIn.read();
			} catch (SocketException e) { }
			if (c == 03)
				break;
			else if (c == -1)
				throw new IOException("connection closed");
			buf[i] = (byte) c;
		}
		if (i == 4096)
			throw new IOException("packet too long");

		byte[] d = new byte[i];
	    System.arraycopy(buf, 0, d, 0, i);
		return d;
	}
}
