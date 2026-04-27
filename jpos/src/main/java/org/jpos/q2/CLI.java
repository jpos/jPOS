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

import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Interactive command-line interface for a running Q2 instance. */
public class CLI implements Runnable {
    final private static String DEFAULT_PROMPT = "q2> ";
    final private static String ESCAPED_SEMICOLON = "__semicolon__";
    private Thread t;
    private String line = null;
    private boolean keepRunning = false;
    private boolean interactive = false;
    /** The context for this CLI session. */
    protected CLIContext ctx;
    private CLICommandInterface cmdInterface;
    private Terminal terminal;
    private LineReader reader;
    private Q2 q2;
    private String prompt = DEFAULT_PROMPT;
    private History mainHistory;

    /**
     * Creates a simple CLI with a single command and no streams.
     * @param q2 the Q2 instance
     * @param line the initial command line
     * @param keepRunning true to keep running after the first command
     * @throws IOException on I/O failure
     */
    public CLI(Q2 q2, String line, boolean keepRunning) throws IOException {
        this(q2, System.in, System.out, line, keepRunning, true);
    }

    /**
     * Creates a full CLI with explicit I/O streams.
     * @param q2 the Q2 instance
     * @param in input stream
     * @param rawout output stream
     * @param line initial command (may be null for interactive mode)
     * @param keepRunning true to keep running after commands
     * @param interactive true for interactive (line-edit) mode
     * @throws IOException on I/O failure
     */
    public CLI(Q2 q2, InputStream in, OutputStream rawout, String line, boolean keepRunning, boolean interactive) throws IOException {
        Logger.getLogger("org.jline").setLevel(Level.SEVERE);
        this.q2 = q2;
        PrintStream out = rawout instanceof PrintStream ? (PrintStream) rawout : new PrintStream(rawout);
        ctx = buildCLIContext(in, out);
        this.line = line;
        this.keepRunning = keepRunning;
        this.interactive = interactive;
        this.mainHistory = new DefaultHistory();
        if (interactive) {
            terminal = terminal = buildTerminal(in, out);
        }
        initCmdInterface(getCompletionPrefixes(), mainHistory);
    }

    /**
     * Returns true if this CLI is still running.
     * @return true if running
     */
    protected boolean running() {
        return getQ2() == null || getQ2().running();
    }

    /** Called when the CLI is stopping; subclasses may override. */
    protected void markStopped() { }

    /** Called when the CLI is starting; subclasses may override. */
    protected void markStarted() { }

    /**
     * Returns command prefixes registered for tab-completion.
     * @return array of command prefixes
     */
    protected String[] getCompletionPrefixes() {
        return new String[] {"org.jpos.q2.cli." };
    }

    /** Called on normal exit; subclasses may override. */
    protected void handleExit() { }

    void setPrompt(String prompt, String[] completionPrefixes) throws IOException {
        this.prompt = prompt != null ? prompt : DEFAULT_PROMPT;
        initCmdInterface(completionPrefixes, completionPrefixes == null ? mainHistory : new DefaultHistory());
    }

    private void initCmdInterface(String[] completionPrefixes, History history) throws IOException {
        completionPrefixes = completionPrefixes == null ? getCompletionPrefixes() : completionPrefixes;
        cmdInterface = new CLICommandInterface(ctx);
        for (String s : completionPrefixes) {
            cmdInterface.addPrefix(s);
        }
        cmdInterface.addPrefix("org.jpos.q2.cli.builtin.");
        if (terminal != null) {
            reader = buildReader(terminal, completionPrefixes, history);
            ctx.setReader(reader);
        }
    }

    /** Starts the CLI session.
     * @throws Exception on startup failure
     */
    public void start() throws Exception {
        markStarted();
        t = new Thread(this);
        t.setName("Q2-CLI");
        t.start();
    }

    /** Stops the CLI session. */
    public void stop() {
        markStopped();
        try {
            t.join();
        }
        catch (InterruptedException ignored) { }
    }

