package org.jpos.util;

import java.io.PrintStream;
import java.io.Serializable;

public class MarkdownLogEvent extends LogEvent {
    private String frozen;

    public MarkdownLogEvent(String frozen) {
        this.frozen = frozen;
    }
    public MarkdownLogEvent (LogEvent evt) {
        super(evt.getSource(), evt.getTag(), evt.getRealm());
        frozen = evt.toString();
    }
    @Override
    public void dump (PrintStream ps, String indent) {
        ps.print (frozen);
    }

    @Override
    public String toString () {
        return frozen;
    }
}

