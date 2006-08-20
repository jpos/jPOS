package org.jpos.transaction;

import java.io.Serializable;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;

public class TestRetryParticipant implements TransactionParticipant {
    Configuration cfg;
    public int prepare (long id, Serializable o) { 
        Context ctx = (Context) o;
        Integer ii = (Integer) ctx.get ("RETRY");
        if (ii != null) {
            if (ii.intValue() > 0) {
                ctx.log ("retry " + ii.intValue());
                ctx.put ("RETRY", new Integer (ii.intValue()-1));
                return RETRY;
            }
        }
        return PREPARED | READONLY | NO_JOIN;
    }
    public void commit (long id, Serializable o) { }
    public void abort  (long id, Serializable o) { }
}

