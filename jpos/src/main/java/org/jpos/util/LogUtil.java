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
import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * Helpers for emitting strings that may contain XML-reserved characters
 * inside log payloads, optionally wrapping them in {@code CDATA} blocks.
 */
public class LogUtil {
    /** Utility class; instances carry no state. */
    public LogUtil() {}
    /** Regex matching XML-reserved characters that force CDATA escaping. */
    public static final Pattern xmlReservedPattern = Pattern.compile("&|<|>");

    /**
     * Writes {@code s} to {@code p}, wrapping it in {@code <![CDATA[...]]>} when
     * the string contains XML-reserved characters.
     *
     * @param p destination stream
     * @param indent prefix used when the value is dumped on a single line
     * @param s value to emit
     */
    public static void dump (PrintStream p, String indent, String s) {
        try {
            boolean expanded = s.length() > 60;
            if (needsCDATA(s)) {
                if (expanded) {
                    p.println("<![CDATA[");
                    p.println(s);
                    p.println("]]>");
                } else {
                    p.print(indent + "<![CDATA[");
                    p.print(s);
                    p.println("]]>");
                }
            } else
                p.print(s);
        } catch (Exception e) {
            p.println(e.getMessage());
        }
    }

    /**
     * Indicates whether {@code s} contains any XML-reserved characters and therefore
     * needs {@code CDATA} escaping when embedded in XML output.
     *
     * @param s value to inspect
     * @return {@code true} if any of {@code &}, {@code <}, or {@code >} is present
     */
    public static boolean needsCDATA(String s) {
        return xmlReservedPattern.matcher(s).find();
    }
}
