/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
public class TMEvent extends Event {
    @Name("name")
    protected final String name;

    @Name("id")
    protected final long id;

    public TMEvent(String name, long id) {
        this.name = name;
        this.id = id;
    }
    
    @Name("jpos.TMPrepare")
    public static class Prepare extends TMEvent {
        public Prepare(String name, long id) {
            super(name, id);
        }
    }

    @Name("jpos.TMPrepareForAbort")
    public static class PrepareForAbort extends TMEvent {
        public PrepareForAbort(String name, long id) {
            super(name, id);
        }
    }

    @Name("jpos.TMCommit")
    public static class Commit extends TMEvent {
        public Commit(String name, long id) {
            super(name, id);
        }
    }

    @Name("jpos.TMAbort")
    public static class Abort extends TMEvent {
        public Abort(String name, long id) {
            super(name, id);
        }
    }

    @Name("jpos.TMSnapshot")
    public static class Snapshot extends TMEvent {
        public Snapshot(String name, long id) {
            super(name, id);
        }
    }

    @Name("jpos.TMPause")
    public static class Pause extends TMEvent {
        public Pause(String name, long id) {
            super(name, id);
        }
    }
    @Name("jpos.TMRecover")
    public static class Recover extends TMEvent {
        public Recover(String name, long id) {
            super(name, id);
        }
    }

}
