package org.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;
import org.jpos.iso.ISOFilter.VetoException;

/**
 * allows the transmision and reception of ISO 8583 Messages
 * <p>
 * It is not necessarily thread-safe, 
 * ISOMUX or higher level classes should take care of 
 * the synchronization details
 * <p>
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */

public interface ISOChannel {
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
     * sends an ISOMsg
     * @param m the Message to be sent
     * @exception IOException
     * @exception ISOException
     * @exception ISOFilter.VetoException;
     */
    public void send (ISOMsg m) 
	throws IOException, ISOException, VetoException;

    /**
     * @param b - usable state
     */
    public void setUsable(boolean b);

    /**
     * get this channel counters (TX/RX/CONNECT)
     * @return counters
     */
    public int[] getCounters();

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

