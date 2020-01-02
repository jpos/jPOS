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
import java.io.IOException;

@SuppressWarnings("unused")
public class SHUTDOWN implements CLICommand {
    public void exec(CLIContext cli, String[] args) throws IOException {
        boolean shutdown;

        if (cli.isInteractive() && cli.getOutputStream() != System.out) {
            cli.println ("Can't shutdown remotely");
            return;
        }

        if (hasOption(args, "-f", "--force", "-fq")) {
            shutdown = true;
        } else {
            shutdown = cli.confirm("Confirm shutdown (Yes/No) ? ");
        }
        if (shutdown) {
            if (!hasOption (args, "-q", "--quiet", "-fq"))
                cli.println("Shutting down.");
            cli.getCLI().getQ2().shutdown();
        } else {
            cli.println("Q2 will continue running.");
        }
    }

    private boolean hasOption (String[] args, String... opts) {
        for (String s : args) {
            for (String o : opts) {
                if (s.equals(o))
                    return true;
            }
        }
        return false;
    }
}
