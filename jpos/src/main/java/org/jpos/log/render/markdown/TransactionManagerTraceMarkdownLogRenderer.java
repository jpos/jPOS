package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;
import org.jpos.transaction.TransactionManager;

import java.io.PrintStream;

public final class TransactionManagerTraceMarkdownLogRenderer implements LogRenderer<TransactionManager.Trace> {
    @Override
    public void render(TransactionManager.Trace t, PrintStream ps, String indent) {
        ps.println (indent(indent, t.toString()));
    }
    public Class<?> clazz() {
        return TransactionManager.Trace.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
