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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.ConcurrentUtil;

public class Pause implements TransactionParticipant, Configurable {
    private long timeout = 0L;
    private ScheduledThreadPoolExecutor executor = ConcurrentUtil.newScheduledThreadPoolExecutor();

    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        executor.schedule(new Resumer(ctx), timeout, TimeUnit.MILLISECONDS);
        return PREPARED | PAUSE | NO_JOIN | READONLY;
    }

    public void setConfiguration(Configuration cfg) {
        timeout = cfg.getLong ("timeout");
    }

    static class Resumer implements Runnable {
        Context ctx;
        public Resumer(Context ctx) {
            this.ctx = ctx;
        }
        @Override
        public void run () {
            ctx.resume();
        }
    }
}
