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
	 * Construct client ISOChannel
	 * @param host	server TCP Address
	 * @param port  server port number
	 * @param p     an ISOPackager (should be ISO87BPackager)
	 * @param hlen  the header len
	 * @see ISO87BPackager
	 */
	private int headerLen = 0;
	public VAPChannel (String host, int port, ISOPackager p, int hLen) {
		super(host, port, p);
		this.headerLen = hLen;
	}
	/**
	 * Construct server ISOChannel
	 * @param p     an ISOPackager (should be ISO87BPackager)
	 * @param hlen  the header len
	 * @exception IOException
	 * @see ISO87BPackager
	 */
	public VAPChannel (ISOPackager p, int hLen) throws IOException {
		super(p);
		this.headerLen = hLen;
	}
	protected void sendMessageLength(int len) throws IOException {
		serverOut.write (len >> 8);
		serverOut.write (len);
		serverOut.write (0);
		serverOut.write (0);
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
				System.out.println ("VAP Poll received (and answered)");
			}
		}
		return l;
	}
	protected int getHeaderLength() { return headerLen; }
}
