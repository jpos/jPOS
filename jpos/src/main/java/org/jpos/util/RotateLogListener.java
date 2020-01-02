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

package org.jpos.util;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Rotates logs
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.core.Configurable
 * @since jPOS 1.2
 */
public class RotateLogListener extends SimpleLogListener 
    implements Configurable, Destroyable
{
    FileOutputStream f;
    String logName = null;
    int maxCopies = 0;
    long sleepTime = 0;
    long maxSize = 0;
    int  msgCount;
    boolean rotateOnStartup = false;
    String fileNamePattern = null;
    Rotate rotate;
    public static final int CHECK_INTERVAL = 100;
    public static final long DEFAULT_MAXSIZE = 10000000;

    ScheduleTimer timer = null;
    RotationAlgo rotationAlgo = null;

    public RotateLogListener () {
        super();
    }

   /**
    * Configure this RotateLogListener<br>
    * Properties:<br>
    * <ul>
    *  <li>file      base log filename</li>
    *  <li>[window]  in seconds (default 0 - never rotate)</li>
    *  <li>[count]   number of copies (default 0 == single copy)</li>
    *  <li>[maxsize] max log size in bytes (approx)</li>
    *  <li>[rotate-on-startup] Rotate file on q2 startup (default: false)</li>
    *  <li>[file-name-pattern] Comma-delimited codes for positional token replacement (case sensitive)</li>
    * </ul>
    *
    * <p>
    * Currently supported file-pattern-codes:
    * <ul>
    *     <li>h - hostname lookup</li>
    * </ul>
    * </p>
    * <p>
    * When code replacement fails, the token will be replaced by the code preceded by a # to give an indication
    * of what failed.  This type of failure will not result in a startup failure.
    * </p>
    * <p>
    * file is expected to contain %s tokens for replacement when enabled, as expected by String.format.
    * </p>
    * @param cfg Configuration 
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        if (maxCopies == 0) {
            maxCopies = cfg.getInt("copies", 0);
        }

        if (sleepTime == 0) {
            sleepTime = cfg.getInt("window") * 1000;
        }

        if (logName == null) {
            logName = cfg.get ("file");
        }

        // This check allows derived classes to have a different default.
        if (maxSize == 0) {
            maxSize = cfg.getLong("maxsize");
            maxSize = maxSize <= 0 ? DEFAULT_MAXSIZE : maxSize;
        }

        rotateOnStartup = cfg.getBoolean("rotate-on-startup", false);

        if (fileNamePattern == null) {
            fileNamePattern = cfg.get("file-name-pattern", null);
        }

        if (fileNamePattern != null && !fileNamePattern.isEmpty()) {
            logName = fileNameFromPattern(logName, fileNamePattern);
        }

        if (timer == null) {
            timer = () -> {
                Timer timer = DefaultTimer.getTimer();
                if (sleepTime != 0) timer.schedule(rotate = new Rotate(), sleepTime, sleepTime);
            };
        }

        if (rotationAlgo == null) {
            rotationAlgo = () -> {
                for (int i = maxCopies; i > 0; ) {
                    File dest = new File(logName + "." + i);
                    File source = new File(logName + (--i > 0 ? "." + i : ""));
                    dest.delete();
                    source.renameTo(dest);
                }
            };
        }

        runPostConfiguration();
    }

    public synchronized LogEvent log (LogEvent ev) {
        if (msgCount++ > CHECK_INTERVAL) {
            checkSize();
            msgCount = 0;
        }

        return super.log (ev);
    }
    protected synchronized void openLogFile() throws IOException {
        if (f != null)
            f.close();
        f = new FileOutputStream (logName, true);
        setPrintStream (new PrintStream(f));
        p.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        p.println ("<logger class=\"" + getClass().getName() + "\">");
    }
    protected synchronized void closeLogFile() throws IOException {
        p.println ("</logger>");
        if (f != null)
            f.close();
        f = null;
    }
    public void logRotate ()
        throws IOException
    {
        logRotate(false);
    }

    public synchronized void logRotate(boolean isStartup)
    throws IOException {
        if (!isStartup) {
            closeLogFile();
            super.close();
            setPrintStream(null);
        }
        rotationAlgo.rotate();
        openLogFile();
    }

    protected synchronized void logDebug (String msg) {
        if (p != null) {
            p.println ("<log realm=\"rotate-log-listener\" at=\""+new Date().toString() +"\">");
            p.println ("   "+msg);
            p.println ("</log>");
        }
    }
    protected void checkSize() {
        if (maxSize > 0) {
            File logFile = new File(logName);
            if (logFile.length() > maxSize) {
                try {
                    logDebug("maxSize (" + maxSize + ") threshold reached");
                    logRotate();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    protected String fileNameFromPattern(String inFileName, String patternCodes) {
        String[] computedValues;

        String[] codes = patternCodes.split(",");
        computedValues = new String[codes.length];
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals("h")) {
                try {
                    computedValues[i] = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    computedValues[i] = "#h";
                }
            } else if (codes[i].startsWith("e")) {
                try {
                    String envVar = codes[i].substring(2, codes[i].length()-1);
                    computedValues[i] = System.getenv(envVar);
                } catch (Exception e) {
                    computedValues[i] = "#e";
                }
            }
        }

        return String.format(inFileName, (Object[]) computedValues);
    }

    public class Rotate extends TimerTask {
        public void run() {
            try {
                logDebug ("time exceeded - log rotated");
                logRotate();
            } catch (Exception e) {
                logDebug(e.getMessage());
            }
        }
    }
    public void destroy () {
        if (rotate != null)
            rotate.cancel();
        try {
            closeLogFile ();
        } catch (IOException e) {
            logDebug(e.getMessage());
        }
    }

    @FunctionalInterface
    interface ScheduleTimer {
        void schedule() throws ConfigurationException;
    }

    @FunctionalInterface
    interface RotationAlgo {
        void rotate();
    }

    private void runPostConfiguration() throws ConfigurationException {
        try {
            if (rotateOnStartup) {
                logRotate(rotateOnStartup);
            } else {
                openLogFile();
            }
        } catch (IOException e) {
            throw new ConfigurationException (e);
        }
        timer.schedule();
    }
}
