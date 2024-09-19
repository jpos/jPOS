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
