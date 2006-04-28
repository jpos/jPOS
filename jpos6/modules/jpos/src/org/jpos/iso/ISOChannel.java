/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
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
    public static final int CONNECT      = 0;
    public static final int TX           = 1;
    public static final int RX           = 2;
    public static final int SIZEOF_CNT   = 3;

    /**
     * Associate a packager with this channel
     * @param p     an ISOPackager
     */
    public void setPackager(ISOPackager p);

    /**
     * Connects ISOChannel 
     * @exception IOException
     */
    public void connect () throws IOException;

    /**
     * disconnects ISOChannel
     * @exception IOException
     */
    public void disconnect () throws IOException;

    /**
     * Reconnect channel
     * @exception IOException
     */
    public void reconnect() throws IOException;

    /**
     * @return true if Channel is connected and usable
     */
    public boolean isConnected();

    /**
     * Receives an ISOMsg
     * @return the Message received
     * @exception IOException
     * @exception ISOException
     */
    public ISOMsg receive() throws IOException, ISOException;

    /**
     * sends an ISOMsg over the TCP/IP session
     * @param m the Message to be sent
     * @exception IOException
     * @exception ISOException
     */
    public void send (ISOMsg m) throws IOException, ISOException;

    /**
     * @param b - usable state
     */
    public void setUsable(boolean b);

    /**
     * associates this ISOChannel with a name on NameRegistrar
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName (String name);

   /**
    * @return this ISOChannel's name ("" if no name was set)
    */
    public String getName();

   /**
    * @return current packager
    */
    public ISOPackager getPackager();
    
}

