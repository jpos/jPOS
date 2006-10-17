/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction.participant;

import java.io.Serializable;
import org.jpos.transaction.Context;
import org.jpos.transaction.AbortParticipant;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;

public class Trace implements AbortParticipant, Configurable {
    String trace;
    public int prepare (long id, Serializable o) {
        Context ctx = (Context) o;
        ctx.checkPoint ("prepare:" + trace);
        return PREPARED | READONLY;
    }
    public void commit (long id, Serializable o) { 
        Context ctx = (Context) o;
        ctx.checkPoint ("commit:" + trace);
    }
    public void abort  (long id, Serializable o) { 
        Context ctx = (Context) o;
        ctx.checkPoint ("abort:" + trace);
    }
    public int prepareForAbort (long id, Serializable o) { 
        Context ctx = (Context) o;
        ctx.checkPoint ("prepareForAbort:" + trace);
        return PREPARED | READONLY;
    }
    public void setConfiguration (Configuration cfg) {
        this.trace = cfg.get ("trace", this.getClass().getName());
    }
}

