/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

/** CLI command that echoes its argument back to the console. */
public class ECHO implements CLICommand
{
    /** Default constructor. */
    public ECHO() {
        super();
    }

    /**
     * Echoes the trimmed argument to the CLI context.
     * @param cli the CLI context
     * @param args command-line arguments
     * @throws Exception on error
     */
    public void exec(CLIContext cli, String[] args) throws Exception
    {
        String s = ISOUtil.unPadLeft(args[0].substring(4), ' ');
        cli.println(s);
    }
}

