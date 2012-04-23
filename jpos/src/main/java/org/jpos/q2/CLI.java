/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class CLI implements Runnable
{
    Thread t;
    Completor completor;
    String line = null;
    boolean keepRunning = false;
    protected CLIContext ctx;
    CLICommandInterface cmdInterface;

    public CLI(Q2 q2, String line, boolean keepRunning) throws IOException
    {
        this(q2, System.in, System.out, line, keepRunning);
    }

    public CLI(Q2 q2, InputStream in, OutputStream out, String line, boolean keepRunning) throws IOException
    {
        ConsoleReader reader = new ConsoleReader(in, new OutputStreamWriter(out));
        reader.setBellEnabled(true);

        ctx = new CLIContext();
        ctx.setQ2(q2);
        ctx.setConsoleReader(reader);
        ctx.setOutputStream(new PrintStream(out));

        this.line = line;
        this.keepRunning = keepRunning;

        initCompletor();
    }

    protected boolean isRunning()
    {
        return ctx.getQ2().running();
    }

    protected void markStopped()
    {
    }

    protected void markStarted()
    {
    }

    protected String[] getCompletionPrefixes()
    {
        return new String[]{"org.jpos.q2.cli."};
    }

    protected String getPrompt()
    {
        return "q2> ";
    }

    protected void handleExit()
    {
    }

    private void initCompletor() throws IOException
    {
        cmdInterface = new CLICommandInterface(ctx);
        for (String s : getCompletionPrefixes())
        {
            cmdInterface.addPrefix(s);
        }

        List<Completor> l = new LinkedList<Completor>();
        l.add(new CLIPrefixedClassNameCompletor(cmdInterface.getPrefixes()));
        completor = new ArgumentCompletor(l);
    }

    public void start() throws Exception
    {
        markStarted();
        t = new Thread(this);
        t.setName("Q2-CLI");
        t.start();
    }

    public void stop()
    {
        markStopped();
        try
        {
            t.join();
        }
        catch (InterruptedException e)
        {
        }
    }

    public void run()
    {
        while (isRunning())
        {
            if (line == null)
            {
                ConsoleReader reader = ctx.getConsoleReader();
                reader.addCompletor(completor);
                try
                {
                    line = reader.readLine(getPrompt());
                }
                catch (IOException e)
                {
                    ctx.printThrowable(e);
                }
                finally
                {
                    reader.removeCompletor(completor);
                }
            }
            if (line != null)
            {
                StringTokenizer st = new StringTokenizer(line, ";");
                while (st.hasMoreTokens())
                {
                    String n = st.nextToken();
                    try
                    {
                        cmdInterface.execCommand(n);
                    }
                    catch (IOException e)
                    {
                        ctx.printThrowable(e);
                    }
                }
                line = null;
            }
            if (!keepRunning)
            {
                break;
            }
        }
        handleExit();
    }

    // COMPATIBILITY METHODS
    public void print(String s) {}
    public void println(String s) {}
    public boolean confirm(String prompt) throws IOException { return false; }
    public Q2 getQ2() { return null; };
    public ConsoleReader getConsoleReader() { return null; }
    public PrintStream getOutputStream() { return null; }

    public interface Command
    {
        public void exec(CLI cli, String[] args) throws Exception;
    }
}
