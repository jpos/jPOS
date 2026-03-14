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
 * Implemented by objects that own a {@link Logger} and can produce {@link LogEvent} instances.
 * @author apr@cs.com.uy
 * @version $Id$
 */
public interface LogSource {
    /**
     * Attaches a {@link Logger} and realm to this log source.
     * @param logger the logger to use
     * @param realm  the log realm (diagnostic label)
     */
    void setLogger(Logger logger, String realm);
    /** @return the log realm associated with this source */
    String getRealm();
    /** @return the Logger associated with this source */
    Logger getLogger();
}

