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

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.TransactionConstants;
import org.jpos.transaction.TransactionManager;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class Join
       implements TransactionConstants, AbortParticipant, 
                  XmlConfigurable
{
    private TransactionManager mgr;
    private List participants = new ArrayList ();

    public int prepare (long id, Serializable o) {
        return mergeActions(
            joinRunners(prepare (createRunners(id, o)))
        );
    }
    public int prepareForAbort  (long id, Serializable o) { 
        return mergeActions(
            joinRunners(prepareForAbort (createRunners(id, o)))
        );
    }
    public void commit (long id, Serializable o) { 
        joinRunners(commit (createRunners(id, o)));
    }
    public void abort  (long id, Serializable o) { 
        joinRunners(abort (createRunners(id, o)));
    }
    public void setConfiguration (Element e)
        throws ConfigurationException
    {
        Iterator iter = e.getChildren ("participant").iterator();
        while (iter.hasNext()) {
            participants.add (mgr.createParticipant ((Element) iter.next()));
        }
    }
    public void setTransactionManager (TransactionManager mgr) {
        this.mgr = mgr;
    }
    private Runner[] prepare (Runner[] runners) {
        for (Runner runner : runners) runner.prepare();
        return runners;
    }
    private Runner[] prepareForAbort (Runner[] runners) {
        for (Runner runner : runners) runner.prepareForAbort();
        return runners;
    }
    private Runner[] commit (Runner[] runners) {
        for (Runner runner : runners) runner.commit();
        return runners;
    }
    private Runner[] abort (Runner[] runners) {
        for (Runner runner : runners) runner.abort();
        return runners;
    }
    private Runner[] createRunners(long id, Serializable o) {
        Runner[] runners = new Runner[participants.size()];
        Iterator iter = participants.iterator();
        for (int i=0; iter.hasNext(); i++) {
            runners[i] = new Runner (
                (TransactionParticipant) iter.next(), id, o
            );
        }
        return runners;
    }
    private Runner[] joinRunners (Runner[] runners) {
        for (Runner runner : runners) runner.join();
        return runners;
    }
    private int mergeActions (Runner[] runners) {
        boolean prepared = true;
        boolean readonly = true;
        boolean no_join = true;
        boolean retry = false;
        for (Runner runner : runners) {
            int action = runner.rc;
            retry = (action & RETRY) == RETRY;
            if (retry)
                return RETRY;
            if ((action & PREPARED) == ABORTED)
                prepared = false;
            if ((action & READONLY) != READONLY)
                readonly = false;
            if ((action & NO_JOIN) != NO_JOIN)
                no_join = false;
        }
        return (prepared ? PREPARED : ABORTED) |
               (no_join  ? NO_JOIN  : 0) |
               (readonly ? READONLY : 0);
    }
    public static class Runner implements Runnable {
        TransactionParticipant p;
        public int rc;
        long id;
        int mode;
        Serializable ctx;
        Thread t;
        public static final int PREPARE = 0;
        public static final int PREPARE_FOR_ABORT = 1;
        public static final int COMMIT = 2;
        public static final int ABORT = 3;
        public static final String[] MODES = {
            "prepare", "prepareForAbort", "commit", "abort"
        };

        public Runner (TransactionParticipant p, long id, Serializable ctx) {
            this.p = p;
            this.id = id;
            this.ctx = ctx;
        }
        public void prepare() {
            createThread (PREPARE);
        }
        public void prepareForAbort() {
            createThread (PREPARE_FOR_ABORT);
        }
        public void commit () {
            createThread (COMMIT);
        }
        public void abort () {
            createThread (ABORT);
        }
        public void run() {
            switch (mode) {
                case PREPARE:
                    rc = p.prepare(id, ctx);
                    break;
                case PREPARE_FOR_ABORT:
                    if (p instanceof AbortParticipant)
                        rc = ((AbortParticipant)p).prepareForAbort (id, ctx);
                    break;
                case COMMIT:
                    if ((rc & NO_JOIN) == 0)
                        p.commit (id, ctx);
                    break;
                case ABORT:
                    if ((rc & NO_JOIN) == 0)
                        p.abort (id, ctx);
                    break;
            }
        }
        public void join () {
            try {
                t.join ();
            } catch (InterruptedException e) { }
        }
        private void createThread (int m) {
            this.t = new Thread(this);
            t.setName (
                MODES[m] +
                this.getClass().getName() + ":" + p.getClass().getName()
            );
            this.mode = m;
            t.start();
        }
    }
}
