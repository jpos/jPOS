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

import javax.management.ObjectName;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.Q2;
import org.jpos.q2.QBean;

import java.util.Set;
import java.util.Iterator;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
/**
* CLI implementation - Deploy subsystem
* 
* @author Felipph Calado - luizfelipph@gmail.com
*/
public class PS implements CLICommand {

    @Override
    public void exec(CLIContext ctx, String[] args) throws Exception {
        final ObjectName on = new ObjectName("Q2:type=qbean,service=*");
        MBeanServer server = ctx.getCLI().getQ2().getMBeanServer();
        Set<ObjectInstance> b = server.queryMBeans(on, null);
        Iterator<ObjectInstance> it = b.iterator();
        while (it.hasNext()) {
            ObjectInstance instance = it.next();
            int status = (Integer) server.getAttribute(instance.getObjectName(), "State");
            if (status == QBean.STARTED) {
                ctx.println(instance.getObjectName().getKeyProperty("service") + "\t\t" + instance.getClassName());
            }

        }

    }

}
