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
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;

import java.io.Serializable;

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

