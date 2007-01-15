/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction.participant;

import java.io.Serializable;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;

/**
 * HasEntry is a general purpose GroupSelector that can be used to
 * verify that a given object is available in the context.
 * It checks the Context for the given entry ('name' property) and returns
 * the content of the 'yes' or 'no' properties as the group list.
 * If "yes" or "no" is not specified, it returns the constant UNKNOWN
 */
public class HasEntry implements GroupSelector, Configurable {
    private Configuration cfg;
    public static final String YES = "yes";
    public static final String NO  = "no";
    public static final String UNKNOWN = "UNKNOWN";
    public int prepare (long id, Serializable o) {
        return PREPARED | NO_JOIN | READONLY;
    }
    public String select (long id, Serializable ser) {
        Context ctx = (Context) ser;
        String name = cfg.get ("name");
        String action = ctx.get (name) != null ? YES : NO;
        return cfg.get (action, UNKNOWN);
    }
    public void commit (long id, Serializable o) { }
    public void abort  (long id, Serializable o) { }
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }
}

