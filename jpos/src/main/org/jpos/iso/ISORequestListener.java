package org.jpos.iso;

/**
 * It is possible to ask ISOMUX to forward all unmatched
 * messages received through its associated ISOChannel
 * to be processed by an ISORequestListener.
 * 
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMUX#setISORequestListener
 */
public interface ISORequestListener {
    /**
     * @param  source source where you optionally can reply
     * @param  m   the unmatched request
     * @returns true if request was handled by this listener
     */
    public boolean process (ISOSource source, ISOMsg m);
}

