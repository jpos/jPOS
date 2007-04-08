package org.jpos.q2.misc;

import org.jpos.q2.QBeanSupport;

/**
* QBean for starting and stopping scripts or programs.
* <pre>
* Example xml:
*  &lt;exec class="org.jpos.q2.qbean.QExec"&gt;
*    &lt;attr name="start"&gt;YOUR PATH TO PROGRAM&lt;/attr&gt;
*    &lt;attr name="shutdown"&gt;YOUR PATH TO PROGRAM&lt;/attr&gt;
*  &lt;/exec&gt;
* </pre>
* @author Alwyn Schoeman
* @version $Revision$ $Date$
* @jmx:mbean description="QExec QBean" extends="org.jpos.q2.QBeanSupportMBean"
*/ 

public class QExec extends QBeanSupport implements QExecMBean {
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
     * @jmx:managed-attribute description="Program startup script"
     */
    public void setStartScript (String scriptPath) {
        startScript = scriptPath;
    }

    /**
     * @jmx:managed-attribute description="Program startup script"
     */
    public String getStartScript () {
        return startScript;
    }

    /**
     * @jmx:managed-attribute description="Program shutdown script"
     */
    public void setShutdownScript (String scriptPath) {
        shutdownScript = scriptPath;
    }

    /**
     * @jmx:managed-attribute description="Program shutdown script"
     */
    public String getShutdownScript () {
        return shutdownScript;
    }
}
