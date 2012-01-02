/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
 * @jmx:mbean description="DirPoll adaptor QBean"
 *                  extends="org.jpos.q2.QBeanSupportMBean"
 */
public class DirPollAdaptor 
    extends QBeanSupport
    implements DirPollAdaptorMBean
{
    String path, priorities, processorClass;
    int poolSize;
    long pollInterval;
    protected DirPoll dirPoll;
    public DirPollAdaptor () {
        super ();
        poolSize = 1;
        pollInterval = 1000;
    }

    protected void initService () throws Exception {
        QFactory factory = getServer().getFactory();
        dirPoll  = new DirPoll ();
        dirPoll.setPath (getPath ());
        dirPoll.setThreadPool (new ThreadPool (1, poolSize));
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
    protected void startService () throws Exception {
        new Thread (dirPoll).start ();
    }

    protected void stopService () throws Exception {
        dirPoll.destroy ();
    }


    /**
     * @jmx:managed-attribute description="Base path"
     */
    public synchronized void setPath (String path) {
        this.path = path;
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="thread pool size"
     */
    public synchronized void setPoolSize (int size) {
        this.poolSize = size;
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="thread pool size"
     */
    public int getPoolSize () {
        return poolSize;
    }

    /**
     * @jmx:managed-attribute description="Base path"
     */
    public String getPath () {
        return path == null ? "." : path;
    }

    /**
     * @jmx:managed-attribute description="poll time in millis"
     */
    public synchronized void setPollInterval (long pollInterval) {
        this.pollInterval = pollInterval;
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="poll time in millis"
     */
    public long getPollInterval () {
        return pollInterval;
    }
    /**
     * @jmx:managed-attribute description="priorities"
     */
    public synchronized void setPriorities (String priorities) {
        this.priorities = priorities;
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="priorities"
     */
    public String getPriorities () {
        return priorities;
    }

    /**
     * @jmx:managed-attribute description="processor class"
     */
    public synchronized void setProcessor (String processor) {
        this.processorClass = processor;
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="processor class"
     */
    public String getProcessor() {
        return processorClass;
    }
}

   
