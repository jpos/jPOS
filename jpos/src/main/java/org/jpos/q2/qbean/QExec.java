/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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
import java.util.ArrayList;
import java.util.List;

/**
* QBean for starting and stopping scripts or programs.
* <pre>
* Example xml:
*  &lt;exec class="org.jpos.q2.qbean.QExec"&gt;
*    &lt;attr name="startScript"&gt;YOUR PATH TO PROGRAM&lt;/attr&gt;
*    &lt;attr name="shutdownScript"&gt;YOUR PATH TO PROGRAM&lt;/attr&gt;
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

    private void exec (String script) throws IOException, InterruptedException {
        if (script != null) {
            ProcessBuilder pb = new ProcessBuilder (parseCommandLine(script));
            Process p = pb.start();
            BufferedReader in = p.inputReader();
            LogEvent evt = getLog().createInfo("--- " + script + " ---");
            String line;
            while ((line = in.readLine()) != null) {
                evt.addMessage(line);
            }
            p.waitFor();
            Logger.log(evt);
        }
    }

    public static List<String> parseCommandLine(String commandLine) {
        if (commandLine == null || commandLine.isEmpty())
            throw new IllegalArgumentException("Empty command");
        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inQuotes = false;
        boolean isEscaped = false;

        for (int i = 0; i < commandLine.length(); i++) {
            char c = commandLine.charAt(i);

            if (c == '\\' && !isEscaped) {
                isEscaped = true;
                continue;
            }

            if (c == '\"' && !isEscaped) {
                inQuotes = !inQuotes;
            } else {
                if (c == ' ' && !inQuotes) {
                    if (currentArg.length() > 0) {
                        args.add(currentArg.toString());
                        currentArg.setLength(0);
                    }
                } else {
                    currentArg.append(c);
                }
            }

            if (isEscaped) {
                isEscaped = false;
            }
        }

        if (currentArg.length() > 0) {
            args.add(currentArg.toString());
        }

        return args;
    }

}
