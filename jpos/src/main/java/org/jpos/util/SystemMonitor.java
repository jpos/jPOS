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

import java.io.PrintStream;

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
    private volatile boolean shutdown = false;

    /**
     * noargs constructor
     */
    public SystemMonitor () {
        super();
    }
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
            thread = new Thread(this,"SystemMonitor");
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
        while (!shutdown) {
            Logger.log (new LogEvent (this, "SystemMonitor", this));
            try {
                long expected = System.currentTimeMillis() + sleepTime;
                Thread.sleep (sleepTime);
                delay = (int) (System.currentTimeMillis() - expected);
            } catch (InterruptedException e) { }
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    public void dump (PrintStream p, String indent) {
        String newIndent = indent + "  ";
        Runtime r = Runtime.getRuntime();
        p.println (indent + "--- memory ---");
        p.println (newIndent+" freeMemory="+r.freeMemory());
        p.println (newIndent+"totalMemory="+r.totalMemory());
        p.println (newIndent+"inUseMemory="+(r.totalMemory()-r.freeMemory()));
        p.println ("");
        p.println (indent + "--- threads ---");
        p.println (newIndent+"      delay="+delay+" ms");
        p.println (newIndent+"    threads="+Thread.activeCount());
        showThreadGroup (Thread.currentThread().getThreadGroup(), p, newIndent);
        p.println ("");
        NameRegistrar.getInstance().dump (p, indent);
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


