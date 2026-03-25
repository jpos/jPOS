/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public interface LogRenderer<T> {
    /**
     * Renders the given object to the print stream with the specified indentation.
     * @param obj the object to render
     * @param ps the output stream
     * @param indent indentation prefix
     */
    void render (T obj, PrintStream ps, String indent);
    /**
     * Returns the class this renderer handles.
     * @return the handled class
     */
    Class<?> clazz();
    /**
     * Returns the log event type this renderer handles.
     * @return the handled event type
     */
    Type type();

    /**
     * Renders the given object to the print stream with no indentation.
     * @param obj the object to render
     * @param ps the output stream
     */
    default void render (T obj, PrintStream ps) {
        render (obj, ps, "");
    }

    /**
     * Renders the given object to a string with the specified indentation.
     * @param obj the object to render
     * @param indent indentation prefix
     * @return rendered string
     */
    default String render (T obj, String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        render (obj, ps, indent);
        return baos.toString();
    }

    /**
     * Renders the given object to a string with no indentation.
     * @param obj the object to render
     * @return rendered string
     */
    default String render (T obj) {
        return render (obj, "");
    }

    /**
     * Prepends {@code indent} to each line of {@code s}.
     * @param indent the indentation string
     * @param s the multi-line string to indent
     * @return the indented string
     */
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


    /** Supported log output format types. */
    enum Type {
        XML,
        JSON,
        TXT,
        MARKDOWN
    }
}
