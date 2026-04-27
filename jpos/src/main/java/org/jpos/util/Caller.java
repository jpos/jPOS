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

package org.jpos.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Return Caller's short class name, method and line number
 */
public class Caller {
    /** Utility class; instances carry no state. */
    public Caller() {}
    private static String JAVA_ID_PATTERN = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\.*";
    private static Pattern FQCN = Pattern.compile(JAVA_ID_PATTERN + "(\\." + JAVA_ID_PATTERN + ")*");
    /**
     * Returns information for the immediate caller of the method that calls this helper.
     *
     * @return abbreviated {@code class.method:line} string
     */
    public static String info() {
        return info(1);
    }

    /**
     * Returns information for the caller {@code pos} frames above the immediate caller.
     *
     * @param pos additional stack frames to skip past the immediate caller
     * @return abbreviated {@code class.method:line} string
     */
    public static String info(int pos) {
        return info (Thread.currentThread().getStackTrace()[2+pos]);
    }

    /**
     * Formats a stack trace element as {@code abbreviatedClass.method:line}.
     *
     * @param st stack frame to format
     * @return abbreviated {@code class.method:line} string
     */
    public static String info (StackTraceElement st) {
        String clazz = st.getClassName();
        Matcher matcher = FQCN.matcher(clazz);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.hitEnd() ? matcher.group(1) : matcher.group(1).charAt(0));
            sb.append('.');
        }
        return sb.append(st.getMethodName())
          .append(':')
          .append(st.getLineNumber())
          .toString();
    }


    /**
     * Abbreviates a fully-qualified class name by collapsing every package segment
     * to its first character (e.g. {@code o.j.u.Caller}).
     *
     * @param clazz fully-qualified class name
     * @return the abbreviated class name
     */
    public static String shortClassName(String clazz) {
        Matcher matcher = FQCN.matcher(clazz);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.hitEnd()) {
                sb.append(matcher.group(1));
                break;
            }
            else {
                sb.append(matcher.group(1).charAt(0));
                sb.append('.');
            }
        }
        return sb.toString();
    }
}
