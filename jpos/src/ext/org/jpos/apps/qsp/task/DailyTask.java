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
