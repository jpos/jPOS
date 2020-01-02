/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.transaction.participant;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;

import java.io.Serializable;

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

