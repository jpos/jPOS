package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ISOChannel implementation suitable for OASIS Ltd &copy; hosts<br>
 * (four ASCII characters header indicating message length)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class ASCIIChannel extends ISOChannel {
	/**
	 * Public constructor (used by Class.forName("...").newInstance())
	 */
	public ASCIIChannel () {
		super();
	}
	/**
	 * Construct client ISOChannel
	 * @param host	server TCP Address
	 * @param port  server port number
	 * @param p     an ISOPackager
	 * @see ISOPackager
	 */
	public ASCIIChannel (String host, int port, ISOPackager p) {
		super(host, port, p);
	}
	/**
	 * Construct server ISOChannel
	 * @param p     an ISOPackager
	 * @exception IOException
	 * @see ISOPackager
	 */
	public ASCIIChannel (ISOPackager p) throws IOException {
		super(p);
	}
	/**
	 * constructs a server ISOChannel associated with a Server Socket
	 * @param p     an ISOPackager
	 * @param serverSocket where to accept a connection
	 * @exception IOException
	 * @see ISOPackager
	 */
	public ASCIIChannel (ISOPackager p, ServerSocket serverSocket) 
		throws IOException
	{
		super(p, serverSocket);
	}
	/**
	 * @param len the packed Message len
	 * @exception IOException
	 */
	protected void sendMessageLength(int len) throws IOException {
		if (len > 9999)
			throw new IOException ("len exceeded");

		try {
			serverOut.write(
				ISOUtil.zeropad(Integer.toString(len), 4).getBytes()
			);
		} catch (ISOException e) { }
	}
	/**
	 * @return the Message len
	 * @exception IOException, ISOException
	 */
	protected int getMessageLength() throws IOException, ISOException {
		int l = 0;
		byte[] b = new byte[4];
		while (l == 0) {
			serverIn.readFully(b,0,4);
			try {
				if ((l=Integer.parseInt(new String(b))) == 0) {
					serverOut.write(b);
					serverOut.flush();
				}
			} catch (NumberFormatException e) { 
				throw new ISOException ("Invalid header len "+new String(b));
			}
		}
		return l;
	}
}
