package org.jpos.q2;

import jline.ConsoleReader;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.CLI.Command;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

public class CLICommandInterface
{
    CLIContext ctx;
    List<String> prefixes = new ArrayList<String>();
    WeakHashMap<String, Object> commandCache = new WeakHashMap<String, Object>(100);

    public List<String> getPrefixes()
    {
        return prefixes;
    }

    public CLICommandInterface(CLIContext ctx)
    {
        this.ctx = ctx;
    }

    public void addPrefix(String prefix)
    {
        prefixes.add(prefix);
    }

    public void execCommand(String line) throws IOException
    {
        String args[] = parseCommand(line);
        if (args.length == 0)
        {
            return;
        }
        String verbatimCmd=args[0];
        String command = args[0].toUpperCase();
        String className = command;

        for (String prefix : prefixes)
        {
            if (command.indexOf(".") < 0)
            {
                className = prefix + command;
            }

            try
            {
                Object cmd = getCommand(className);
                if (cmd != null)
                {
                    try
                    {
                        args[0] = ISOUtil.unPadLeft(line, ' '); // full line
                        if (cmd instanceof Command)
                        {
                            ((Command) cmd).exec(new LegacyCommandAdapter(ctx), args);
                        }
                        else if (cmd instanceof CLICommand)
                        {
                            ((CLICommand) cmd).exec(ctx, args);
                        }
                        return;
                    }
                    catch (Exception ex)
                    {
                        ctx.printThrowable(ex);
                    }
                }
            }
            catch (ClassNotFoundException ex)
            {
            }
            catch (Exception ex)
            {
                ctx.printThrowable(ex);
                break;
            }
        }
        ctx.println("Invalid command '" + verbatimCmd + "'");
    }

    private Object getCommand(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        Object cmd = commandCache.get(className);
        if (cmd == null)
        {
            Class<?> clazz = getClazz(className);
            final Object o = clazz.newInstance();
            if (o instanceof Command || o instanceof CLICommand)
            {
                commandCache.put(className, o);
                cmd = o;
            }
        }
        return cmd;
    }

    private Class<?> getClazz(String className) throws ClassNotFoundException
    {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    private String[] parseCommand(String line) throws IOException
    {
        if (line == null)
        {
            return new String[0];
        }

        StringTokenizer st = new StringTokenizer(line);
        String[] args = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
        {
            args[i] = st.nextToken();
        }
        return args;
    }

    public static class LegacyCommandAdapter extends CLI
    {
        CLIContext ctx;

        public LegacyCommandAdapter(CLIContext ctx) throws IOException
        {
            super(null, null, false);
            this.ctx = ctx;
        }

        @Override
        public void print(String s)
        {
            ctx.print(s);
        }

        @Override
        public void println(String s)
        {
            ctx.println(s);
        }

        @Override
        public boolean confirm(String prompt) throws IOException
        {
            return ctx.confirm(prompt);
        }

        @Override
        public Q2 getQ2()
        {
            return ctx.getQ2();
        }

        @Override
        public ConsoleReader getConsoleReader()
        {
            return ctx.getConsoleReader();
        }

        @Override
        public PrintStream getOutputStream()
        {
            return ctx.getOutputStream();
        }
    }
}
