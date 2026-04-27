/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

/**
 * Transaction participant that runs a list of nested participants concurrently
 * (one virtual thread each) and merges their lifecycle results.
 */
@SuppressWarnings("unchecked")
public class Join
       implements TransactionConstants, AbortParticipant,
                  XmlConfigurable
{
    /** Default constructor; no instance state to initialise. */
    public Join() {}
    private TransactionManager tm;
    private final List<TransactionParticipant> participants = new ArrayList<> ();

    /**
     * Runs {@code prepare} on every nested participant in parallel and merges their results.
     *
     * @param id transaction id
     * @param o transaction context
     * @return the merged action mask
     */
    public int prepare (long id, Serializable o) {
        return mergeActions(
            joinRunners(prepare (createRunners(id, o)))
        );
    }
    /**
     * Runs {@code prepareForAbort} on every nested participant in parallel and merges their results.
     *
     * @param id transaction id
     * @param o transaction context
     * @return the merged action mask
     */
    public int prepareForAbort  (long id, Serializable o) {
        return mergeActions(
            joinRunners(prepareForAbort (createRunners(id, o)))
        );
    }
    /**
     * Runs {@code commit} on every nested participant in parallel.
     *
     * @param id transaction id
     * @param o transaction context
     */
    public void commit (long id, Serializable o) {
        joinRunners(commit (createRunners(id, o)));
    }
    /**
     * Runs {@code abort} on every nested participant in parallel.
     *
     * @param id transaction id
     * @param o transaction context
     */
    public void abort  (long id, Serializable o) {
        joinRunners(abort (createRunners(id, o)));
    }
    /**
     * Reads {@code <participant>} children from {@code e} and instantiates each via the
     * associated {@link TransactionManager}.
     *
     * @param e XML configuration element
     * @throws ConfigurationException if any nested participant fails to instantiate
     */
    public void setConfiguration (Element e) throws ConfigurationException {
        for (Element element : e.getChildren("participant")) {
            participants.add(tm.createParticipant(element));
        }
    }
    /**
     * Captures the {@link TransactionManager} used to construct nested participants.
     *
     * @param mgr the hosting transaction manager
     */
    public void setTransactionManager (TransactionManager mgr) {
        this.tm = mgr;
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
        Iterator<TransactionParticipant> iter = participants.iterator();
        for (int i=0; iter.hasNext(); i++) {
            runners[i] = new Runner (
              iter.next(), id, o
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
    /**
     * Wraps a single nested {@link TransactionParticipant} in its own virtual thread
     * so its lifecycle calls can run concurrently with siblings.
     */
    public static class Runner implements Runnable {
        private TransactionParticipant p;
        /** Result code returned by the most recent lifecycle call. */
        public int rc;
        long id;
        int mode;
        private Serializable ctx;
        Thread t;
        /** Mode constant: prepare. */
        public static final int PREPARE = 0;
        /** Mode constant: prepareForAbort. */
        public static final int PREPARE_FOR_ABORT = 1;
        /** Mode constant: commit. */
        public static final int COMMIT = 2;
        /** Mode constant: abort. */
        public static final int ABORT = 3;
        /** Human-readable labels indexed by mode constant. */
        public static final String[] MODES = {
            "prepare", "prepareForAbort", "commit", "abort"
        };

        private String threadName;

        /**
         * Constructs a Runner for the given participant and transaction.
         *
         * @param p nested participant to invoke
         * @param id transaction id
         * @param ctx transaction context
         */
        public Runner (TransactionParticipant p, long id, Serializable ctx) {
            this.p = p;
            this.id = id;
            this.ctx = ctx;
        }
        /** Schedules the participant's {@code prepare} call on a virtual thread. */
        public void prepare() {
            createThread (PREPARE);
        }
        /** Schedules the participant's {@code prepareForAbort} call on a virtual thread. */
        public void prepareForAbort() {
            createThread (PREPARE_FOR_ABORT);
        }
        /** Schedules the participant's {@code commit} call on a virtual thread. */
        public void commit () {
            createThread (COMMIT);
        }
        /** Schedules the participant's {@code abort} call on a virtual thread. */
        public void abort () {
            createThread (ABORT);
        }
        /** Invokes the appropriate lifecycle method on the wrapped participant. */
        public void run() {
            switch (mode) {
                case PREPARE -> rc = p.prepare(id, ctx);
                case PREPARE_FOR_ABORT -> {
                    if (p instanceof AbortParticipant)
                        rc = ((AbortParticipant) p).prepareForAbort(id, ctx);
                }
                case COMMIT -> {
                    if ((rc & NO_JOIN) == 0)
                        p.commit(id, ctx);
                }
                case ABORT -> {
                    if ((rc & NO_JOIN) == 0)
                        p.abort(id, ctx);
                }
            }
        }
        /** Waits for the runner's virtual thread to terminate, swallowing interrupts. */
        public void join () {
            try {
                t.join ();
            } catch (InterruptedException ignored) { }
        }
        private void createThread (int m) {
            this.mode = m;
            this.t = Thread.ofVirtual().name(
              "%s%s:%s".formatted(
                MODES[mode],
                this.getClass().getName(),
                p.getClass().getName()
              )
            ).start(this);
        }
    }
}
