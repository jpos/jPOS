/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.iso;

import org.jpos.util.LogEvent;

/**
 * Receives the header and binary image of an incoming message 
 * (suitable for MAC validation)
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public interface RawIncomingFilter extends ISOFilter {
    /**
     * @param channel current ISOChannel instance
     * @param m ISOMsg to filter
     * @param header optional header 
     * @param image raw image
     * @param evt LogEvent
     * @return an ISOMsg (possibly parameter m)
     * @throws VetoException
     */
    public ISOMsg filter (ISOChannel channel, ISOMsg m, 
            byte[] header, byte[] image, LogEvent evt) 
        throws VetoException;
}

