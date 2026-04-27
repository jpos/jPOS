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

package org.jpos.util;

import java.io.PrintStream;
import java.io.Serializable;

/** An immutable, serializable snapshot of a {@link LogEvent}. */
public class FrozenLogEvent extends LogEvent implements Serializable {
    /** The pre-rendered string representation of this frozen event. */
    private String frozen;

    /**
     * Creates a FrozenLogEvent from a pre-rendered string.
     * @param frozen the pre-rendered log event string
     */
    public FrozenLogEvent(String frozen) {
        this.frozen = frozen;
    }
    /**
     * Creates a FrozenLogEvent by capturing the current dump of the given LogEvent.
     * @param evt the LogEvent to freeze
     */
    public FrozenLogEvent (LogEvent evt) {
        super(evt.getSource(), evt.getTag(), evt.getRealm());
        frozen = evt.toString();
    }
    @Override
    public void dump (PrintStream ps, String indent) {
        ps.print (frozen);
    }

    @Override
    public String toString () {
        return frozen;
    }

    private static final long serialVersionUID = -8672445411081885024L;
}
