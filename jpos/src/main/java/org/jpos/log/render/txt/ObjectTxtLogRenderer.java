package org.jpos.log.render.txt;

import org.jpos.log.LogRenderer;
import java.io.PrintStream;

public final class ObjectTxtLogRenderer implements LogRenderer<Object> {
    @Override
    public void render(Object obj, PrintStream ps, String indent) {
        ps.printf ("%s%s%n", indent, obj.toString().replaceAll("\\r\\n|\\r|\\n", ("\\\\n")));
    }
    public Class<?> clazz() {
        return Object.class;
    }
    public Type type() {
        return Type.TXT;
    }
}
