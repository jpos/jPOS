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
package org.jpos.q2.iso;

import java.util.Set;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.management.ObjectName;
import org.jpos.util.LogSource;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.q2.Q2;
import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.DirPoll;
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
    DirPoll dirPoll;
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
            dirPoll.setProcessor (dpp);
        }
        if (dpp instanceof Configurable) {
            ((Configurable) dpp).setConfiguration (cfg);
        }
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

   
