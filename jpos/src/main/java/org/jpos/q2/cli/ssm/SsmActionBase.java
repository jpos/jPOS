/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

package org.jpos.q2.cli.ssm;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.SSM;
import org.jpos.security.SMException;
import org.jpos.security.jceadapter.JCESecurityModule;

/**
 * Provides base for most SSM based commands.  The exec method wraps the processing for derived classes.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public abstract class SsmActionBase implements CLICommand {

    protected abstract boolean checkUsage(CLIContext cli, String[] strings);

    protected abstract void doCommand(
            CLIContext cli,
            JCESecurityModule sm,
            short keyLength,
            String[] strings)
            throws SMException;

    @Override
    public void exec(CLIContext cli, String[] strings) throws Exception {
        if (!checkUsage(cli, strings)) return;
        try {
            short keyLength = (short) Integer.parseInt(strings[1]);
            JCESecurityModule securityModule = SSM.getSecurityModule(cli);
            if (securityModule == null) {
                cli.println("No security module initialized.  Use the INIT command to initialize one.");
                return;
            }
            doCommand(cli, securityModule, keyLength, strings);
        } catch (SMException sme) {
            cli.printThrowable(sme);
        } catch (NumberFormatException nfe) {
            cli.println("Invalid KeyLength");
        }
    }
}
