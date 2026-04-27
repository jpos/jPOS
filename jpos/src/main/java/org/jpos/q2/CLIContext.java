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

package org.jpos.q2;

import org.jline.reader.LineReader;
import org.jpos.util.Loggeable;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Holds the I/O streams, LineReader, and metadata for a CLI session. */
public class CLIContext {
    private boolean stopped = false;
    private OutputStream out;
    private OutputStream err;
    private InputStream in;
    private LineReader reader;
    private Map<Object, Object> userData;
    private CLI cli;
    private String activeSubSystem = null;

    @SuppressWarnings("unused")
    private CLIContext() { }

    private CLIContext(CLI cli, OutputStream out, OutputStream err, InputStream in, LineReader reader, Map<Object, Object> userData) {
        this.cli = cli;
        this.out = out;
        this.err = err;
        this.in = in;
        this.reader = reader;
        this.userData = userData;
    }

    /**
     * Returns the name of the active CLI sub-system.
     * @return the active sub-system name, or null
     */
    public String getActiveSubSystem() {
        return activeSubSystem;
    }

    /**
     * Sets the active CLI sub-system by name.
     * @param subSystem the name of the new active sub-system
     */
    public void setActiveSubSystem(String subSystem) {
        String activeSubSystem = getActiveSubSystem();
        if (subSystem == null && activeSubSystem != null) {
            getUserData().remove(activeSubSystem);
        }
        this.activeSubSystem = subSystem;
    }

    /**
     * Returns true if this CLI session has been stopped.
     * @return true if stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Marks this CLI session as stopped.
     * @param stopped true to stop
     */
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    /**
     * Returns the JLine3 LineReader for interactive input.
     * @return the LineReader
     */
    public LineReader getReader() {
        return reader;
    }

    /**
     * Sets the JLine3 LineReader.
     * @param reader the LineReader
     */
    public void setReader(LineReader reader) {
        this.reader = reader;
    }

    /**
     * Returns the standard output stream for this session.
     * @return the output stream
     */
    public OutputStream getOutputStream() {
        return out;
    }

    /**
     * Returns the error stream for this session.
     * @return the error stream
     */
    public OutputStream getErrorStream() {
        return err;
    }

    /**
     * Returns the input stream for this session.
     * @return the input stream
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Returns the mutable user-data map for sharing state across CLI commands.
     * @return user data map
     */
    public Map<Object,Object> getUserData() {
        return userData;
    }

    /**
     * Returns true if this session is interactive (has a LineReader).
     * @return true if interactive
     */
    public boolean isInteractive() {
        return cli.isInteractive();
    }

    /**
     * Returns the CLI instance managing this context.
     * @return the CLI
     */
    public CLI getCLI() {
        return cli;
    }

    /** Prints all user-data entries to the output stream. */
    public void printUserData() {
        getUserData().forEach((k,v) -> {
            println("Key: " + k.toString());
            println("Value: " + v.toString());
        });
    }

    /**
     * Prints a throwable stack trace to the error stream.
     * @param t the throwable to print
     */
    public void printThrowable(Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(baos));
        println (baos.toString());
    }

    /**
     * Dumps a Loggeable to the output stream.
     * @param l the Loggeable to dump
     * @param indent indentation prefix
     */
    public void printLoggeable(Loggeable l, String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        l.dump (new PrintStream(baos), indent);
        println (baos.toString());
    }

    /**
     * Prints a string to the output stream (no trailing newline).
     * @param s the string to print
     */
    public void print(String s) {
        if (isInteractive()) {
            PrintWriter writer = getReader().getTerminal().writer();
            writer.print(s);
            writer.flush();
        }
        else {
            try {
                out.write(s.getBytes());
                out.flush();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * Prints a string to the output stream followed by a newline.
     * @param s the string to print
     */
    public void println(String s)  {
        print (s + System.getProperty("line.separator"));
    }

    /**
     * Prompts the user for confirmation.
     * @param prompt the confirmation prompt
     * @return true if user confirmed
     */
    public boolean confirm(String prompt) {
        return "yes".equalsIgnoreCase(getReader().readLine(prompt));
    }

    /**
     * Reads a string from the user without echoing (e.g. for passwords).
     * @param prompt the prompt to display
     * @return the input string
     */
    public String readSecurely(String prompt) {
        return getReader().readLine(prompt, '*');
    }

    /**
     * Returns a new Builder for constructing a CLIContext.
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for constructing {@link CLIContext} instances. */
    public static class Builder {
        OutputStream out;
        OutputStream err;
        InputStream in;
        LineReader reader;
        CLI cli;
        private Builder () { }

        /**
         * Sets the standard output stream.
         * @param out the standard output stream
         * @return this
         */
        public Builder out (OutputStream out) {
            this.out = out;
            return this;
        }

        /**
         * Sets the error stream.
         * @param err the error stream
         * @return this
         */
        public Builder err (OutputStream err) {
            this.err = err;
            return this;
        }

        /**
         * Sets the input stream.
         * @param in the input stream
         * @return this
         */
        public Builder in (InputStream in) {
            this.in = in;
            return this;
        }

        /**
         * Sets the JLine3 LineReader.
         * @param reader the LineReader
         * @return this
         */
        public Builder reader (LineReader reader) {
            this.reader = reader;
            return this;
        }

        /**
         * Sets the CLI instance.
         * @param cli the CLI
         * @return this
         */
        public Builder cli (CLI cli) {
            this.cli = cli;
            return this;
        }

        /**
         * Builds and returns the CLIContext.
         * @return the constructed CLIContext
         */
        public CLIContext build() {
            if (reader != null) {
                if (out == null)
                    out = reader.getTerminal().output();
                if (err == null)
                    err = out;
            }
            return new CLIContext(cli, out, err, in, reader, new LinkedHashMap());
        }
    }
}
