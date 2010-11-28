/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

import jline.*;
import org.jpos.iso.ISOUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class CLI extends Thread 
    implements SimpleCompletor.SimpleCompletorFilter 
{
    Q2 q2;
    ConsoleReader reader;
    PrintStream out;
    Completor completor;
    String line = null;
    boolean keepRunning = false;

    private CLI() { }

    public CLI (Q2 q2, String line, boolean keepRunning) throws IOException {
        super();
        this.q2 = q2;
        reader = new ConsoleReader();
        reader.setBellEnabled(true);
        out = System.out;
        this.line = line;
        this.keepRunning = keepRunning;
        initCompletors();
        setName ("Q2-CLI");
    }

    public void run() {
        try {
            while (q2.running()) {
                if (line == null) {
                    reader.addCompletor(completor);
                    line = reader.readLine("q2> ");
                    reader.removeCompletor(completor);
                }
                if (line != null) {
                    StringTokenizer st = new StringTokenizer (line, ";");
                    while (st.hasMoreTokens()) {
                        String n = st.nextToken();
                        execCommand (n);
                    }
                    line = null;
                }
                if (!keepRunning)
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace (out);
            out.flush();
        }
    }
    public void print (String s) {
        try {
            reader.printString (s);
        } catch (IOException e) {
            e.printStackTrace (out);
            out.flush();
        }
    }
    public void println (String s) {
        try {
            reader.printString (s);
            reader.printNewline ();
        } catch (IOException e) {
            e.printStackTrace (out);
            out.flush();
        }
    }
    public boolean confirm (String prompt) throws IOException {
        return "yes".equalsIgnoreCase (reader.readLine (prompt));
    }
    public Q2 getQ2() {
        return q2;
    }
    public ConsoleReader getConsoleReader() {
        return reader;
    }
    public PrintStream getOutputStream () {
        return out;
    }
    public String filter (String element) {
        return element.startsWith ("org.jpos.q2.cli.") ? element.substring(16).toLowerCase() : null;
    }
    private void execCommand (String line) throws IOException {
        String args[] = parseCommand (line);
        if (args.length == 0)
            return;
        String command = args[0].toUpperCase();
        String msg = null;
        String className = command;

        if (command.indexOf (".") < 0)
            className = "org.jpos.q2.cli."+command;
        try {
            if (className != null) {
                Object cmd = q2.getFactory().newInstance (className);
                if (cmd instanceof Command) {
                    try {
                        args[0] = ISOUtil.unPadLeft(line, ' '); // full line
                        ((Command) cmd).exec (this, args);
                    } catch (Exception ex) {
                        ex.printStackTrace (out);
                        out.flush();
                    }
                }
            }
        } catch (Exception ex) {
            println ("Invalid command '" + command + "'");
        }
    }
    private String[] parseCommand (String line) throws IOException {
        if (line == null)
            return new String[0];

        StringTokenizer st = new StringTokenizer(line);
        String[] args = new String[st.countTokens()];
        for (int i=0; st.hasMoreTokens(); i++)
            args[i] = new String (st.nextToken());
        return args;
    }
    private void initCompletors() throws IOException {
        List l = new LinkedList();
        l.add(new ClassNameCompletor(this));
        completor = new ArgumentCompletor(l);
    }
    public interface Command {
        public void exec (CLI cli, String[] args) throws Exception;
    }
}

