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

import java.io.PrintStream;

/**
 * Abstract class for LogEventWriter implementations.
 *
 * Ensures that derived classes close the PrintStream since some of them
 * may wrap it in an outer stream.
 *
 * Default write implementation is what is currently used by SimpleLogListener
 * with an additional null check on the passed in LogEvent.
 *
 * @author Alwyn Schoeman
 * @since 2.1.4
 */
public abstract class BaseLogEventWriter implements LogEventWriter {
    PrintStream p;

    @Override
    public void setPrintStream(PrintStream p) {
        if (p == null) {
            close();
            return;
        }
        if (this.p == p) return;
        if (this.p != null) close();
        this.p = p;
    }

    @Override
    public synchronized void close() {
        if (p != null) {
            p.close();
            p = null;
        }
    }

    @Override
    public void write(LogEvent ev) {
        if (p != null && ev != null) {
            ev.dump(p, "");
            p.flush();
        }
    }
}
