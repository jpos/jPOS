/**
 * ISOChannel implementation - RAW Channel
 * Transmite largo en un long nbo, el resto es 'raw'.
 * Incluye TPDU opcional.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */

/*
 * $Log$
 * Revision 1.1  1998/12/14 22:48:23  apr
 * Added RawChannel support
 * Pruebas OK packaging POSNet
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class RawChannel extends ISOChannel {
	byte[] TPDU;
	public RawChannel (String host, int port, ISOPackager p, byte[] TPDU) {
		super(host, port, p);
		this.TPDU = TPDU;
	}
	protected void sendMessageLength(int len) throws IOException {
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
		return (int) (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];
	}
	protected void sendMessageHeader(ISOMsg m) throws IOException { 
		serverOut.write(TPDU);
	}
	protected int getHeaderLength() { 
		return TPDU.length;
	}
}
