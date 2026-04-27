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

package org.jpos.rc;

import org.jpos.transaction.TransactionConstants;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the Result of a transaction
 */
public class Result implements Loggeable {
    private final List<Entry> entries = Collections.synchronizedList(new ArrayList<>());

    /** Default constructor. */
    public Result() {
        super();
    }

    /**
     * Adds an informational entry to this result.
     *
     * @param source the source identifier of the entry
     * @param format printf-style format string
     * @param args   format arguments
     * @return this Result instance
     */
    public Result info (String source, String format, Object ... args) {
        return add(Type.INFO, null, source, format, args);
    }

    /**
     * Adds a warning entry to this result.
     *
     * @param source the source identifier of the entry
     * @param format printf-style format string
     * @param args   format arguments
     * @return this Result instance
     */
    public Result warn (String source, String format, Object ... args) {
        return add(Type.WARN, null, source, format, args);
    }

    /**
     * Adds a success entry to this result.
     *
     * @param irc    the success IRC code (must satisfy {@link IRC#success()})
     * @param source the source identifier of the entry
     * @param format printf-style format string
     * @param args   format arguments
     * @return this Result instance
     * @throws IllegalArgumentException if the IRC is not a success code
     */
    public Result success (IRC irc, String source, String format, Object ... args) {
        if (!irc.success())
            throw new IllegalArgumentException("Invalid success IRC " + irc);
        return add(Type.SUCCESS, irc, source, ""+format, args);
    }

    /**
     * Adds a failure entry to this result.
     *
     * @param irc    the failure IRC code
     * @param source the source identifier of the entry
     * @param format printf-style format string
     * @param args   format arguments
     * @return this Result instance
     */
    public Result fail (IRC irc, String source, String format, Object ... args) {
        synchronized (entries) {
            if (isSuccess()) {
                format = "" + format + " (inhibits " + success() + ")";
            }
            return add(Type.FAIL, irc, source, ""+format, args);
        }
    }

    /**
     * Helper method used to avoid adding an extra 'return' line in failing transaction participants
     * @return TransactionConstants.FAIL which is basically ABORT | READONLY | NO_JOIN;
     */
    public int FAIL() {
        return TransactionConstants.FAIL;
    }

