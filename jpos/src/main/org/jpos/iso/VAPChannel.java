package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

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
	 * @param host	server TCP Address
	 * @param port  server port number
	 * @param p     an ISOPackager (should be ISO87BPackager)
	 * @see ISO87BPackager
	 */
	public VAPChannel (String host, int port, ISOPackager p) {
		super(host, port, p);
	}
	protected void sendMessageLength(int len) throws IOException {
		serverOut.write (len >> 8);
		serverOut.write (len);
		serverOut.write (0);
		serverOut.write (0);
	}
	protected int getMessageLength() throws IOException, ISOException {
		byte[] b = new byte[4];
		if (serverIn.read(b,0,4) != 4)
			throw new ISOException("error reading message length");
		// return (int) (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];
		return (int) (b[0] << 8) | (b[1]);
	}
}
