/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

/**
 * Channels that can use socket factories need to implement this.
 *
 * @author <a href="mailto:alwynschoeman@yahoo.com">Alwyn Schoeman</a>
 * @version $Revision$ $Date$
 */

public interface FactoryChannel {
    /**
     * @param sfac a socket factory
     */
    public void setSocketFactory(ISOClientSocketFactory sfac);
}
