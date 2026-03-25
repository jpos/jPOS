/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.iso;

import java.io.IOException;

/**
 * allows the transmision and reception of ISO 8583 Messages
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:alwynschoeman@yahoo.com">Alwyn Schoeman</a>
 * @version $Revision$ $Date$
 */
public interface ISOChannel extends ISOSource {
    /** Counter index for connection events. */
    int CONNECT      = 0;
    /** Counter index for transmitted messages. */
    int TX           = 1;
    /** Counter index for received messages. */
    int RX           = 2;
    /** Number of counters maintained by this channel. */
    int SIZEOF_CNT   = 3;

    /**
     * Associate a packager with this channel
     * @param p     an ISOPackager
     */
    void setPackager(ISOPackager p);

    /**
     * Connects ISOChannel 
     * @exception IOException on I/O failure
     */
    void connect() throws IOException;

    /**
     * disconnects ISOChannel
     * @exception IOException on I/O failure
     */
    void disconnect() throws IOException;

    /**
     * Reconnect channel
     * @exception IOException on I/O failure
     */
    void reconnect() throws IOException;

    /**
     * @return true if Channel is connected and usable
     */
    boolean isConnected();

    /**
     * Receives an ISOMsg
     * @return the Message received
     * @exception IOException on I/O failure
     * @exception ISOException on ISO packing/unpacking failure
     */
    ISOMsg receive() throws IOException, ISOException;

    /**
     * sends an ISOMsg over the TCP/IP session
     * @param m the Message to be sent
     * @exception IOException on I/O failure
     * @exception ISOException on ISO packing/unpacking failure
     */
    void send(ISOMsg m) throws IOException, ISOException;
    
    /**
     * sends a byte[] over the TCP/IP session
     * @param b the byte array to be sent
     * @exception IOException on I/O failure
     * @exception ISOException on ISO packing/unpacking failure
     */
    void send(byte[] b) throws IOException, ISOException;

    /**
     * Marks this channel as usable or not; a non-usable channel will not accept messages.
     * @param b - usable state
     */
    void setUsable(boolean b);

    /**
     * associates this ISOChannel with a name on NameRegistrar
     * @param name name to register
     * @see org.jpos.util.NameRegistrar
     */
    void setName(String name);

   /**
    * Returns this channel's registered name.
    * @return this ISOChannel's name ("" if no name was set)
    */
   String getName();

   /**
    * Returns the packager used to pack/unpack ISO messages on this channel.
    * @return current packager
    */
   ISOPackager getPackager();

   /**
    * Returns a clone of this channel.
    * @return a deep copy of this ISOChannel
    */
   Object clone();
    
}

