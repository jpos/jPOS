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
import org.jpos.util.Profiler;

import java.io.PrintStream;
import java.util.Set;

public final class ProfilerMarkdownRenderer implements LogRenderer<Profiler> {
    public ProfilerMarkdownRenderer() {
    }

    @Override
    public void render(Profiler prof, PrintStream ps, String indent) {
        var events = prof.getEvents();
        int width = maxLength(events.keySet());
        final String fmt = "| %-" + width + "s | %10.10s | %10.10s |%n";
        ps.print (row(fmt, "Checkpoint", "Elapsed", "Total"));
        ps.print(
          row(fmt, "-".repeat(width), "---------:", "-------:")
        );
        StringBuilder graph = new StringBuilder();
        events.forEach((key, v) -> {
            ps.print(
              row(fmt, v.getEventName(), toMillis(v.getDurationInNanos()), toMillis(v.getTotalDurationInNanos()))
            );
            graph.append ("  \"%s\" : %s%n".formatted(key, toMillis(v.getDurationInNanos())));
        });
        ps.println();
        ps.println ("```mermaid");
        ps.println ("pie title Profiler");
        ps.println (graph);
        ps.println ("```");

    }
    public Class<?> clazz() {
        return Profiler.class;
    }

    public Type type() {
        return Type.MARKDOWN;
    }
    private String row (String fmt, String c1, String c2, String c3) {
        return fmt.formatted(c1, c2, c3);
    }
    
    private String toMillis(long nanos) {
        long millis = nanos / Profiler.TO_MILLIS;
        long fractional = (nanos % Profiler.TO_MILLIS) / (Profiler.TO_MILLIS / 1000);
        return String.format("%d.%03d", millis, fractional);
    }
    private int maxLength (Set<String> keys) {
        return keys.stream()
          .mapToInt(String::length)
          .max()
          .orElse(0); // R
    }
}
