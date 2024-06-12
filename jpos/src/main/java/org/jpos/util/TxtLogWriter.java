/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TxtLogWriter implements LogEventWriter {
    private PrintStream ps;
    private final LogRenderer<LogEvent> renderer = LogRendererRegistry.getRenderer(LogEvent.class, LogRenderer.Type.TXT);
    private Instant start;

    @Override
    public void write(LogEvent ev) {
        renderer.render(ev, ps, LocalDateTime.ofInstant(start, ZoneId.systemDefault()) + " ");
    }

    @Override
    public void setPrintStream(PrintStream ps) {
        this.ps = ps;
        start = Instant.now();
        // ps.printf ("# Log Start %s (%d)%n", LocalDateTime.ofInstant(start, ZoneId.systemDefault()), ps.hashCode());
    }

    @Override
    public void close() {
//        Instant now = Instant.now();
//        ps.printf ("# Log End %s (%s)%n",
//          LocalDateTime.ofInstant(now, ZoneId.systemDefault()),
//          ISODate.formatDuration(Duration.between(start,now))
//        );
        if (ps != System.out && ps != System.err) {
            ps.close();
        }
    }
}
