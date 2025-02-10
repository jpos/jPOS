/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

package org.jpos.q2.iso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.util.DirPoll;
import org.jpos.util.LogSource;
import org.jpos.util.ThreadPool;

/**
 * DirPoll Adaptor
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class DirPollAdaptor
    extends QBeanSupport
    implements DirPollAdaptorMBean
{
    String path, priorities, processorClass;
    int poolSize;
    long pollInterval;
    protected DirPoll dirPoll;
    protected ExecutorService dirPollExecutor;

    public DirPollAdaptor () {
        super ();
        poolSize = 1;
        pollInterval = 1000;
    }

    protected void initService () throws Exception {
        QFactory factory = getServer().getFactory();
        dirPoll  = createDirPoll();
        dirPoll.setPath (getPath ());
        if(cfg.getBoolean("virtual-threads", true)) {
            dirPoll.setThreadPool(Executors.newFixedThreadPool(poolSize, Thread.ofVirtual().factory()));
        }else {
            dirPoll.setThreadPool(Executors.newFixedThreadPool(poolSize, Thread.ofPlatform().inheritInheritableThreadLocals(true).factory()));
        }
        dirPoll.setPollInterval (pollInterval);
        if (priorities != null)
            dirPoll.setPriorities (priorities);
        dirPoll.setLogger (getLog().getLogger(), getLog().getRealm ());
        Configuration cfg = factory.getConfiguration (getPersist());
        dirPoll.setConfiguration (cfg);
        dirPoll.createDirs ();
        Object dpp = factory.newInstance (getProcessor());
        if (dpp instanceof LogSource) {
            ((LogSource) dpp).setLogger (
                getLog().getLogger(), getLog().getRealm ()
            );
        }
        if (dpp instanceof Configurable) {
            ((Configurable) dpp).setConfiguration (cfg);
        }
        dirPoll.setProcessor (dpp);
    }

    protected DirPoll createDirPoll() {
        return new DirPoll();
    }

    protected void startService () throws Exception {
        if (dirPoll == null) {
            throw new IllegalStateException("Not initialized!");
        }
        synchronized (dirPoll) {
            dirPollExecutor =  Executors.newVirtualThreadPerTaskExecutor();
            dirPollExecutor.submit(dirPoll);
        }
    }

    protected void stopService () throws Exception {
        dirPoll.destroy ();
        synchronized (dirPoll) {
            long shutdownTimeout = cfg.getLong("shutdown-timeout", 60000);
            dirPollExecutor.shutdown();
            try {
                if (!dirPollExecutor.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS)) {
                    dirPollExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    public synchronized void setPath (String path) {
        this.path = path;
        setModified (true);
    }

    public synchronized void setPoolSize (int size) {
        this.poolSize = size;
        setModified (true);
    }

    public int getPoolSize () {
        return poolSize;
    }

    public String getPath () {
        return path == null ? "." : path;
    }

    public synchronized void setPollInterval (long pollInterval) {
        this.pollInterval = pollInterval;
        setModified (true);
    }

    public long getPollInterval () {
        return pollInterval;
    }

    public synchronized void setPriorities (String priorities) {
        this.priorities = priorities;
        setModified (true);
    }

    public String getPriorities () {
        return priorities;
    }

    public synchronized void setProcessor (String processor) {
        this.processorClass = processor;
        setModified (true);
    }

    public String getProcessor() {
        return processorClass;
    }
}
