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
import org.jpos.core.ConfigurationException;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

@SuppressWarnings("unchecked")
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

