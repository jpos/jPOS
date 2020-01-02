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
import org.jpos.q2.Q2;
import org.jpos.q2.install.ModuleUtils;
import org.jpos.util.PGPHelper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class VERSION implements CLICommand {
    public void exec(CLIContext cli, String[] args) throws IOException, NoSuchAlgorithmException {
        boolean all = args.length > 1 && args[1].startsWith("-a");
        if (args.length > 1 && !all) {
            cli.println ("Unknown option");
            return;
        }
        cli.println(Q2.getVersionString());
        if (all) {
            cli.println(ModuleUtils.getModulesUUIDs().stream().collect(Collectors.joining(System.lineSeparator())));
            cli.println(ModuleUtils.getSystemHash());
            cli.println(PGPHelper.getLicenseeHash());
        }
    }
}
