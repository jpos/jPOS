package org.jpos.iso;

import java.io.IOException;
import org.jpos.iso.ISOFilter.VetoException;

/**
 * Source for an ISORequest (where to send a reply)
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 * @see ISORequestListener
 */
public interface ISOSource {
    /**
     * sends (or hands back) an ISOMsg
     * @param m the Message to be sent
     * @exception IOException
     * @exception ISOException
     * @exception ISOFilter.VetoException;
     */
    public void send (ISOMsg m) 
	throws IOException, ISOException, VetoException;
}

