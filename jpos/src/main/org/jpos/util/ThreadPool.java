package uy.com.cs.jpos.util;

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2000/02/02 00:06:28  apr
 * CVS sync
 *
 * Revision 1.2  2000/01/17 18:26:06  apr
 * Supervise every 100 executions
 *
 * Revision 1.1  2000/01/11 17:16:52  apr
 * Added ThreadPool support
 *
 */

import java.io.PrintStream;
import uy.com.cs.jpos.util.BlockingQueue.Closed;

/**
 * Implements a ThreadPool with the ability to run simple Runnable
 * tasks as well as Jobs (supervised Runnable tasks)
 * @since 1.1
 * @author apr@cs.com.uy
 */
public class ThreadPool extends ThreadGroup implements LogProducer, Loggeable 
{
    private static int poolNumber=0;
    private static int threadNumber=0;
    private int maxPoolSize = 1;
    private BlockingQueue pool = new BlockingQueue();
    private Logger logger;
    private String realm;
    private int jobs = 0;

    public interface Supervised {
	public boolean expired ();
    }

    private class PooledThread extends Thread {
	Object currentJob = null;

	public PooledThread() {
	    super ((ThreadGroup) ThreadPool.this, 
		"PooledThread-" + (threadNumber++));
	    setDaemon(true);
	}
	public void run () {
	    String name = getName();
	    try {
		while (pool.ready()) {
		    Object job = pool.dequeue();
		    if (job instanceof Runnable) {
			setName (name + "-running");
			synchronized (this) {
			    currentJob = job;
			}
			((Runnable) job).run();
			setName (name + "-idle");
			synchronized (this) {
			    currentJob = null;
			}
		    }
		}
	    } catch (InterruptedException e) {
	    } catch (Closed e) {
	    }
	}
	public synchronized void supervise () {
	    if (currentJob != null && currentJob instanceof Supervised) 
		if ( ((Supervised)currentJob).expired() )
		    this.interrupt();
	}
    }

    /**
     * @param poolSize starting pool size
     * @param maxPoolSize maximum number of threads on this pool
     */
    public ThreadPool (int poolSize, int maxPoolSize) {
	super ("ThreadPool-" + poolNumber++);
	this.maxPoolSize = maxPoolSize;
	while (activeCount() < Math.min (poolSize, maxPoolSize))
	    new PooledThread().start();
    }
    public void close () {
	pool.close();
    }
    public synchronized void execute (Runnable action) throws Closed
    {
	if (!pool.ready())
	    throw new Closed();

	if (++jobs % 100 == 0 || pool.consumerCount() <= 0)
	    supervise();

	synchronized (pool) {
	    if (activeCount() < maxPoolSize && pool.consumerCount() <= 0)
		new PooledThread().start();
	}
	pool.enqueue (action);
    }
    public void dump (PrintStream p, String indent) {
	String inner = indent + "  ";
	p.println (indent + "<ThreadPool name=\""+getName()+"\">");
	if (!pool.ready())
	    p.println (inner  + "<closed/>");
	p.println (inner  + "<jobs>" +jobs+"</jobs>");
	p.println (inner  + "<size>" +activeCount()+"</size>");
	p.println (inner  + "<max>"  +maxPoolSize+"</max>");
	p.println (inner  + "<idle>"  + pool.consumerCount() +"</idle>");
	p.println (inner  + "<pending>"  +pool.pending()+"</pending>");
	p.println (indent + "</ThreadPool>");
    }

    public void supervise () {
	Thread[] t = new Thread[maxPoolSize];
	int cnt = enumerate (t);
	for (int i=0; i<cnt; i++) 
	    if (t[i] instanceof PooledThread)
		((PooledThread) t[i]).supervise();
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
    public static void main (String args[]) {
	ThreadPool p = new ThreadPool (100, 2000);
	Logger l = new Logger();
	l.addListener (new SimpleLogListener (System.out));
	p.setLogger (l, "ThreadPool");
	LogEvent evt = new LogEvent (p, "ThreadPoolStatus", p);
	new SystemMonitor (60000, l, "monitor");

	for (int i=0; i<5000; i++) {
	    p.execute (
		new Runnable() {
		    public void run() {
			System.out.print (".");
			try {
			    Thread.sleep (100);
			} catch (InterruptedException e) { 
			    System.out.println ("Got Interruption");
			}
		    }
		}
	    );
	    Thread.yield();
	}
	for (;;) {
	    try {
		Thread.sleep (10000);
	    } catch (InterruptedException e) { }
	    Logger.log (evt);
	}
	// p.close();
    }
}
