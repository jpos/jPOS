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

import java.io.*;
import java.util.*;
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
    implements Runnable, Configurable
{
    FileOutputStream f;
    String logName;
    int maxCopies;
    int sleepTime;

    /**
     * @param name base log filename
     * @param t switch logs every t seconds
     * @param maxCopies number of old logs
     */
    public RotateLogListener (String logName, int t, int maxCopies) 
	throws IOException
    {
	super();
	this.logName   = logName;
	this.maxCopies = maxCopies;
	this.sleepTime = t;
	f = null;
	openLogFile ();
	if (t != 0) 
	    (new Thread(this)).start();
    }
    public RotateLogListener () {
	super();
	logName = null;
	maxCopies = 0;
	sleepTime = 0;
	f = null;
    }

   /**
    * Configure this RotateLogListener<br>
    * Properties:<br>
    * <ul>
    *  <li>file     base log filename
    *  <li>[window] in seconds (default 0 - never rotate)
    *  <li>[count]  number of copies (default 0 == single copy)
    * </ul>
    * @param cfg Configuration 
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
	throws ConfigurationException
    {
	maxCopies = cfg.getInt ("copies");
	sleepTime = cfg.getInt ("window");
	logName   = cfg.get ("file");
	try {
	    openLogFile();
	} catch (IOException e) {
	    throw new ConfigurationException (e);
	}
	if (sleepTime != 0) 
	    (new Thread(this)).start();
    }
    private void openLogFile() throws IOException {
	f = new FileOutputStream (logName, true);
	setPrintStream (new PrintStream(f));
    }
    public synchronized void logRotate ()
	throws IOException
    {
	setPrintStream (null);
	super.close();
	f.close();
	for (int i=maxCopies; i>0; ) {
	    File dest   = new File (logName + "." + i);
	    File source = new File (logName + ((--i > 0) ? ("." + i) : ""));
	    dest.delete();
	    source.renameTo(dest);
	}
	openLogFile();
    }
    public void run () {
	for (;;) {
	    try {
		Thread.sleep (sleepTime * 1000);
		logRotate();
	    } catch (InterruptedException e) { 
	    } catch (IOException e) {
		e.printStackTrace (System.err);
	    }
	}
    }
}
