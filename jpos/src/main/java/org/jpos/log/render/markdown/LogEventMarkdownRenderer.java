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
        ps.printf ("## %s %s %s%s%n",
          LocalDateTime.ofInstant(evt.getDumpedAt(), ZoneId.systemDefault()),
          evt.getRealm(),
          evt.getTag(),
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
