package org.jpos.q2.tomcat;

import org.jpos.q2.QBeanSupport;

/**
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 * @jmx:mbean description="Tomcat QBean" extends="org.jpos.q2.QBeanSupportMBean"
 */

public class Tomcat extends QBeanSupport implements TomcatMBean {
    String startScript;
    String shutdownScript;

    public void initService () throws Exception {
    }

    public void startService () throws Exception {
        Runtime.getRuntime().exec (startScript);
    }

    public void stopService () throws Exception {
        Runtime.getRuntime().exec (shutdownScript);
    }

    /**
     * @jmx:managed-attribute description="Tomcat startup script"
     */
    public void setStartScript (String scriptPath) {
        startScript = scriptPath;
    }

    /**
     * @jmx:managed-attribute description="Tomcat startup script"
     */
    public String getStartScript () {
        return startScript;
    }

    /**
     * @jmx:managed-attribute description="Tomcat shutdown script"
     */
    public void setShutdownScript (String scriptPath) {
        shutdownScript = scriptPath;
    }

    /**
     * @jmx:managed-attribute description="Tomcat shutdown script"
     */
    public String getShutdownScript () {
        return shutdownScript;
    }
}
