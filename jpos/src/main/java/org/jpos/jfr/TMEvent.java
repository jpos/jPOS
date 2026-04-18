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

package org.jpos.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Category("jPOS")
@Name("jpos.TMEvent")
@StackTrace()
/** Base Java Flight Recorder event for TransactionManager lifecycle notifications. */
public class TMEvent extends Event {
    /** Transaction or participant name associated with the event. */
    @Name("name")
    protected final String name;

    /** Transaction identifier associated with the event. */
    @Name("id")
    protected final long id;

    /**
     * Creates a transaction manager JFR event.
     *
     * @param name transaction or participant name
     * @param id transaction identifier
     */
    public TMEvent(String name, long id) {
        this.name = name;
        this.id = id;
    }

    /** JFR event emitted during transaction prepare. */
    @Name("jpos.TMPrepare")
    public static class Prepare extends TMEvent {
        /**
         * Creates a prepare event.
         *
         * @param name transaction or participant name
         * @param id transaction identifier
         */
        public Prepare(String name, long id) {
            super(name, id);
        }
    }

    /** JFR event emitted during prepare-for-abort. */
    @Name("jpos.TMPrepareForAbort")
    public static class PrepareForAbort extends TMEvent {
        /**
         * Creates a prepare-for-abort event.
         *
         * @param name transaction or participant name
         * @param id transaction identifier
         */
        public PrepareForAbort(String name, long id) {
            super(name, id);
        }
    }

    /** JFR event emitted during commit. */
    @Name("jpos.TMCommit")
    public static class Commit extends TMEvent {
        /**
         * Creates a commit event.
         *
         * @param name transaction or participant name
         * @param id transaction identifier
         */
        public Commit(String name, long id) {
            super(name, id);
        }
    }

    /** JFR event emitted during abort. */
    @Name("jpos.TMAbort")
    public static class Abort extends TMEvent {
        /**
         * Creates an abort event.
         *
         * @param name transaction or participant name
         * @param id transaction identifier
         */
        public Abort(String name, long id) {
            super(name, id);
        }
    }

    /** JFR event emitted when capturing a transaction snapshot. */
    @Name("jpos.TMSnapshot")
    public static class Snapshot extends TMEvent {
        /**
         * Creates a snapshot event.
         *
         * @param name transaction or participant name
         * @param id transaction identifier
         */
        public Snapshot(String name, long id) {
            super(name, id);
        }
    }

    /** JFR event emitted when a transaction is paused. */
    @Name("jpos.TMPause")
    public static class Pause extends TMEvent {
        /**
         * Creates a pause event.
         *
         * @param name transaction or participant name
         * @param id transaction identifier
         */
        public Pause(String name, long id) {
            super(name, id);
        }
    }

    /** JFR event emitted during recovery. */
    @Name("jpos.TMRecover")
    public static class Recover extends TMEvent {
        /**
         * Creates a recovery event.
         *
         * @param name transaction or participant name
         * @param id transaction identifier
         */
        public Recover(String name, long id) {
            super(name, id);
        }
    }
}
