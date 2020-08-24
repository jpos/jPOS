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

package org.jpos.transaction;

import org.jpos.iso.ISOUtil;
import java.io.Serializable;

public class TestPauseParticipant implements TransactionParticipant {
    TransactionManager txnmgr;

    public int prepare (long id, Serializable o) { 
        final Context ctx = (Context) o;
        new Thread(() -> {
            ctx.getPausedTransaction(200).forceAbort();
            // ISOUtil.sleep (1000);
            ctx.resume();
        }).start();
        ISOUtil.sleep(100); // force race condition
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

