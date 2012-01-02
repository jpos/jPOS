/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.core.Configurable
 * @since jPOS 1.2
 */
public class SimpleLogListener implements LogListener {
    PrintStream p;

    public SimpleLogListener () {
        super();
        p = System.out;
    }
    public SimpleLogListener (PrintStream p) {
        this ();
        setPrintStream (p);
    }
    public synchronized void setPrintStream (PrintStream p) {
        this.p = p;
    }
    public synchronized void close() {
        if (p != null) {
            p.close();
            p = null;
        }
    }
    public synchronized LogEvent log (LogEvent ev) {
        if (p != null) {
            ev.dump (p, "");
            p.flush();
        }
        return ev;
    }
}

