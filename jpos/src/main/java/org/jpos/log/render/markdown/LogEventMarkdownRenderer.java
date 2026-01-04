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

package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;
import org.jpos.log.LogRendererRegistry;
import org.jpos.util.LogEvent;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class LogEventMarkdownRenderer implements LogRenderer<LogEvent> {
    @Override
    public void render(LogEvent evt, PrintStream ps, String indent) {
        ps.printf ("## %s %s %s [%s]%s%n",
          LocalDateTime.ofInstant(evt.getDumpedAt(), ZoneId.systemDefault()),
          evt.getRealm(),
          evt.getTag(),
          evt.getTraceId(),
          evt.hasException() ? " (*)" : ""
        );

        indent = indent + "    ";
        for (Object obj : evt.getPayLoad()) {
            LogRendererRegistry.getRenderer(obj.getClass(), Type.MARKDOWN).render(obj, ps, indent);
        }
    }
    public Class<?> clazz() {
        return LogEvent.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
