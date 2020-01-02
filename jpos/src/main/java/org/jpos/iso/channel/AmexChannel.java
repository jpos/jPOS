/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.channel;

import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * ISOChannel implementation - American Express
 * 
 * @author marksalter@dsl.pipex.com
 * @version $Id: AmexChannel.java,v 1.5 2006/01/27 10:36:18 mark Exp $
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */
public class AmexChannel extends BaseChannel {

	/**
	 * Public constructor (used by Class.forName("...").newInstance())
	 */
	public AmexChannel() {
		super();
	}

	/**
	 * Construct client ISOChannel
	 * 
	 * @param host
	 *            server TCP Address
	 * @param port
	 *            server port number
	 * @param p
	 *            an ISOPackager (should be ISO87BPackager)
	 * @see org.jpos.iso.packager.ISO87BPackager
	 */
	public AmexChannel(String host, int port, ISOPackager p) {
		super(host, port, p);
	}

	/**
	 * Construct server ISOChannel
	 * 
	 * @param p
	 *            an ISOPackager (should be ISO87BPackager)
	 * @exception IOException
	 * @see org.jpos.iso.packager.ISO87BPackager
	 */
	public AmexChannel(ISOPackager p) throws IOException {
		super(p);
	}

	/**
	 * constructs a server ISOChannel associated with a Server Socket
	 * 
	 * @param p
	 *            an ISOPackager
	 * @param serverSocket
	 *            where to accept a connection
	 * @exception IOException
	 * @see ISOPackager
	 */
	public AmexChannel(ISOPackager p, ServerSocket serverSocket)
			throws IOException {
		super(p, serverSocket);
	}

	protected void sendMessageLength(int len) throws IOException {
		serverOut.write(len+2 >> 8);
		serverOut.write(len+2);
	}

	protected int getMessageLength() throws IOException, ISOException {
		int l = 0;
		byte[] b = new byte[2];
		// ignore polls (0 message length)
		while (l == 0) {
			serverIn.readFully(b, 0, 2);
			l = ((int) b[0] & 0xFF) << 8 | (int) b[1] & 0xFF;
			if (l == 0) {
				serverOut.write(b);
				serverOut.flush();
				Logger.log(new LogEvent(this, "poll"));
			}
		}
		// Message length includes length itself, so adjust the message total down by 2
		l = l - 2;

		return l;
	}

}
