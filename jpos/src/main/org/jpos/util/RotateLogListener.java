/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

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
    private synchronized void openLogFile() throws IOException {
        if (f != null)
            f.close();
        f = new FileOutputStream (logName, true);
        setPrintStream (new PrintStream(f));
    }
    private synchronized void closeLogFile() throws IOException {
        if (f != null)
            f.close();
        f = null;
    }
    public synchronized void logRotate ()
        throws IOException
    {
        super.close ();
        setPrintStream (null);
        closeLogFile ();
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
    private void checkSize() {
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
