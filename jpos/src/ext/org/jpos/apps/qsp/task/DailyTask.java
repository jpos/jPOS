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

package org.jpos.apps.qsp.task;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.ThreadPool;
import org.jpos.core.Configuration;
import org.jpos.core.Configurable;
import org.jpos.core.ReConfigurable;
import org.jpos.core.ConfigurationException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class DailyTask 
	extends SimpleLogSource 
	implements Runnable, ReConfigurable
{
    Configuration cfg;
    Thread thisThread = null;
    ThreadPool pool;
    Runnable task;

    public DailyTask () {
	super();
    }

    public DailyTask(Runnable task, ThreadPool pool) {
	super();
	this.task = task;
	this.pool = pool;
    }

    public void setConfiguration (Configuration cfg) 
	throws ConfigurationException
    {

	if ( (task instanceof ReConfigurable) ||
	     (this.cfg == null && task instanceof Configurable) )
	    ((Configurable)task).setConfiguration (cfg);

	this.cfg = cfg;
	try {
	    getWhen ();
	    if (thisThread != null) 
		thisThread.interrupt();
	} catch (Exception e) {
	    throw new ConfigurationException 
		("property start invalid: "+cfg.get ("start"), e);
	}
    }
    public void run () {
	thisThread = Thread.currentThread();
	for (;;) {
	    waitUntilStartTime();
	    if (task != null) {
		if (pool != null)
		    pool.execute(task);
		else
		    task.run();
	    }
	    waitOneMinute();
	}
    }

    protected void waitUntilStartTime() {
	Date when = getWhen();
	for (;;) {
	    Date now = new Date();
	    if (now.before (when)) {
		long sleepTime = when.getTime() - now.getTime();
		Logger.log (new LogEvent (this, "sleeping",
		    (sleepTime/1000) + " secs until " + when.toString()));
		try {
		    Thread.sleep (sleepTime);
		} catch (InterruptedException e) { 
		    when = getWhen();
		}
	    } else
		break;
	}
    }

    protected void waitOneMinute () {
	try {
	    Thread.sleep (60000);
	} catch (InterruptedException e) { }
    }

    protected Date getWhen() {
	String s = cfg.get ("start")+":00:00";
        int hh = Integer.parseInt(s.substring (0, 2));
        int mm = Integer.parseInt(s.substring (3, 5));
        int ss = Integer.parseInt(s.substring (6, 8));

        Date now = new Date();
        Calendar cal = new GregorianCalendar();

        cal.setTime (now);
        cal.set (Calendar.HOUR_OF_DAY, hh);
        cal.set (Calendar.MINUTE, mm);
        cal.set (Calendar.SECOND, ss);

	Date when = cal.getTime();
	if (when.before(now)) 
	    when = new Date(when.getTime() + 24*60*60*1000);

	return when;
    }
    public void setLogger (Logger logger, String realm) {
	super.setLogger (logger, realm);
	if (task instanceof LogSource)
	    ((LogSource)task).setLogger (logger, realm + ".task");
    }
}
