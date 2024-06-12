package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;
import java.io.PrintStream;

public final class StringMarkdownLogRenderer implements LogRenderer<String> {
    @Override
    public void render(String s, PrintStream ps, String indent) {
        ps.println (indent(indent, s));
    }
    public Class<?> clazz() {
        return String.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
