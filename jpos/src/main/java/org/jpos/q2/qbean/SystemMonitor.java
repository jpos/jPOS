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

package org.jpos.q2.qbean;

import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.io.PrintStream;

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
        Runtime r = getRuntimeInstance();
        p.printf ("%s<revision>%s</revision>%n", indent, getRevision());
        p.printf ("%s<instance>%s</instance>%n", indent, getInstanceIdAsString());
        p.printf ("%s<uptime>%s</uptime>%n", indent, ISOUtil.millisToString(getServerUptimeAsMillisecond()));
        p.println(indent + "<memory>");
        p.println(newIndent + " freeMemory=" + r.freeMemory());
        p.println(newIndent + "totalMemory=" + r.totalMemory());
        p.println(newIndent + "inUseMemory="
                + (r.totalMemory() - r.freeMemory()));
        p.println(indent + "</memory>");
        if (hasSecurityManager())
            p.println (indent +"sec.manager=" + getSecurityManager());
        p.println(indent + "<threads>");
        p.println(newIndent + "      delay=" + delay + " ms");
        p.println(newIndent + "    threads=" + Thread.activeCount());
        showThreadGroup(Thread.currentThread().getThreadGroup(), p, newIndent);
        p.println(indent + "</threads>");
        NameRegistrar.getInstance().dump(p, indent, detailRequired);
    }

    SecurityManager getSecurityManager() {
	return System.getSecurityManager();
    }

    boolean hasSecurityManager() {
	return getSecurityManager() != null;
    }

    Runtime getRuntimeInstance() {
	return Runtime.getRuntime();
    }

    long getServerUptimeAsMillisecond() {
	return getServer().getUptime();
    }

    String getInstanceIdAsString() {
	return getServer().getInstanceId().toString();
    }

    String getRevision() {
	return getServer().getRevision();
    }

}
