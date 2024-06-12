package org.jpos.log.render.xml;

import org.jpos.log.LogRenderer;
import org.jpos.util.Loggeable;

import java.io.PrintStream;

public final class LoggeableXmlLogRenderer implements LogRenderer<Loggeable> {
    @Override
    public void render(Loggeable obj, PrintStream ps, String indent) {
        obj.dump (ps, indent);
    }
    public Class<?> clazz() {
        return Loggeable.class;
    }
    public Type type() {
        return Type.XML;
    }
}
