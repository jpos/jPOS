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

import java.io.Serializable;

public class TransactionStatusEvent {
    int session;
    long id;
    long timestamp;
    String info;
    State state;
    Serializable context;

    public enum State {
        READY(0),
        PREPARING(1),
        PREPARING_FOR_ABORT(2),
        COMMITING(3),
        ABORTING(4),
        DONE(5),
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
        public int intValue() {
            return state;
        }
    }

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
    public int getSession() {
        return session;
    }
    public long getId() {
        return id;
    }
    public String getInfo() {
        return info;
    }
    public State getState() {
        return state;
    }
    public String getStateAsString () {
        return state.toString();
    }
    public Serializable getContext(){
        return context;
    }
}
