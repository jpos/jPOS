package org.jpos.q2.jetty;

import org.jpos.q2.QBeanSupport;
import org.mortbay.jetty.Server;

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @jmx:mbean description="Jetty QBean" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class Jetty extends QBeanSupport implements JettyMBean {
    Server server;
    String config;
    public void initService () throws Exception {
        server = new Server(config);
    }
    public void startService () throws Exception {
        server.start ();
    }
    public void stopService () throws Exception {
        server.stop ();
    }
    /**
     * @jmx:managed-attribute description="Configuration File"
     */
    public void setConfig (String config) {
        this.config = config;
    }
    /**
     * @jmx:managed-attribute description="Configuration File"
     */
    public String getConfig () {
        return config;
    }
}

