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
