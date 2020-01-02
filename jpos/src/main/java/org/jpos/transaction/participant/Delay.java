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

import java.io.Serializable;
import java.util.Random;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.transaction.TransactionParticipant;

public class Delay implements TransactionParticipant, Configurable {
    long prepareDelay = 0L;
    long commitDelay = 0L;
    long abortDelay = 0L;
    Random random;
    public int prepare(long id, Serializable context) {
        sleep (prepareDelay);
        return PREPARED;
    }

    public void commit(long id, Serializable context) {
        sleep (commitDelay);
    }

    public void abort(long id, Serializable context) {
        sleep (abortDelay);
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        prepareDelay = cfg.getLong ("prepare-delay");
        commitDelay  = cfg.getLong ("commit-delay");
        abortDelay   = cfg.getLong ("abort-delay");
        random       = cfg.getBoolean ("random") ? new Random() : null;
    }
    void sleep (long delay) {
        if (delay > 0L) {
            try {
                Thread.sleep (random != null ? (long)random.nextDouble()*delay : delay);
            } catch (InterruptedException ignored) { }
        }
    }
}
