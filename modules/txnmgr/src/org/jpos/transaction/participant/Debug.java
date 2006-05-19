/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction.participant;

import java.io.Serializable;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.transaction.Context;
import org.jpos.transaction.AbortParticipant;

public class Debug extends Log implements AbortParticipant {
    public int prepare (long id, Serializable o) {
        // Logger.log (createEvent ("prepare", id, (Context) o));
        return PREPARED | READONLY;
    }
    public void commit (long id, Serializable o) { 
        Logger.log (createEvent ("commit", id, (Context) o));
    }
    public void abort  (long id, Serializable o) { 
        Logger.log (createEvent ("abort", id, (Context) o));
    }
    public int prepareForAbort (long id, Serializable o) { 
        // Logger.log (createEvent ("prepare-for-abort", id, (Context) o));
        return PREPARED | READONLY;
    }
    private LogEvent createEvent (String action, long id, Context ctx) {
        LogEvent evt = createLogEvent (action);
        evt.addMessage ("<id>" + id + "</id>");
        evt.addMessage (ctx);
        return evt;
    }
}

