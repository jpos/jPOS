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

import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.util.*;

public class CLI implements Runnable {
    final private static String DEFAULT_PROMPT = "q2> ";
    final private static String ESCAPED_SEMICOLON = "__semicolon__";
    private Thread t;
    private String line = null;
    private boolean keepRunning = false;
    private boolean interactive = false;
    protected CLIContext ctx;
    private CLICommandInterface cmdInterface;
    private Terminal terminal;
    private LineReader reader;
    private Q2 q2;
    private String prompt = DEFAULT_PROMPT;
    private History mainHistory;

    public CLI(Q2 q2, String line, boolean keepRunning) throws IOException {
        this(q2, System.in, System.out, line, keepRunning, true);
    }

    public CLI(Q2 q2, InputStream in, OutputStream rawout, String line, boolean keepRunning, boolean interactive) throws IOException {
        this.q2 = q2;
        PrintStream out = rawout instanceof PrintStream ? (PrintStream) rawout : new PrintStream(rawout);
        ctx = buildCLIContext(in, out);
        this.line = line;
        this.keepRunning = keepRunning;
        this.interactive = interactive;
        this.mainHistory = new DefaultHistory();
        if (interactive) {
            terminal = buildTerminal(in, out);
        }
        initCmdInterface(getCompletionPrefixes(), mainHistory);
    }

    protected boolean running() {
        return getQ2() == null || getQ2().running();
    }

    protected void markStopped() { }

    protected void markStarted() { }

    protected String[] getCompletionPrefixes() {
        return new String[] {"org.jpos.q2.cli." };
    }

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

    public void start() throws Exception {
        markStarted();
        t = new Thread(this);
        t.setName("Q2-CLI");
        t.start();
    }

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

    public Q2 getQ2() {
        return q2;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public LineReader getReader() {
        return reader;
    }

    public static void exec (InputStream in, OutputStream out, String command) throws Exception {
        CLI cli = new CLI(Q2.getQ2(), in, out, command, false, false);
        cli.start();
        cli.stop();
    }

    public static String exec (String command) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exec (null, out, command);
        return out.toString();
    }

    private Terminal buildTerminal (InputStream in, OutputStream out) throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.streams(in,out).system(System.in == in);
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
