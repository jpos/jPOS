package org.jpos.q2.cli.deploy;

import javax.management.ObjectName;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.Q2;
import org.jpos.q2.Q2.QEntry;

import java.util.Set;
import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;

public class PS implements CLICommand {

	@Override
	public void exec(CLIContext ctx, String[] args) throws Exception {
		final ObjectName on = new ObjectName("Q2:type=qbean,service=*");
		
		Q2 q2 = ctx.getCLI().getQ2();
		
		MBeanServer server = q2.getMBeanServer();
		Set<ObjectInstance> b = server.queryMBeans(on, null);
		Iterator<ObjectInstance> it = b.iterator();
		while (it.hasNext()) {
			ctx.println(it.next().getObjectName().getKeyProperty("service"));
		}
	}


}
