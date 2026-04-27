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

/**
 * {@link BufferedLogListener} variant that only retains events whose payload
 * carries an exception, useful for buffering recent failures for triage.
 */
@SuppressWarnings("unused")
public class BufferedExceptionLogListener extends BufferedLogListener {
    /** Creates a listener with the inherited default capacity. */
    public BufferedExceptionLogListener() {}
    public LogEvent log(LogEvent ev) {
        if (hasException(ev))
             super.log(ev);
        return ev;
    }

    private boolean hasException (LogEvent evt) {
        for (Object o : evt.getPayLoad()) {
            if (o instanceof Throwable)
                return true;
        }
        return false;
    }
}
