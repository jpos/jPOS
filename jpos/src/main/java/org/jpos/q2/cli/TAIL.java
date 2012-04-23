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

package org.jpos.q2.cli;

import jline.ANSIBuffer;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.util.LogEvent;
import org.jpos.util.LogListener;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class TAIL implements CLICommand, LogListener
{
    PrintStream p;
    CLIContext cli;
    boolean ansi;

    public void exec(CLIContext cli, String[] args) throws Exception
    {
        this.p = cli.getOutputStream();
        this.cli = cli;
        this.ansi = cli.getConsoleReader().getTerminal().isANSISupported();
        if (args.length == 1)
        {
            usage(cli);
            return;
        }
        for (int i = 1; i < args.length; i++)
        {
            try
            {
                Logger logger = (Logger) NameRegistrar.get("logger." + args[i]);
                logger.addListener(this);
            }
            catch (NameRegistrar.NotFoundException e)
            {
                cli.println("Logger " + args[i] + " not found -- ignored.");
            }
        }
        cli.getConsoleReader().readCharacter(new char[]{'q', 'Q'});
        for (int i = 1; i < args.length; i++)
        {
            try
            {
                Logger logger = (Logger) NameRegistrar.get("logger." + args[i]);
                logger.removeListener(this);
            }
            catch (NameRegistrar.NotFoundException e) { }
        }
    }

    public void usage(CLIContext cli)
    {
        cli.println("Usage: tail [log-name] [log-name] ...");
        showLoggers(cli);
    }

    public synchronized LogEvent log(LogEvent ev)
    {
        if (p != null)
        {
            Date d = new Date(System.currentTimeMillis());
            ANSIBuffer ab = new ANSIBuffer();
            ab.setAnsiEnabled(ansi);
            cli.println(
                    ab.bold(
                            ev.getSource().getLogger().getName() +
                            ": " + ev.getRealm() + " " + d.toString() + "." + d.getTime() % 1000
                    ).toString(ansi)
            );
            ev.dump(p, " ");
            p.flush();
        }
        return ev;
    }

    private void showLoggers(CLIContext cli)
    {
        NameRegistrar nr = NameRegistrar.getInstance();
        int maxw = 0;
        Iterator iter = nr.getMap().entrySet().iterator();
        StringBuffer sb = new StringBuffer("available loggers:");
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (key.startsWith("logger.") && entry.getValue() instanceof Logger)
            {
                sb.append(' ');
                sb.append(key.substring(7));
            }
        }
        cli.println(sb.toString());
    }
}

