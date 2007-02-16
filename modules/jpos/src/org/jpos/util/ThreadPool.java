/*
 * Copyright (c) 2005 jPOS.org.  All rights reserved.
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

import java.io.PrintStream;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.BlockingQueue.Closed;
import org.jpos.util.NameRegistrar.NotFoundException;



/**
 * Implements a ThreadPool with the ability to run simple Runnable
 * tasks as well as Jobs (supervised Runnable tasks)
 * @since 1.1
 * @author apr@cs.com.uy
 */
public class ThreadPool extends ThreadGroup implements LogSource, Loggeable, Configurable, ThreadPoolMBean
{
    private static int poolNumber=0;
    private static int threadNumber=0;
    private int maxPoolSize = 1;
    private int available;
    private int running = 0;
    private BlockingQueue pool = new BlockingQueue();
    private Logger logger;
    private String realm;
    private int jobs = 0;
    public static final int DEFAULT_MAX_THREADS = 100;
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
                        try {
                            ((Runnable) job).run();
                            setName (name + "-idle");
                        } catch (Throwable t) {
                            setName (name + "-idle-"+t.getMessage());
                        }
                        synchronized (this) {
                            currentJob = null;
                            available++;
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
        this.maxPoolSize = maxPoolSize > 0 ? maxPoolSize : DEFAULT_MAX_THREADS ;
        this.available = this.maxPoolSize;
        init (poolSize);
    }
    /**
     * @param name pool name
     * @param poolSize starting pool size
     * @param maxPoolSize maximum number of threads on this pool
     */
    public ThreadPool (int poolSize, int maxPoolSize, String name) {
        super (name + "-" + poolNumber++);
        this.maxPoolSize = maxPoolSize > 0 ? maxPoolSize : DEFAULT_MAX_THREADS ;
        this.available = this.maxPoolSize;
        init (poolSize);
    }
    
    private void init(int poolSize){
        while (running < Math.min (poolSize > 0 ? poolSize : 1, maxPoolSize)) {
            running++;
            new PooledThread().start();
        }
    }
    /**
     * Default constructor for ThreadPool
     */
    public ThreadPool () {
        this(1, DEFAULT_MAX_THREADS);
    }
    public void close () {
        pool.close();
    }
    public synchronized void execute (Runnable action) throws Closed
    {
        if (!pool.ready())
            throw new Closed();

        if (++jobs % this.maxPoolSize == 0 || pool.consumerCount() <= 0)
            supervise();

        synchronized (pool) {
        	if (running < maxPoolSize && pool.consumerCount() <= 0) {
            	new PooledThread().start();
	            running++;
    	    }
    	}
        available--;
        pool.enqueue (action);
    }
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.println (indent + "<thread-pool name=\""+getName()+"\">");
        if (!pool.ready())
            p.println (inner  + "<closed/>");
        p.println (inner  + "<jobs>" +jobs+"</jobs>");
        p.println (inner  + "<size>" +available+"</size>");
        p.println (inner  + "<max>"  +maxPoolSize+"</max>");
        p.println (inner  + "<active>" + running +"</active>");
        p.println (inner  + "<idle>"  + pool.consumerCount() +"</idle>");
        p.println (inner  + "<pending>"  +pool.pending()+"</pending>");
        p.println (indent + "</thread-pool>");
    }

    /**
     * @return number of jobs processed by this pool
     */
    public int getJobCount () {
        return jobs;
    }
    /**
     * @return number of active threads
     */
    public int getPoolSize () {
        return available;
    }
    /**
     * @return max number of active threads allowed
     */
    public int getMaxPoolSize () {
        return maxPoolSize;
    }
    /**
     * @return number of idle threads
     */
    public int getIdleCount () {
        return pool.consumerCount ();
    }
    /**
     * @return number of available threads
     */
    synchronized public int getAvailableCount () {
        return available;
    }

    /**
     * @return number of Pending jobs
     */
    public int getPendingCount () {
        return pool.pending ();
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
    
   /** 
    * @param cfg Configuration object
    * @throws ConfigurationException
    */
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        maxPoolSize = cfg.getInt("max-size", DEFAULT_MAX_THREADS);
        init (cfg.getInt("initial-size"));
    }
    
    /** 
     * Retrieves a thread pool from NameRegistrar given its name, unique identifier.
     *
     * @param name Name of the thread pool to retrieve, must be the same as the name property of the thread-pool tag in the QSP config file
     * @throws NotFoundException thrown when there is not a thread-pool registered under this name.
     * @return returns the retrieved instance of thread pool
     */    
    public static ThreadPool getThreadPool(java.lang.String name) throws org.jpos.util.NameRegistrar.NotFoundException {
        return (ThreadPool)NameRegistrar.get("thread.pool." + name);
    }
}
