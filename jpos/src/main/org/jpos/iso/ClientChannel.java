package org.jpos.iso;

/**
 * Tag this channel as a client one (from a Socket point of view)
 * <p>
 * Please note that ISOChannel implementations may choose to
 * implement ClientChannel as well as ServerChannel, being a
 * client does not mean it can not be a server too.
 * <p>
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see ISOChannel
 * @see ServerChannel
 */

public interface ClientChannel extends ISOChannel {
    /**
     * initialize an ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     */
    public void setHost(String host, int port);

    /**
     * @return hostname (may be null)
     */
    public String getHost();

    /**
     * @return port number (may be 0)
     */
    public int getPort();
}

