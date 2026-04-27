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
/** Utility class for obtaining caller information from the current stack trace. */
public class Caller {
    /** Private constructor — utility class. */
    private Caller() { }
    private static String JAVA_ID_PATTERN = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\.*";
    private static Pattern FQCN = Pattern.compile(JAVA_ID_PATTERN + "(\\." + JAVA_ID_PATTERN + ")*");
    /**
     * Returns a string describing the immediate caller (class, method, line).
     * @return caller description
     */
    public static String info() {
        return info(1);
    }

    /**
     * Returns a string describing the caller at the given stack depth.
     * @param pos the stack depth
     * @return caller description
     */
    public static String info(int pos) {
        return info (Thread.currentThread().getStackTrace()[2+pos]);
    }

    /**
     * Returns a formatted string for the given stack trace element.
     * @param st the element to format
     * @return formatted string
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
     * Returns the simple class name from a fully-qualified class name.
     * @param clazz the fully-qualified class name
     * @return the simple class name
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
