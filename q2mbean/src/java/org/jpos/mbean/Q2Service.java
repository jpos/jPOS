/*
 * File: Q2Service.java
 * Package: package org.jboss.mbean;
 * Project: Q2MBean
 * 
 * Created on Jan 29, 2004
 *
 */
package org.jpos.mbean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.jboss.system.ServiceMBeanSupport;
import org.jpos.q2.Q2;

/**
 * @jmx.mbean
 *      description="Q2 MBean"
 *      name="Q2:name=Q2Service"
 *      extends="org.jboss.system.ServiceMBean"
 * 
 * @jboss.service
 *      servicefile="jboss"
 * 
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 *
 */
public class Q2Service extends ServiceMBeanSupport implements Q2ServiceMBean, Runnable {
    private Q2 q2Server = null;

    protected void startService() throws Exception {
        super.startService();
        log.info ("Q2Service starting");
        String[] deployPath = {"-d","q2/deploy"};
        q2Server = new Q2(deployPath);
        new Thread(this).start();
    }

    public void run() {
        try {
            q2Server.start();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        }
    }
    
    protected void stopService() throws Exception {
        super.stopService();
        log.info ("Q2Service stopping");
        q2Server.shutdown();
    }

}
