package org.jpos.transaction;

import java.io.Serializable;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;

public class SwitchParticipant implements Configurable, GroupSelector {
    Configuration cfg;
    public String select (long id, Serializable context) {
        return cfg.get ("switch", null);
    }
    public int prepare (long id, Serializable o) { 
        return PREPARED | READONLY | NO_JOIN;
    }
    public void commit (long id, Serializable o) { }
    public void abort  (long id, Serializable o) { }
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }
}

