/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

public class PausedTransaction implements Loggeable {
    private long id;
    private List members;
    private Iterator iter;
    private boolean aborting;
    private TransactionManager txnmgr;
    private boolean resumed;
    private TimerTask expirationMonitor;
    public PausedTransaction (
            TransactionManager txnmgr,
            long id, List members, Iterator iter, boolean aborting, TimerTask expirationMonitor) 
    {
        super();
        this.txnmgr = txnmgr;
        this.id = id;
        this.members = members;
        this.iter = iter;
        this.aborting = aborting;
        this.expirationMonitor = expirationMonitor;
    }
    public long id() {
        return id;
    }
    public List members() {
        return members;
    }
    public Iterator iterator() {
        return iter;
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + "id: " + id
                + (isAborting() ? " (aborting)" : ""));

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
    public synchronized void cancelExpirationMonitor() {
        if (expirationMonitor != null)
            expirationMonitor.cancel();
    }
}

