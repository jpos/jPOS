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


import java.util.EventListener;

/**
 * Implemented by objects that wish to receive and process {@link LogEvent} instances produced by a {@link Logger}.
 * @author apr@cs.com.uy
 * @version $Id$
 */
public interface LogListener extends EventListener {
    /**
     * Processes a log event.
     * @param ev the log event
     * @return the (potentially modified) log event, or null to suppress it
     */
    LogEvent log(LogEvent ev);
    default void setLogEventWriter (LogEventWriter w) {
        // do nothing, for backward compatibility
    }
}
