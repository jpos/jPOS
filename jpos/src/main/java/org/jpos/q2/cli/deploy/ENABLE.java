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

package org.jpos.q2.cli.deploy;

import java.io.File;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
/**
* CLI implementation - Deploy subsystem
* 
* @author Felipph Calado - luizfelipph@gmail.com
*/
public class ENABLE implements CLICommand {

    @Override
    public void exec(CLIContext ctx, String[] args) throws Exception {
        if (args.length < 2) {
            ctx.println("Usage: enable <xml_file(no extension)>");
            return;
        }
        File deployDir = ctx.getCLI().getQ2().getDeployDir();
        File deploy = new File(deployDir.getAbsolutePath() + "/" + args[1] + ".xml.off");
        if (deploy.exists() && deploy.isFile()) {
            File dest = new File(deployDir.getAbsolutePath() + "/" + args[1] + ".xml");
            deploy.renameTo(dest);
            ctx.println("ENABLED: " + args[1]);
            return;
        }
        File dest = new File(deployDir.getAbsolutePath() + "/" + args[1] + ".xml");
        if (dest.exists()) {
            ctx.println("Already enabled: " + args[1]);
            return;
        }
        ctx.println("Can't find the bean descriptor: " + args[1]);
        return;
    }
}
