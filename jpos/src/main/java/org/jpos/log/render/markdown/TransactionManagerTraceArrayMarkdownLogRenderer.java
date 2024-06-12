package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;
import static  org.jpos.transaction.TransactionManager.Trace;

import java.io.PrintStream;

public final class TransactionManagerTraceArrayMarkdownLogRenderer implements LogRenderer<Trace[]> {
    @Override
    public void render(Trace[] traces, PrintStream ps, String indent) {
        ps.println ("```mermaid");
        ps.println ("gitGraph");
        for (int i=0; i<traces.length; i++) {
            ps.println (traces[i]);
        }
        ps.println ("```");
    }
    public Class<?> clazz() {
        return Trace[].class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
