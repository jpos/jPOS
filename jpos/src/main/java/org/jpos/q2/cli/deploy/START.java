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

public class START implements CLICommand {

	@Override
	public void exec(CLIContext ctx, String[] args) throws Exception {

		if (args.length < 2) {
			ctx.println("Usage: start <service>");
			return;
		}

		ObjectName on = new ObjectName("Q2:type=qbean,service=" + args[1]);
		Q2 q2 = ctx.getCLI().getQ2();

		MBeanServer server = q2.getMBeanServer();
		Set<ObjectInstance> b = server.queryMBeans(on, null);
		Iterator<ObjectInstance> it = b.iterator();

		if (it.hasNext()) {
			ObjectInstance instance = it.next();
			if((Integer) server.getAttribute(instance.getObjectName(), "State") == QBean.STARTED){
				ctx.println(args[1]+ " is already started");
				return;
			}
			server.invoke(instance.getObjectName(), "start", null, null);

			int status;
			try {
				while ((Integer) server.getAttribute(instance.getObjectName(), "State") == QBean.STARTING) {
					Thread.sleep(2000);// wait to start... is necessary??
					ctx.println("waiting for " + args[1] + " to stop");
				}
				status = (Integer) server.getAttribute(instance.getObjectName(), "State");
				if (status == QBean.STARTED) {
					ctx.println("Started: " + args[1]);
				}
			} catch (Exception e) {
				ctx.println("Can't get state: " + args[1]);
			}

			return;

		} else {
			ctx.println(args[1] + "not found");
		}

	}
}