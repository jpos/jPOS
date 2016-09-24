/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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
import org.jline.reader.impl.history.history.MemoryHistory;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.io.*;
import java.util.*;

public class CLI implements Runnable {
    final private static String DEFAULT_PROMPT = "q2> ";
    private Thread t;
    private String line = null;
    private boolean keepRunning = false;
    protected CLIContext ctx;
    private CLICommandInterface cmdInterface;
    private Terminal terminal;
    private LineReader reader;
    private Q2 q2;
    private String prompt = DEFAULT_PROMPT;
    private History mainHistory;

    public CLI(Q2 q2, String line, boolean keepRunning) throws IOException {
        this(q2, System.in, System.out, line, keepRunning);
    }

    public CLI(Q2 q2, InputStream in, OutputStream rawout, String line, boolean keepRunning) throws IOException {
        this.q2 = q2;
        PrintStream out = rawout instanceof PrintStream ? (PrintStream) rawout : new PrintStream(rawout);
        terminal = buildTerminal(in, out);
        ctx = buildCLIContext(in, out);
        this.line = line;
        this.keepRunning = keepRunning;
        this.mainHistory = new MemoryHistory();
        initCmdInterface(getCompletionPrefixes(), mainHistory);
    }

    protected boolean running() {
        return getQ2().running();
    }

    protected void markStopped() { }

    protected void markStarted() { }

    protected String[] getCompletionPrefixes() {
        return new String[] {"org.jpos.q2.cli." };
    }

    protected void handleExit() {
        q2.shutdown();
    }

    void setPrompt(String prompt, String[] completionPrefixes) throws IOException {
        this.prompt = prompt != null ? prompt : DEFAULT_PROMPT;
        initCmdInterface(completionPrefixes, completionPrefixes == null ? mainHistory : new MemoryHistory());
    }

    private void initCmdInterface(String[] completionPrefixes, History history) throws IOException {
        completionPrefixes = completionPrefixes == null ? getCompletionPrefixes() : completionPrefixes;
        cmdInterface = new CLICommandInterface(ctx);
        for (String s : completionPrefixes) {
            cmdInterface.addPrefix(s);
        }
        reader = buildReader(terminal, completionPrefixes, history);
        ctx.setReader(reader);
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
                if (line == null) {
                    line = reader.readLine(prompt, null, null, null);
                }
                if (line != null) {
                    StringTokenizer st = new StringTokenizer(line, ";");
                    while (st.hasMoreTokens()) {
                        String n = st.nextToken();
                        try {
                            cmdInterface.execCommand(n);
                        } catch (IOException e) {
                            ctx.printThrowable(e);
                        }
                    }
                    line = null;
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
        return keepRunning;
    }

    public LineReader getReader() {
        return reader;
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
