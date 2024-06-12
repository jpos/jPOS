package org.jpos.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public interface LogRenderer<T> {
    void render (T obj, PrintStream ps, String indent);
    Class<?> clazz();
    Type type();

    default void render (T obj, PrintStream ps) {
        render (obj, ps, "");
    }

    default String render (T obj, String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        render (obj, ps, indent);
        return baos.toString();
    }

    default String render (T obj) {
        return render (obj, "");
    }

    default String indent (String indent, String s) {
        if (s == null || s.isEmpty() || indent==null || indent.isEmpty()) {
            return s;
        }
        String[] lines = s.split("\n", -1);  // Preserve trailing empty strings
        StringBuilder indentedString = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            indentedString.append(indent).append(lines[i]);
            if (i < lines.length - 1) {
                indentedString.append("\n");
            }
        }
        return indentedString.toString();
    }


    enum Type {
        XML,
        JSON,
        TXT,
        MARKDOWN
    }
}
