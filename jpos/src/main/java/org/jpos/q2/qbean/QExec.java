/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.q2.qbean;

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
