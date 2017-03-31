/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
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

package org.jpos.rc;

import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Result of a transaction
 */
public class Result implements Loggeable {
    private final List<Entry> entries = Collections.synchronizedList(new ArrayList<>());

    public Result() {
        super();
    }

    public Result info (String source, String format, Object ... args) {
        return add(Type.INFO, null, source, format, args);
    }
    public Result warn (String source, String format, Object ... args) {
        return add(Type.WARN, null, source, format, args);
    }
    public Result fail (IRC irc, String source, String format, Object ... args) {
        return add(Type.FAIL, irc, source, format, args);
    }

    public boolean hasInfo() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.type == Type.INFO);
        }
    }
    public boolean hasWarnings() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.type == Type.WARN);
        }
    }
    public boolean hasFailures() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.type == Type.FAIL);
        }
    }

    public Entry failure() {
        synchronized (entries) {
            Optional<Entry> entry = entries.stream().filter(e -> e.type == Type.FAIL).findFirst();
            return entry.isPresent() ? entry.get() : null;
        }
    }

    public List<Entry> entries() {
        return entries;
    }

    public List<Entry> infos() {
        return entries
          .stream()
          .filter(s -> s.type == Type.INFO)
          .collect(Collectors.toList());
    }

    public List<Entry> warnings() {
        return entries
          .stream()
          .filter(s -> s.type == Type.WARN)
          .collect(Collectors.toList());
    }
    public List<Entry> failures() {
        return entries
          .stream()
          .filter(s -> s.type == Type.FAIL)
          .collect(Collectors.toList());
    }
    private Result add (Type type, IRC irc, String source, String format, Object ... args) {
        entries.add (
          new Entry(type, irc, source, String.format(format, args))
        );
        return this;
    }

    @Override
    public void dump(final PrintStream ps, final String indent) {
        if (entries.size() == 0) {
            ps.printf ("%s<result/>%n", indent);
            return;
        }
        final String inner = indent + "  ";
        ps.printf("%s<result>%n", indent);
        synchronized (entries) {
            if (hasFailures()) {
                ps.printf("%s<fail>%n", inner);
                entries
                  .stream()
                  .filter(s -> s.type == Type.FAIL)
                  .forEach(e -> ps.printf("%s  [%s] %s %s%n", inner, e.irc, e.source, e.message));
                ps.printf("%s</fail>%n", inner);
            }
            if (hasWarnings()) {
                ps.printf("%s<warn>%n", inner);
                entries
                  .stream()
                  .filter(s -> s.type == Type.WARN)
                  .forEach(e -> ps.printf("%s  [%s] %s%n", inner, e.source, e.message));
                ps.printf("%s</warn>%n", inner);
            }
            if (hasInfo()) {
                ps.printf("%s<info>%n", inner);
                entries
                  .stream()
                  .filter(s -> s.type == Type.INFO)
                  .forEach(e -> ps.printf("%s  [%s] %s%n", inner, e.source, e.message));
                ps.printf("%s</info>%n", inner);
            }
        }
        ps.printf("%s</result>%n", indent);
    }

    @Override
    public String toString() {
        return "Result{" +
          "entries=" + entries +
          '}';
    }

    private enum Type {
        INFO,
        WARN,
        FAIL
    }
    public static class Entry {
        Type type;
        IRC irc;
        String source;
        String message;

        public Entry(Type type, IRC irc, String source, String message) {
            this.type = type;
            this.irc = irc;
            this.source = source;
            this.message = message;
        }

        public Type getType() {
            return type;
        }

        public IRC getIrc() {
            return irc;
        }

        public String getSource() {
            return source;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "Entry{" +
              "type=" + type +
              ", irc=" + irc +
              ", source='" + source + '\'' +
              ", message='" + message + '\'' +
              '}';
        }
    }
}