    /**
     * Returns {@code true} if this result contains at least one informational entry.
     *
     * @return {@code true} if an INFO entry is present
     */
    public boolean hasInfo() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.type == Type.INFO);
        }
    }

    /**
     * Returns {@code true} if this result contains at least one warning entry.
     *
     * @return {@code true} if a WARN entry is present
     */
    public boolean hasWarnings() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.type == Type.WARN);
        }
    }

    /**
     * Returns {@code true} if this result contains at least one failure entry.
     *
     * @return {@code true} if a FAIL entry is present
     */
    public boolean hasFailures() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.type == Type.FAIL);
        }
    }

    /**
     * Returns {@code true} if any entry has an IRC that is marked as inhibit.
     *
     * @return {@code true} if an inhibit IRC is present
     */
    public boolean hasInhibit() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.irc != null && e.irc.inhibit());
        }
    }

    /**
     * Returns {@code true} if any entry carries the given IRC code.
     *
     * @param irc the IRC to look for
     * @return {@code true} if the IRC is found in any entry
     */
    public boolean hasIRC (IRC irc) {
        synchronized (entries) {
            return entries.stream().anyMatch(entry -> entry.irc == irc);
        }
    }

    /**
     * Returns {@code true} if there is a failure entry with the given IRC code.
     *
     * @param irc the failure IRC to look for
     * @return {@code true} if a failure entry with the given IRC is present
     */
    public boolean hasFailure(IRC irc) {
        synchronized (entries) {
            return failureList().stream().anyMatch(entry -> entry.irc == irc);
        }
    }

    /**
     * Returns {@code true} if there is a warning entry with the given IRC code.
     *
     * @param irc the warning IRC to look for
     * @return {@code true} if a warning entry with the given IRC is present
     */
    public boolean hasWarning(IRC irc) {
        synchronized (entries) {
            return warningList().stream().anyMatch(entry -> entry.irc == irc);
        }
    }

    /**
     * Returns {@code true} if there is an informational entry with the given IRC code.
     *
     * @param irc the info IRC to look for
     * @return {@code true} if an info entry with the given IRC is present
     */
    public boolean hasInfo(IRC irc) {
        synchronized (entries) {
            return infoList().stream().anyMatch(entry -> entry.irc == irc);
        }
    }

    /**
     * Returns {@code true} if this result has a success entry and no failure entries.
     *
     * @return {@code true} if the overall result is a success
     */
    public boolean isSuccess() {
        synchronized (entries) {
            return isSuccess0() && !hasFailures();
        }
    }

    /**
     * Returns the first failure entry, or {@code null} if none exists.
     *
     * @return the first {@link Entry} of type FAIL, or {@code null}
     */
    public Entry failure() {
        synchronized (entries) {
            Optional<Entry> entry = entries.stream().filter(e -> e.type == Type.FAIL).findFirst();
            return entry.isPresent() ? entry.get() : null;
        }
    }

    /**
     * Returns the first success entry if the result is successful, or {@code null} otherwise.
     *
     * @return the first {@link Entry} of type SUCCESS if successful, or {@code null}
     */
    public Entry success() {
        synchronized (entries) {
            Optional<Entry> entry = entries.stream().filter(e -> e.type == Type.SUCCESS).findFirst();
            return entry.isPresent() && !hasFailures() ? entry.get() : null;
        }
    }

    /**
     * Returns the full list of all entries in this result.
     *
     * @return list of all {@link Entry} objects
     */
    public List<Entry> entries() {
        return entries;
    }

    /**
     * Returns a list of all informational entries.
     *
     * @return list of INFO {@link Entry} objects
     */
    public List<Entry> infoList() {
        return entries
          .stream()
          .filter(s -> s.type == Type.INFO)
          .collect(Collectors.toList());
    }

    /**
     * Returns a list of all success entries.
     *
     * @return list of SUCCESS {@link Entry} objects
     */
    public List<Entry> successList() {
        return entries
          .stream()
          .filter(s -> s.type == Type.SUCCESS)
          .collect(Collectors.toList());
    }

    /**
     * Returns a list of all warning entries.
     *
     * @return list of WARN {@link Entry} objects
     */
    public List<Entry> warningList() {
        return entries
          .stream()
          .filter(s -> s.type == Type.WARN)
          .collect(Collectors.toList());
    }

    /**
     * Returns a list of all failure entries.
     *
     * @return list of FAIL {@link Entry} objects
     */
    public List<Entry> failureList() {
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
            if (isSuccess0()) {
                String inhibited = hasFailures() ? " inhibited='true'" : "";
                ps.printf("%s<success%s>%n", inner, inhibited);
                entries
                  .stream()
                  .filter(s -> s.type == Type.SUCCESS)
                  .forEach(e -> ps.printf("%s  [%s] %s %s%n", inner, e.irc, e.source, e.message));
                ps.printf("%s</success>%n", inner);
            }
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

    private boolean isSuccess0() {
        synchronized (entries) {
            return entries.stream().anyMatch(e -> e.type == Type.SUCCESS);
        }
    }

    private enum Type {
        /** Informational entry type. */
        INFO,
        /** Warning entry type. */
        WARN,
        /** Success entry type. */
        SUCCESS,
        /** Failure entry type. */
        FAIL
    }

    /** Represents a single entry in a transaction result. */
    public static class Entry {
        /** The type of this entry (INFO, WARN, SUCCESS, or FAIL). */
        Type type;
        /** The IRC code associated with this entry, or {@code null} for INFO/WARN. */
        IRC irc;
        /** The source identifier that generated this entry. */
        String source;
        /** The human-readable message for this entry. */
        String message;

        /**
         * Constructs an Entry with the given type, IRC, source, and message.
         *
         * @param type    the entry type
         * @param irc     the IRC code (may be {@code null})
         * @param source  the source identifier
         * @param message the entry message
         */
        public Entry(Type type, IRC irc, String source, String message) {
            this.type = type;
            this.irc = irc;
            this.source = source;
            this.message = message;
        }

        /**
         * Returns the type of this entry.
         *
         * @return entry type
         */
        public Type getType() {
            return type;
        }

        /**
         * Returns the IRC code of this entry.
         *
         * @return IRC code, or {@code null} if not applicable
         */
        public IRC getIrc() {
            return irc;
        }

        /**
         * Returns the source identifier of this entry.
         *
         * @return source identifier string
         */
        public String getSource() {
            return source;
        }

        /**
         * Returns the message text of this entry.
         *
         * @return message string
         */
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
