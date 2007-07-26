/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso.filter;

import org.jpos.core.Configuration;
import org.jpos.core.Configurable;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.util.LogEvent;
import org.jpos.util.ThroughputControl;

public class ThroughputControlFilter implements ISOFilter, Configurable {
    ThroughputControl tc;
    public ThroughputControlFilter () {
        super();
        tc = null;
    }

   /**
    * @param cfg
    * <ul>
    *  <li>transactions</li>
    *  <li>period (in millis)</li>
    * </ul>
    */
    public void setConfiguration (Configuration cfg) {
        tc = new ThroughputControl (cfg.getInts ("transactions"), cfg.getInts ("period"));
    }

    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
    {
        if (tc != null) {
            long delay = tc.control ();
            if (delay > 0L)
                evt.addMessage ("ThroughputControl=" + delay);
        }
        return m;
    }
}

