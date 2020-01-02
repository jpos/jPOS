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

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.Q2;
import org.jpos.q2.QBean;
/**
* CLI implementation - Deploy subsystem
* 
* @author Felipph Calado - luizfelipph@gmail.com
*/
public class SERVICE implements CLICommand {

    @Override
    public void exec(CLIContext ctx, String[] args) throws Exception {

        if (args.length < 3) {
            ctx.println("Usage: service <service> start|stop|restart");
            return;
        }
        if (!"start".equals(args[2]) && !"stop".equals(args[2]) && !"restart".equals(args[2])) {
            ctx.println("Invalid operation: " + args[2]);
            return;
        }
        String command = "";
        int waiting = 0;
        int executed = 0;

        switch (args[2]) {
            case "start":
                command = "start";
                waiting = QBean.STARTING;
                executed = QBean.STARTED;
                break;
            case "stop":
                command = "stop";
                waiting = QBean.STOPPING;
                executed = QBean.STOPPED;
                break;
            case "restart":
                args[2] = "stop";
                this.exec(ctx, args);
                args[2] = "start";
                this.exec(ctx, args);
                return;
        }

        MBeanServer server = ctx.getCLI().getQ2().getMBeanServer();

        ObjectName on = new ObjectName(Q2.QBEAN_NAME + args[1]);
        Set<ObjectInstance> b = server.queryMBeans(on, null);
        Iterator<ObjectInstance> it = b.iterator();
        if (it.hasNext()) {
            ObjectInstance instance = it.next();
            if ((Integer) server.getAttribute(instance.getObjectName(), "State") == executed) {
                ctx.println(args[2] + ": " + args[1] + " already done.");
                return;
            }

            server.invoke(instance.getObjectName(), command, null, null);
            try {
                for (int i = 0; i < 100; i++) {
                    if ((Integer) server.getAttribute(instance.getObjectName(), "State") == executed) {
                        ctx.println(args[2] + ": " + args[1] + " done");
                        return;
                    }
                    Thread.sleep(2000);// wait to stop...
                    ctx.println("waiting for " + args[1] + " to " + args[2]);
                }
                ctx.println(args[2] + ": " + args[1] + " is stuck in " + waiting + " state");
                return;
            } catch (Exception e) {
                ctx.println("Can't get state: " + args[1]);
            }

            return;

        } else {
            ctx.println(args[1] + " not found");
        }

    }
}