/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

public class LogUtil {
    public static final Pattern xmlReservedPattern = Pattern.compile("&|<|>");

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

    public static boolean needsCDATA(String s) {
        return xmlReservedPattern.matcher(s).find();
    }
}
