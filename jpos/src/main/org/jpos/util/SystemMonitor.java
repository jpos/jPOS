package org.jpos.util;

import java.io.*;
import java.util.*;

/**
 * Periodically dumps Thread and memory usage
 * @author apr@cs.com.uy
 * @version $Id$
 * @see Logger
 */
public class SystemMonitor implements Runnable, LogSource, Loggeable
{
    private Logger logger = null;
    private String realm  = null;
    private int sleepTime = 0;
    private int delay     = 0;
    private Thread thread = null;

    /**
     * @param sleepTime sleep
     * @param logger current logger
     * @param realm  instance realm
     */
    public SystemMonitor (int sleepTime, Logger logger, String realm) {
	setLogger (logger, realm);
	this.sleepTime = sleepTime;
	startThread();
    }
    
    private void startThread() {
	if (thread != null)
	    thread.interrupt();
	else if (sleepTime > 0) {
	    thread = new Thread(this);
	    thread.setName ("SystemMonitor");
	    thread.setPriority (Thread.MIN_PRIORITY);
	    thread.start();
	}
    }

    /**
     * @param sleepTime new sleepTime;
     */
    public void setSleepTime (int sleepTime) {
	this.sleepTime = sleepTime;
	startThread();
    }

    void dumpThreads (ThreadGroup g, PrintStream p, String indent) {
	Thread[] list = new Thread[g.activeCount()+5];
	int nthreads = g.enumerate(list);
	for (int i=0; i<nthreads; i++) 
	    p.println (indent + list[i]);
    }

    public void showThreadGroup (ThreadGroup g, PrintStream p, String indent) {
	if (g.getParent() != null)
	    showThreadGroup (g.getParent(), p, indent + "  ");
	else
	    dumpThreads (g, p, indent + "    ");
    }

    public void run() {
	for (;;) {
	    Logger.log (new LogEvent (this, "SystemMonitor", this));
	    try {
		long expected = System.currentTimeMillis() + sleepTime;
		Thread.sleep (sleepTime);
		delay = (int) (System.currentTimeMillis() - expected);
	    } catch (InterruptedException e) { }
	}
    }
    public void dump (PrintStream p, String indent) {
	String newIndent = indent + "  ";
	Runtime r = Runtime.getRuntime();
	p.println (newIndent+" freeMemory="+r.freeMemory());
	p.println (newIndent+"totalMemory="+r.totalMemory());
	p.println (newIndent+"inUseMemory="+(r.totalMemory()-r.freeMemory()));
	p.println (newIndent+"      delay="+delay+" ms");
	p.println (newIndent+"    threads="+Thread.activeCount());
	showThreadGroup (Thread.currentThread().getThreadGroup(), p, newIndent);
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
}


