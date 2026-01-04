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

import org.jpos.log.LogRenderer;
import org.jpos.log.LogRendererRegistry;

import java.io.PrintStream;

public class XmlLogWriter implements LogEventWriter {
    private PrintStream ps;
    private final LogRenderer<LogEvent> renderer = LogRendererRegistry.getRenderer(LogEvent.class, LogRenderer.Type.XML);

    @Override
    public void write(LogEvent ev) {
        renderer.render(ev, ps, "");
    }

    @Override
    public void setPrintStream(PrintStream ps) {
        this.ps = ps;
    }

    @Override
    public void close() {
        if (ps != System.out && ps != System.err) {
            ps.close();
        }
    }
}

