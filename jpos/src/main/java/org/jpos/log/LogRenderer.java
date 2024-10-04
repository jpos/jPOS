/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
