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

package org.jpos.q2;

import org.jline.reader.LineReader;
import org.jpos.util.Loggeable;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public String getActiveSubSystem() {
        return activeSubSystem;
    }

    public void setActiveSubSystem(String subSystem) {
        String activeSubSystem = getActiveSubSystem();
        if (subSystem == null && activeSubSystem != null) {
            getUserData().remove(activeSubSystem);
        }
        this.activeSubSystem = subSystem;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public LineReader getReader() {
        return reader;
    }

    public void setReader(LineReader reader) {
        this.reader = reader;
    }

    public OutputStream getOutputStream() {
        return out;
    }

    public OutputStream getErrorStream() {
        return err;
    }

    public InputStream getInputStream() {
        return in;
    }

    public Map<Object,Object> getUserData() {
        return userData;
    }

    public boolean isInteractive() {
        return cli.isInteractive();
    }

    public CLI getCLI() {
        return cli;
    }

    public void printUserData() {
        getUserData().forEach((k,v) -> {
            println("Key: " + k.toString());
            println("Value: " + v.toString());
        });
    }

    public void printThrowable(Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(baos));
        println (baos.toString());
    }

    public void printLoggeable(Loggeable l, String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        l.dump (new PrintStream(baos), indent);
        println (baos.toString());
    }

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

    public void println(String s)  {
        print (s + System.getProperty("line.separator"));
    }

    public boolean confirm(String prompt) {
        return "yes".equalsIgnoreCase(getReader().readLine(prompt));
    }

    public String readSecurely(String prompt) {
        return getReader().readLine(prompt, '*');
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        OutputStream out;
        OutputStream err;
        InputStream in;
        LineReader reader;
        CLI cli;
        private Builder () { }

        public Builder out (OutputStream out) {
            this.out = out;
            return this;
        }

        public Builder err (OutputStream err) {
            this.err = err;
            return this;
        }

        public Builder in (InputStream in) {
            this.in = in;
            return this;
        }

        public Builder reader (LineReader reader) {
            this.reader = reader;
            return this;
        }

        public Builder cli (CLI cli) {
            this.cli = cli;
            return this;
        }

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
