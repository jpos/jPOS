/**
 * ISOChannel implementation - BASE24 X.25 framing
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:04  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class BASE24Channel extends ISOChannel {
	public BASE24Channel (String host, int port, ISOPackager p) {
		super(host, port, p);
	}
	protected void sendMessageTrailer(ISOMsg m) throws IOException {
		serverOut.write (3);
	}
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
	public static void main (String args[]) {
		System.out.println ("ISOChannel running");
		ISOChannel channel=new BASE24Channel
			(args[0], Integer.parseInt(args[1]), new ISO87APackager());
		try {
			ISOMsg m = new ISOMsg ();
			m.set(new ISOField (0,  "0800"));
			m.set(new ISOField (3,  "000001"));
			m.set(new ISOField (7,  ISODate.getDateTime(new Date())));
			m.dump(System.out, "");
			channel.connect();
			channel.send(m);
			ISOMsg d = channel.receive();
			d.dump(System.out, "");
			channel.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ISOException e) {
			e.printStackTrace();
		}
	}
}
