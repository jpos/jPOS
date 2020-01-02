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

import org.jpos.q2.CLIContext;
import org.jpos.q2.CLISubSystem;

/**
 * CLI implementation - Deploy subsystem
 * 
 * @author Felipph Calado - luizfelipph@gmail.com
 */
public class DEPLOY implements CLISubSystem {
    @Override
    public String getPrompt(CLIContext ctx, String[] args) {
        return "deploy> ";
    }

    @Override
    public String[] getCompletionPrefixes(CLIContext ctx, String[] args) {
        return new String[] { "org.jpos.q2.cli.deploy." };
    }
}
