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

package org.jpos.q2.qbean;

import org.jpos.q2.QBeanSupport;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.*;

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
*/

public class QExec extends QBeanSupport implements QExecMBean {
    String startScript;
    String shutdownScript;

    @Override
    protected void startService () throws Exception {
        exec(startScript);
    }

    @Override
    protected void stopService () throws Exception {
        exec(shutdownScript);
    }

    public void setStartScript (String scriptPath) {
        startScript = scriptPath;
    }

    public String getStartScript () {
        return startScript;
    }

    public void setShutdownScript (String scriptPath) {
        shutdownScript = scriptPath;
    }

    public String getShutdownScript () {
        return shutdownScript;
    }

    private void exec (String script) throws IOException {
        if (script != null) {
            Process p = Runtime.getRuntime().exec(script);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()) );
            LogEvent evt = getLog().createInfo("--- " + script + " ---");
            String line;
            while ((line = in.readLine()) != null) {
                evt.addMessage(line);
            }
            Logger.log(evt);
        }
    }
}
