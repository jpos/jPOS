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

package org.jpos.q2.cli;

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

public class TAIL implements CLICommand, LogListener {
    PrintStream p;
    CLIContext cli;
    boolean ansi;

    public void exec(CLIContext cli, String[] args) throws Exception {
        this.p = new PrintStream(cli.getReader().getTerminal().output());
        this.cli = cli;
        this.ansi = false; // cli.getReader().getTerminal().isAnsiSupported();
        if (args.length == 1) {
            usage(cli);
            return;
        }
        for (int i = 1; i < args.length; i++) {
            try {
                Logger logger = (Logger) NameRegistrar.get("logger." + args[i]);
                logger.addListener(this);
            } catch (NameRegistrar.NotFoundException e) {
                cli.println("Logger " + args[i] + " not found -- ignored.");
            }
        }
        // cli.getReader().readCharacter(new char[]{'q', 'Q'});
        cli.getReader().readLine();
        for (int i = 1; i < args.length; i++) {
            try {
                Logger logger = (Logger) NameRegistrar.get("logger." + args[i]);
                logger.removeListener(this);
            } catch (NameRegistrar.NotFoundException ignored) {
                // NOPMD OK to happen
            }
        }
    }

    public void usage(CLIContext cli) {
        cli.println("Usage: tail [log-name] [log-name] ...");
        showLoggers(cli);
    }

    public synchronized LogEvent log(LogEvent ev) {
        if (p != null) {
            Date d = new Date(System.currentTimeMillis());
//            if (ansi)
//                cli.getOutputStream().write(1); // BOLD
            cli.println(
              ev.getSource().getLogger().getName() +
                ": " + ev.getRealm() + " " + d.toString() + "." + d.getTime() % 1000
            );
            ev.dump(p, " ");
//            if (ansi)
//                cli.getOutputStream().write(0); // OFF

            p.flush();
        }
        return ev;
    }

    private void showLoggers(CLIContext cli) {
        NameRegistrar nr = NameRegistrar.getInstance();
        int maxw = 0;
        Iterator iter = NameRegistrar.getAsMap().entrySet().iterator();
        StringBuilder sb = new StringBuilder("available loggers:");
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (key.startsWith("logger.") && entry.getValue() instanceof Logger) {
                sb.append(' ');
                sb.append(key.substring(7));
            }
        }
        cli.println(sb.toString());
    }
}

