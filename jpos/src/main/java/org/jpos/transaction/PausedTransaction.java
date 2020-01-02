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

import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;
import org.jpos.util.Profiler;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

public class PausedTransaction implements Loggeable {
    private long id;
    private List<TransactionParticipant> members;
    private Iterator<TransactionParticipant> iter;
    private boolean aborting;
    private TransactionManager txnmgr;
    private boolean resumed;
    private TimerTask expirationMonitor;
    private Profiler prof;
    private LogEvent evt;
    private TransactionParticipant participant;
    public PausedTransaction (
            TransactionManager txnmgr, long id, TransactionParticipant participant, List<TransactionParticipant> members
           ,Iterator<TransactionParticipant> iter, boolean aborting
           ,TimerTask expirationMonitor, Profiler prof, LogEvent evt)
    {
        super();
        this.txnmgr = txnmgr;
        this.id = id;
        this.participant = participant;
        this.members = members;
        this.iter = iter;
        this.aborting = aborting;
        this.expirationMonitor = expirationMonitor;
        this.prof = prof;
        this.evt = evt;
    }
    public long id() {
        return id;
    }
    public List<TransactionParticipant> members() {
        return members;
    }
    public Iterator<TransactionParticipant> iterator() {
        return iter;
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + "id: " + id
                + (isAborting() ? " (aborting)" : ""));

    }

    public TransactionParticipant getParticipant() {
        return participant;
    }

    public boolean isAborting() {
        return aborting;
    }
    public void forceAbort() {
        this.aborting = true;
    }
    public TransactionManager getTransactionManager() {
        return txnmgr;
    }
    public void setResumed (boolean resumed) {
        this.resumed = resumed;
    }
    public boolean isResumed() {
        return resumed;
    }
    public Profiler getProfiler() {
        return prof;
    }
    public LogEvent getLogEvent() { return evt; }

    public synchronized void cancelExpirationMonitor() {
        if (expirationMonitor != null)
            expirationMonitor.cancel();
    }
}
