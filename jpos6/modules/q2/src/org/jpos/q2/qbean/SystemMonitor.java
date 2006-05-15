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

package org.jpos.q2.qbean;

import java.io.PrintStream;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

/**
 * Periodically dumps Thread and memory usage
 * 
 * @author apr@cs.com.uy
 * @version $Id$
 * @jmx:mbean description="System Monitor"
 *            extends="org.jpos.q2.QBeanSupportMBean"
 * @see Logger
 */
public class SystemMonitor extends QBeanSupport implements Runnable,
        SystemMonitorMBean, Loggeable {

    private long sleepTime = 60 * 60 * 1000;

    private long delay = 0;

    private boolean detailRequired = false;

    private Thread me = null;

    public void startService() {
        try {
            log.info("Starting SystemMonitor");
            me = new Thread(this,"SystemMonitor");
            me.start();
        } catch (Exception e) {
            log.warn("error starting service", e);
        }
    }

    public void stopService() {
        log.info("Stopping SystemMonitor");
        if (me != null)
            me.interrupt();
    }

    /**
     * @jmx:managed-attribute description="Milliseconds between dump"
     */
    public synchronized void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
        setModified(true);
        if (me != null)
            me.interrupt();
    }

    /**
     * @jmx:managed-attribute description="Milliseconds between dump"
     */
    public synchronized long getSleepTime() {
        return sleepTime;
    }

    /**
     * @jmx:managed-attribute description="Detail required?"
     */
    public synchronized void setDetailRequired(boolean detail) {
        this.detailRequired = detail;
        setModified(true);
        if (me != null)
            me.interrupt();
    }

    /**
     * @jmx:managed-attribute description="Detail required?"
     */
    public synchronized boolean getDetailRequired() {
        return detailRequired;
    }

    void dumpThreads(ThreadGroup g, PrintStream p, String indent) {
        Thread[] list = new Thread[g.activeCount() + 5];
        int nthreads = g.enumerate(list);
        for (int i = 0; i < nthreads; i++)
            p.println(indent + list[i]);
    }

    public void showThreadGroup(ThreadGroup g, PrintStream p, String indent) {
        if (g.getParent() != null)
            showThreadGroup(g.getParent(), p, indent + "  ");
        else
            dumpThreads(g, p, indent + "    ");
    }

    public void run() {
        while (running()) {
            log.info(this);
            try {
                long expected = System.currentTimeMillis() + sleepTime;
                Thread.sleep(sleepTime);
                delay = (System.currentTimeMillis() - expected);
            } catch (InterruptedException e) {
            }
        }
    }

    public void dump(PrintStream p, String indent) {
        String newIndent = indent + "  ";
        Runtime r = Runtime.getRuntime();
        p.println(indent + "<memory>");
        p.println(newIndent + " freeMemory=" + r.freeMemory());
        p.println(newIndent + "totalMemory=" + r.totalMemory());
        p.println(newIndent + "inUseMemory="
                + (r.totalMemory() - r.freeMemory()));
        p.println(indent + "</memory>");
        p.println(indent + "<threads>");
        p.println(newIndent + "      delay=" + delay + " ms");
        p.println(newIndent + "    threads=" + Thread.activeCount());
        showThreadGroup(Thread.currentThread().getThreadGroup(), p, newIndent);
        p.println(indent + "</threads>");
        NameRegistrar.getInstance().dump(p, indent, detailRequired);
    }

}
