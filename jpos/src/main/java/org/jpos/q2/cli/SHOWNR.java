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

import org.jpos.iso.ISOUtil;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.util.Loggeable;
import org.jpos.util.NameRegistrar;

import java.util.Iterator;
import java.util.Map;

public class SHOWNR implements CLICommand {
    public void exec(CLIContext cli, String[] args) throws Exception {
        boolean all = args.length > 1 && "-a".equals(args[1]);
        int i = 1;
        if (all) {
            i++;
        }
        if (args.length > i) {
            showOne(cli, args[i], all); }
        else {
            showAll(cli, all);
        }
    }

    private void showOne(CLIContext cli, String name, boolean detail) {
        try {
            Object obj = NameRegistrar.get(name);
            cli.println(name + " : " + obj.toString());
            if (detail && obj instanceof Loggeable) {
                cli.printLoggeable((Loggeable) obj, "");
            }
        }
        catch (NameRegistrar.NotFoundException e) {
            cli.println("Object not found in NameRegistrar");
        }
    }

    private void showAll(CLIContext cli, boolean detail) {
        NameRegistrar nr = NameRegistrar.getInstance();
        int maxw = 0;
        Iterator iter = NameRegistrar.getAsMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            maxw = Math.max(maxw, entry.getKey().toString().length());
        }
        iter = NameRegistrar.getAsMap().entrySet().iterator();
        maxw++;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            cli.println(
                    ISOUtil.strpad(entry.getKey().toString(), maxw) +
                    entry.getValue().toString()
            );
            if (detail && entry.getValue() instanceof Loggeable) {
                cli.printLoggeable((Loggeable) entry.getValue(), "   ");
            }
        }
    }
}
