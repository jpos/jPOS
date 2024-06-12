package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;
import org.jpos.util.Loggeable;

import java.io.PrintStream;

public final class LoggeableMarkdownLogRenderer implements LogRenderer<Loggeable> {
    @Override
    public void render(Loggeable obj, PrintStream ps, String indent) {
        ps.println("```xml");
        obj.dump (ps, indent);
        ps.println("```");
    }
    public Class<?> clazz() {
        return Loggeable.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
