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

package org.jpos.q2.cli.deploy;

import java.io.File;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
/**
* CLI implementation - Deploy subsystem
* 
* @author Felipph Calado - luizfelipph@gmail.com
*/
public class DISABLE implements CLICommand {

    /** Default constructor. */
    public DISABLE() {
        super();
    }

	@Override
	public void exec(CLIContext ctx, String[] args) throws Exception {
		if (args.length < 2) {
			ctx.println("Usage: disable <xml_file(no extension)>");
			return;
		}
		File deployDir = ctx.getCLI().getQ2().getDeployDir();
		File deploy = new File(deployDir, args[1] + ".xml").getCanonicalFile();
		File dest = new File(deployDir, args[1] + ".xml.off").getCanonicalFile();
		if (!deploy.toPath().startsWith(deployDir.getCanonicalFile().toPath())) {
			ctx.println("Invalid path: " + args[1]);
			return;
		}
		if (deploy.exists() && deploy.isFile()) {
			deploy.renameTo(dest);
			ctx.println("DISABLED: " + args[1]);
			return;
		}
		if (dest.exists()) {
			ctx.println("Already disabled: " + args[1]);
			return;
		}
		ctx.println("Can't find the bean descriptor: " + args[1]);
	}
}