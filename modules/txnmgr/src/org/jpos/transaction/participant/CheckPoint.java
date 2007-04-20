/**
 * Copyright (c) 2007 Alejandro Revilla and Contributors
 * jPOS.org (http://jpos.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jpos.transaction.participant;

import java.io.Serializable;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

public class CheckPoint implements TransactionParticipant, Configurable  {
    Configuration cfg;
    public int prepare (long id, Serializable o) {
        if (o instanceof Context) 
            ((Context)o).checkPoint (cfg.get ("message", "checkpoint"));

        return PREPARED | NO_JOIN | READONLY;
    }
    public void commit (long id, Serializable o) { }
    public void abort  (long id, Serializable o) { }
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }
}