    public void run() {
        while (running()) {
            try {
                LineReader reader = getReader();
                String p = prompt;
                if (line == null) {
                    String s;
                    while ((s = reader.readLine(p)) != null) {
                        if (s.endsWith("\\")) {
                            s = s.substring(0, s.length() -1);
                            p = "";
                            line = line == null ? s : line + s;
                            continue;
                        }
                        line = line == null ? s : line + s;
                        break;
                    }
                }
                if (line != null) {
                    line = line.replace("\\;", ESCAPED_SEMICOLON);
                    StringTokenizer st = new StringTokenizer(line, ";");
                    boolean exit = false;
                    while (st.hasMoreTokens()) {
                        String n = st.nextToken().replace (ESCAPED_SEMICOLON, ";");
                        try {
                            String[] args = cmdInterface.parseCommand(n);
                            if (args.length > 0 && args[0].contains(":")) {
                                String prefixCommand = args[0].substring(0, args[0].indexOf(":"));
                                cmdInterface.execCommand(prefixCommand);
                                n = n.substring(prefixCommand.length() + 1);
                                exit = true;
                            }
                            cmdInterface.execCommand(n);
                        } catch (IOException e) {
                            ctx.printThrowable(e);
                        }
                    }
                    line = null;
                    if (exit) {
                        try {
                            cmdInterface.execCommand("exit");
                        } catch (IOException e) {
                            ctx.printThrowable(e);
                        }
                    }
                }
                if (!keepRunning) {
                    break;
                }

            } catch (UserInterruptException | EndOfFileException e) {
                break;
            }
        }
        try {
            if (keepRunning)
                getReader().getTerminal().close();
        } catch (IOException e) {
            ctx.printThrowable(e);
        }
        handleExit();
    }

    /**
     * Returns the Q2 instance this CLI is attached to.
     * @return the Q2 instance
     */
    public Q2 getQ2() {
        return q2;
    }

    /**
     * Returns true if this CLI session is interactive.
     * @return true if interactive
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Returns the JLine3 LineReader for this session.
     * @return the LineReader
     */
    public LineReader getReader() {
        return reader;
    }

    /**
     * Executes a CLI command with the given I/O streams.
     * @param in input stream
     * @param out output stream
     * @param command command to execute
     * @throws Exception on execution failure
     */
    public static void exec (InputStream in, OutputStream out, String command) throws Exception {
        CLI cli = new CLI(Q2.getQ2(), in, out, command, false, false);
        cli.start();
        cli.stop();
    }

    /**
     * Executes a CLI command and captures its output as a string.
     * @param command command string to execute
     * @return captured output
     * @throws Exception on execution failure
     */
    public static String exec (String command) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exec (null, out, command);
        return out.toString();
    }

    /**
     * Builds a JLine3 Terminal for this session.
     * @param in input stream
     * @param out output stream
     * @return the Terminal
     * @throws IOException on I/O failure
     */
    protected Terminal buildTerminal (InputStream in, OutputStream out) throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder()
            .streams(in,out)
            .system(System.in == in);
        Terminal t = builder.build();
        Attributes attr = t.getAttributes();
        attr.getOutputFlags().addAll(
            EnumSet.of(Attributes.OutputFlag.ONLCR, Attributes.OutputFlag.OPOST)
        );
        t.setAttributes(attr);
         return t;
    }


    private LineReader buildReader(Terminal terminal, String[] completionPrefixes, History history) throws IOException {
        LineReader reader = LineReaderBuilder.builder()
          .terminal(terminal)
          .history(history)
          .completer(new CLIPrefixedClassNameCompleter(Arrays.asList(completionPrefixes)))
          .build();
        reader.unsetOpt(LineReader.Option.INSERT_TAB);
        reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        return reader;
    }

    private CLIContext buildCLIContext (InputStream in, OutputStream out) {
        return CLIContext.builder()
                .cli(this)
                .in(in)
                .out(out)
                .build();
    }
}
