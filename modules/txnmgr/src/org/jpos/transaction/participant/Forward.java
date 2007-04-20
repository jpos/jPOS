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
import org.jpos.core.ConfigurationException;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;

public class Forward implements TransactionParticipant, Configurable  {
    Space sp;
    String queue;
    long timeout;
    public int prepare (long id, Serializable o) {
        sp.out (queue, o, timeout);
        return PREPARED | READONLY | NO_JOIN;
    }
    public void commit (long id, Serializable o) { }
    public void abort  (long id, Serializable o) { }

    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        sp = SpaceFactory.getSpace(cfg.get ("space", ""));
        queue = cfg.get ("queue", null);
        if (queue == null)
            throw new ConfigurationException ("Unspecified queue");
        timeout = cfg.getLong ("timeout", 60000L);
    }
}

