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

package org.jpos.util;

/**
 * A specific log listener that filters all LogEvents that doesn't
 * contain any exception.
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class ExceptionLogFilter implements LogListener {
    public ExceptionLogFilter () {
        super();
    }
    public synchronized LogEvent log (LogEvent evt) {
        synchronized (evt.getPayLoad()) {
            for (Object o : evt.getPayLoad()) {
                if (o instanceof Throwable)
                    return evt;
            }
        }
        return null;
    }
}

