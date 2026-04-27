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

package org.jpos.transaction;

import java.io.Serializable;

/** Event describing a transaction status transition. */
public class TransactionStatusEvent {
    int session;
    long id;
    long timestamp;
    String info;
    State state;
    Serializable context;

    /** Enumeration of transaction lifecycle states. */
    public enum State {
        /** Transaction is ready for processing. */
        READY(0),
        /** Transaction is in the prepare phase. */
        PREPARING(1),
        /** Transaction is preparing for abort. */
        PREPARING_FOR_ABORT(2),
        /** Transaction is committing. */
        COMMITING(3),
        /** Transaction is aborting. */
        ABORTING(4),
        /** Transaction processing is complete. */
        DONE(5),
        /** Transaction is paused. */
        PAUSED(6);

        int state;
        String[] stateAsString = new String[] {
            "Ready", "Preparing", "Preparing for abort", "Commiting", "Aborting", "Done", "Paused"
        };
        State (int state) {
            this.state = state;
        }
        public String toString () {
            return stateAsString [state];
        }
        /**
         * Returns the numeric representation of the state.
         *
         * @return integer state code
         */
        public int intValue() {
            return state;
        }
    }

    /**
     * Creates a transaction status event.
     *
     * @param session session identifier
     * @param state transaction state
     * @param id transaction identifier
     * @param info human-readable status information
     * @param context transaction context when available
     */
    public TransactionStatusEvent (int session, State state, long id, String info, Serializable context) {
        super();
        this.session = session;
        this.state = state;
        this.id = id;
        this.info = info;
        this.context = context;
        timestamp = System.nanoTime();
    }
    public String toString() {
        return String.format("%02d %08d %s %s", session, id, state.toString(), info);
    }
    /**
     * Returns the session identifier.
     *
     * @return session identifier
     */
    public int getSession() {
        return session;
    }
    /**
     * Returns the transaction identifier.
     *
     * @return transaction identifier
     */
    public long getId() {
        return id;
    }
    /**
     * Returns the status information string.
     *
     * @return status information
     */
    public String getInfo() {
        return info;
    }
    /**
     * Returns the transaction state.
     *
     * @return current state
     */
    public State getState() {
        return state;
    }
    /**
     * Returns the transaction state as display text.
     *
     * @return state name
     */
    public String getStateAsString () {
        return state.toString();
    }
    /**
     * Returns the transaction context.
     *
     * @return transaction context
     */
    public Serializable getContext(){
        return context;
    }
}
