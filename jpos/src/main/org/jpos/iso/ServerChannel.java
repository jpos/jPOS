package org.jpos.iso;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Tag this channel as a server one (from a Socket point of view)
 * <p>
 * Please note that ISOChannel implementations may choose to
 * implement ClientChannel as well as ServerChannel, being a
 * client does not mean it can not be a server too.
 * <p>
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see ISOChannel
 * @see ClientChannel
 */

public interface ServerChannel extends ISOChannel {
   /**
    * Accepts connection 
    * @exception IOException
    */
    public void accept(ServerSocket s) throws IOException;
}

