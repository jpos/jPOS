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

package org.jpos.util;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
    String logName;
    int maxCopies;
    long sleepTime;
    long maxSize;
    int  msgCount;
    Rotate rotate;
    public static final int CHECK_INTERVAL = 100;
    public static final long DEFAULT_MAXSIZE = 10000000;

    /**
     * @param name base log filename
     * @param sleepTime switch logs every t seconds
     * @param maxCopies number of old logs
     * @param maxSize in bytes 
     */

    public RotateLogListener 
        (String logName, int sleepTime, int maxCopies, long maxSize) 
        throws IOException
    {
        super();
        this.logName   = logName;
        this.maxCopies = maxCopies;
        this.sleepTime = sleepTime * 1000;
        this.maxSize   = maxSize;
        f = null;
        openLogFile ();
        Timer timer = DefaultTimer.getTimer();
        if (sleepTime != 0) {
            timer.schedule (rotate = new Rotate(), 
                    this.sleepTime, this.sleepTime);
        }
    }

    public RotateLogListener 
        (String logName, int sleepTime, int maxCopies) 
        throws IOException
    {
        this (logName, sleepTime, maxCopies, DEFAULT_MAXSIZE); 
    }

    public RotateLogListener () {
        super();
    }

   /**
    * Configure this RotateLogListener<br>
    * Properties:<br>
    * <ul>
    *  <li>file      base log filename
    *  <li>[window]  in seconds (default 0 - never rotate)
    *  <li>[count]   number of copies (default 0 == single copy)
    *  <li>[maxsize] max log size in bytes (aprox)
    * </ul>
    * @param cfg Configuration 
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        maxCopies = cfg.getInt  ("copies");
        sleepTime = cfg.getInt  ("window") * 1000;
        logName   = cfg.get     ("file");
        maxSize   = cfg.getLong ("maxsize");
        maxSize   = maxSize <= 0 ? DEFAULT_MAXSIZE : maxSize;

        try {
            openLogFile();
        } catch (IOException e) {
            throw new ConfigurationException (e);
        }
        Timer timer = DefaultTimer.getTimer();
        if (sleepTime != 0) 
            timer.schedule (rotate = new Rotate(), sleepTime, sleepTime);
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
    public synchronized void logRotate ()
        throws IOException
    {
        closeLogFile ();
        super.close ();
        setPrintStream (null);
        for (int i=maxCopies; i>0; ) {
            File dest   = new File (logName + "." + i);
            File source = new File (logName + ((--i > 0) ? ("." + i) : ""));
            dest.delete();
            source.renameTo(dest);
        }
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
        File logFile = new File (logName);
        if (logFile.length() > maxSize) {
            try {
                logDebug ("maxSize ("+maxSize+") threshold reached");
                logRotate();
            } catch (IOException e) {
                e.printStackTrace (System.err);
            }
        }
    }
    public class Rotate extends TimerTask {
        public void run() {
            try {
                logDebug ("time exceeded - log rotated");
                logRotate();
            } catch (IOException e) {
                e.printStackTrace (System.err);
            }
        }
    }
    public void destroy () {
        if (rotate != null)
            rotate.cancel ();
        try {
            closeLogFile ();
        } catch (IOException e) {
            // nothing we can do.
        }
    }
}
