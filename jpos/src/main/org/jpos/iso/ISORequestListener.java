package org.jpos.iso;

import java.util.*;

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
     * @param   m   the unmatched request
     */
    public void process(ISOMsg m);
    public void addObserver(Observer o);
}
