package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class ThrowableMarkdownLogRenderer implements LogRenderer<Throwable> {
    @Override
    public void render(Throwable t, PrintStream ps, String indent) {
        ps.println(stackTrace(indent+" ", t));
    }
    public Class<?> clazz() {
        return Throwable.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }

    private String stackTrace(String indent, Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        t.printStackTrace(ps);
        return indent(indent, baos.toString());
    }
}
