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
import org.jpos.transaction.TransactionManager;
import org.jpos.transaction.TransactionStatusEvent;
import org.jpos.transaction.TransactionStatusListener;
import org.jpos.util.NameRegistrar;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unused")
public class TMMON implements CLICommand, TransactionStatusListener {
    PrintStream p;
    CLIContext cli;
    boolean ansi;

    public void exec(CLIContext cli, String[] args) throws Exception {
        this.p = new PrintStream(cli.getReader().getTerminal().output());
        this.cli = cli;
        this.ansi = false; // cli.getReader().getTerminal()
        if (args.length == 1) {
            usage(cli);
            return;
        }
        for (int i = 1; i < args.length; i++) {
            try {
                Object obj = NameRegistrar.get(args[i]);
                if (obj instanceof TransactionManager) {
                    ((TransactionManager) obj).addListener(this);
                } else {
                    cli.println("Object '" + args[i]
                      + "' is not an instance of TransactionManager (" + obj.toString() + ")");
                }
            } catch (NameRegistrar.NotFoundException e) {
                cli.println("TransactionManager '" + args[i] + "' not found -- ignored.");
            }
        }

        cli.getReader().readLine();

        for (int i = 1; i < args.length; i++) {
            try {
                Object obj = NameRegistrar.get(args[i]);
                if (obj instanceof TransactionManager) {
                    ((TransactionManager) obj).removeListener(this);
                }
            } catch (NameRegistrar.NotFoundException ignored) {
                // NOPMD ok to happen
            }
        }
    }

    public void usage(CLIContext cli) {
        cli.println("Usage: tmmon [tm-name] [tm-name] ...");
        showTMs(cli);
    }

    private void showTMs(CLIContext cli) {
        NameRegistrar nr = NameRegistrar.getInstance();
        int maxw = 0;
        Iterator iter = NameRegistrar.getAsMap().entrySet().iterator();
        StringBuilder sb = new StringBuilder("available transaction managers:");
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if (entry.getValue() instanceof TransactionManager) {
                sb.append(' ');
                sb.append(key);
            }
        }
        cli.println(sb.toString());
    }

    public void update(TransactionStatusEvent e) {
        cli.println(e.toString());
    }
}

