/**
 * ISOChannel implementation - CS standard 'new isolib' Chennel
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:06  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class CSChannel extends ISOChannel {
	public CSChannel (String host, int port, ISOPackager p) {
		super(host, port, p);
	}
	protected void sendMessageLength(int len) throws IOException {
		serverOut.write (len >> 24);
		serverOut.write (len >> 16);
		serverOut.write (len >> 8);
		serverOut.write (len);
	}
	protected int getMessageLength() throws IOException, ISOException {
		byte[] b = new byte[4];
		if (serverIn.read(b,0,4) != 4)
			throw new ISOException("error reading message length");
		return (int) (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];
	}
}
