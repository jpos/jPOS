package org.jpos.transaction;

import java.io.Serializable;
import org.jpos.iso.ISOUtil;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;

public class TestPauseParticipant implements TransactionParticipant {
    Configuration cfg;
    TransactionManager txnmgr;

    public int prepare (long id, Serializable o) { 
        final Context ctx = (Context) o;
        new Thread() {
            public void run() {
                ISOUtil.sleep (1000);
                txnmgr.queue (ctx);   // re-inject paused transaction
            }
        }.start();
        return PREPARED | PAUSE;
    }
    public void commit (long id, Serializable o) { 
        Context ctx = (Context) o;
        ctx.log ("TestPausedParticipant commit has been called, id: " + id);
    }
    public void abort  (long id, Serializable o) { 
        Context ctx = (Context) o;
        ctx.log ("TestPausedParticipant abort has been called, id:" + id);
    }
    public void setTransactionManager (TransactionManager txnmgr) {
        this.txnmgr = txnmgr;
    }
}

