package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;
import java.io.PrintStream;

public final class ObjectMarkdownLogRenderer implements LogRenderer<Object> {
    @Override
    public void render(Object obj, PrintStream ps, String indent) {
        // ps.printf ("> %s%n%n%s%n", obj.getClass().getCanonicalName(), obj);
        ps.printf ("%s%n", indent(indent,obj.toString()));
    }
    public Class<?> clazz() {
        return Object.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
