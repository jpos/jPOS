/**
 * ISOChannel implementation - VISA's VAP framing
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:36  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class VAPChannel extends ISOChannel {
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

	public static void main (String args[]) {
		System.out.println ("ISOChannel running");
		ISOChannel channel=new VAPChannel
			(args[0], Integer.parseInt(args[1]), new ISO87BPackager());
		try {
			ISOMsg m = new ISOMsg ();
			m.set(new ISOField (0,  "0800"));
			m.set(new ISOField (3,  "000001"));
			channel.connect();
			channel.send(m);
			// ISOMsg d = channel.receive();
			// d.dump(System.out, "");
			channel.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ISOException e) {
			e.printStackTrace();
		}
	}
}
