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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jline.terminal.Terminal;
import org.jpos.iso.ISOUtil;

public class CLICommandInterface {
    CLIContext ctx;
    List<String> prefixes = new ArrayList<String>();

    public List<String> getPrefixes() {
        return prefixes;
    }

    public CLICommandInterface(CLIContext ctx) {
        this.ctx = ctx;
    }

    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }

    public void execCommand(String line) throws IOException {
        String args[] = parseCommand(line);
        if (args.length == 0) {
            return;
        }
        String verbatimCmd = args[0];
        String command = args[0].toUpperCase();
        String className = command;

        for (String prefix : prefixes) {
            if (!command.contains(".")) {
                className = prefix + command;
            }
            try {
                Object cmd = getCommand(className);
                if (cmd != null) {
                    try {
                        args[0] = ISOUtil.unPadLeft(line, ' '); // full line
                        if (cmd instanceof CLISubSystem) {
                            CLISubSystem ss = (CLISubSystem) cmd;
                            ctx.getCLI().setPrompt(ss.getPrompt(ctx, args), ss.getCompletionPrefixes(ctx, args));
                        }
                        if (cmd instanceof CLICommand) {
                            ((CLICommand) cmd).exec(ctx, args);
                        } else if (cmd instanceof Command) {
                            Terminal t = ctx.getReader().getTerminal();
                            ((Command) cmd).exec (t.input(), t.output(), t.output(), args);
                        }
                        return;
                    } catch (Exception ex) {
                        ctx.printThrowable(ex);
                    }
                }
            } catch (ClassNotFoundException ignored) {
                // NOPMD
            } catch (Exception ex) {
                ctx.printThrowable(ex);
                break;
            }
        }
        ctx.println("Invalid command '" + verbatimCmd + "'");
    }

    private Object getCommand(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.loadClass(className).newInstance();
    }

    public String[] parseCommand(String line) throws IOException {
        if (line == null) {
            return new String[0];
        }

        List<String> matchList = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(line);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }
        String[] args = new String[matchList.size()];
        matchList.toArray(args);
        return args;
    }
}
