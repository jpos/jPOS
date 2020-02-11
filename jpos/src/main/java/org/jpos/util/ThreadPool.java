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
import org.jpos.util.BlockingQueue.Closed;
import org.jpos.util.NameRegistrar.NotFoundException;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements a ThreadPool with the ability to run simple Runnable
 * tasks as well as Jobs (supervised Runnable tasks)
 * @since 1.1
 * @author apr@cs.com.uy
 * @deprecated Used Executor framework
 */
public class ThreadPool extends ThreadGroup implements LogSource, Loggeable, Configurable, ThreadPoolMBean {
    private static AtomicInteger poolNumber = new AtomicInteger(0);
    private static AtomicInteger threadNumber = new AtomicInteger(0);
    private int maxPoolSize = 1;
    private int available;
    private int running = 0;
    private int active  = 0;
    private BlockingQueue pool = new BlockingQueue();
    private Logger logger;
    private String realm;
    private int jobs = 0;
    private final String namePrefix;
    public static final int DEFAULT_MAX_THREADS = 100;
    
    public interface Supervised {
        boolean expired();
    }

    private class PooledThread extends Thread {
        Object currentJob = null;

        public PooledThread() {
            super (ThreadPool.this,
                    ThreadPool.this.namePrefix + ".PooledThread-" + threadNumber.getAndIncrement());
            setDaemon(true);
        }

        public void run () {
            String name = getName();
            try {
                while (pool.ready()) {
                    Object job = pool.dequeue();
                    if (job instanceof Runnable) {
                        setName (name + "-running");
                        synchronized (ThreadPool.this) {
                            currentJob = job;
                            active++;
                        }
                        try {
                            ((Runnable) job).run();
                            setName (name + "-idle");
                        } catch (Throwable t) {
                            setName (name + "-idle-"+t.getMessage());
                        }
                        synchronized (ThreadPool.this) {
                            currentJob = null;
                            available++;
                            active--;
                        }
                    } else {
                        synchronized (ThreadPool.this) {
                            currentJob = null;
                            available++;
                        }
                    }
                }
            } catch (InterruptedException e) {
                if (logger != null) {
                    Logger.log(new LogEvent(ThreadPool.this, e.getMessage()));
                }
            } catch (Closed e) {
                if (logger != null) {
                    Logger.log(new LogEvent(ThreadPool.this, e.getMessage()));
                }
            }
        }
        public synchronized void supervise () {
            if (currentJob != null && currentJob instanceof Supervised && ((Supervised)currentJob).expired())
                this.interrupt();
        }
    }

    /**
     * @param poolSize starting pool size
     * @param maxPoolSize maximum number of threads on this pool
     */
    public ThreadPool (int poolSize, int maxPoolSize) {
        this(poolSize, maxPoolSize, "ThreadPool");
    }
    /**
     * @param name pool name
     * @param poolSize starting pool size
     * @param maxPoolSize maximum number of threads on this pool
     */
    public ThreadPool (int poolSize, int maxPoolSize, String name) {
        super(name + "-" + poolNumber.getAndIncrement());
        this.maxPoolSize = maxPoolSize > 0 ? maxPoolSize : DEFAULT_MAX_THREADS ;
        this.available = this.maxPoolSize;
        this.namePrefix = name;
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
    public synchronized void execute(Runnable action) throws Closed {        
        if (!pool.ready())
            throw new Closed();

        if (++jobs % this.maxPoolSize == 0 || pool.consumerCount() <= 0)
            supervise();

        if (running < maxPoolSize && pool.consumerDeficit() >= 0) {
            new PooledThread().start();
            running++;
        }
        available--;
        pool.enqueue (action);
    }
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.println (indent + "<thread-pool name=\""+getName()+"\">");
        if (!pool.ready())
            p.println (inner  + "<closed/>");
        p.println (inner  + "<jobs>" + getJobCount() + "</jobs>");
        p.println (inner  + "<size>" + getPoolSize() + "</size>");
        p.println (inner  + "<max>"  + getMaxPoolSize() + "</max>");
        p.println (inner  + "<idle>"  + getIdleCount() + "</idle>");
        p.println (inner  + "<active>"  + getActiveCount() + "</active>");
        p.println (inner  + "<pending>" + getPendingCount() + "</pending>");
        p.println (indent + "</thread-pool>");
    }

    /**
     * @return number of jobs processed by this pool
     */
    public int getJobCount () {
        return jobs;
    }
    /**
     * @return number of running threads
     */
    public int getPoolSize () {
        return running;
    }
    /**
     * @return max number of active threads allowed
     */
    public int getMaxPoolSize () {
        return maxPoolSize;
    }
    /**
     * @return number of active threads
     */
    public int getActiveCount () {
        return active;
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
    public static ThreadPool getThreadPool(java.lang.String name) throws NotFoundException {
        return (ThreadPool)NameRegistrar.get("thread.pool." + name);
    }
}
