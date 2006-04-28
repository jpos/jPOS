/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.util;

import java.util.Iterator;

/**
 * A specific log listener that filters all LogEvents that doesn't
 * contain any exception.
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class ExceptionLogFilter implements LogListener {
    public ExceptionLogFilter () {
        super();
    }
    public synchronized LogEvent log (LogEvent evt) {
        Iterator iter = evt.getPayLoad().iterator();
        while (iter.hasNext()) {
            if (iter.next() instanceof Throwable)
                return evt;
        }
        return null;
    }
}

