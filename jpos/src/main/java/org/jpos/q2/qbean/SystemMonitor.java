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

package org.jpos.q2.qbean;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Environment;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.*;

import javax.crypto.Cipher;
import javax.management.MBeanServerConnection;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneOffsetTransitionRule;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Periodically dumps Thread and memory usage
 * 
 * @author apr@cs.com.uy
 * @version $Id$
 * @see Logger
 */
public class SystemMonitor extends QBeanSupport
        implements Runnable, SystemMonitorMBean, Loggeable
{
    private long sleepTime = 60 * 60 * 1000;
    private long delay = 0;
    private boolean detailRequired = false;
    private Thread me = null;
    private static final int MB = 1024*1024;
    private String[] scripts;
    private String frozenDump;
    private String localHost;
    private String processName;

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

    public synchronized void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
        setModified(true);
        if (me != null)
            me.interrupt();
    }

    public synchronized long getSleepTime() {
        return sleepTime;
    }

    public synchronized void setDetailRequired(boolean detail) {
        this.detailRequired = detail;
        setModified(true);
        if (me != null)
            me.interrupt();
    }

    public synchronized boolean getDetailRequired() {
        return detailRequired;
    }

    private void dumpThreads(ThreadGroup g, PrintStream p, String indent) {
        Thread[] list = new Thread[g.activeCount() + 5];
        int nthreads = g.enumerate(list);
        for (int i = 0; i < nthreads; i++)
            p.println(indent + list[i]);
    }

    void showThreadGroup(ThreadGroup g, PrintStream p, String indent) {
        if (g.getParent() != null)
            showThreadGroup(g.getParent(), p, indent + "  ");
        else
            dumpThreads(g, p, indent + "    ");
    }

    public void run() {
        localHost = getLocalHost();
        processName = ManagementFactory.getRuntimeMXBean().getName();
        while (running()) {
            frozenDump = generateFrozenDump ("");
            log.info(this);
            frozenDump = null;
            try {
                long expected = System.currentTimeMillis() + sleepTime;
                Thread.sleep(sleepTime);
                delay = System.currentTimeMillis() - expected;
            } catch (InterruptedException ignored) {
            }
        }
        frozenDump = generateFrozenDump ("");
        log.info(this);
        frozenDump = null;
    }
    public void dump (PrintStream p, String indent) {
        if (frozenDump == null)
            frozenDump = generateFrozenDump(indent);
        p.print(frozenDump);
        dumpMetrics();
    }
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        scripts = cfg.getAll("script");
    }

    private SecurityManager getSecurityManager() {
        return System.getSecurityManager();
    }

    private boolean hasSecurityManager() {
        return getSecurityManager() != null;
    }

    private Runtime getRuntimeInstance() {
	    return Runtime.getRuntime();
    }

    private long getServerUptimeAsMillisecond() {
        return getServer().getUptime();
    }

    private String getInstanceIdAsString() {
        return getServer().getInstanceId().toString();
    }

    private String getRevision() {
        return Q2.getRevision();
    }
    private String getLocalHost () {
        try {
            return InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private String generateFrozenDump(String indent) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(baos);
        String newIndent = indent + "  ";
        Runtime r = getRuntimeInstance();
        ZoneId zi = ZoneId.systemDefault();
        Instant instant = Instant.now();

        File cwd = new File(".");
        String freeSpace = ISOUtil.readableFileSize(cwd.getFreeSpace());
        String usableSpace = ISOUtil.readableFileSize(cwd.getUsableSpace());
        p.printf ("%s           OS: %s (%s)%n", indent, System.getProperty("os.name"), System.getProperty("os.version"));
        int maxKeyLength = 0;
        try {
            maxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
        } catch (NoSuchAlgorithmException ignored) { }
        p.printf("%s         Java: %s (%s) AES-%s%n", indent,
          System.getProperty("java.version"),
          System.getProperty("java.vendor"),
          maxKeyLength == Integer.MAX_VALUE ? "secure" : Integer.toString(maxKeyLength)
        );
        p.printf ("%s  environment: %s%n", indent, Environment.getEnvironment().getName());
        p.printf ("%s process name: %s%n", indent, processName);
        p.printf ("%s    user name: %s%n", indent, System.getProperty("user.name"));
        p.printf ("%s         host: %s%n", indent, localHost);
        p.printf ("%s          cwd: %s%n", indent, System.getProperty("user.dir"));
        p.printf ("%s   free space: %s%n", indent, freeSpace);

        if (!freeSpace.equals(usableSpace))
            p.printf ("%s usable space: %s%n", indent, usableSpace);
        p.printf ("%s      version: %s (%s)%n", indent, Q2.getVersion(), getRevision());
        p.printf ("%s     instance: %s%n", indent, getInstanceIdAsString());
        p.printf ("%s       uptime: %s (%f)%n", indent, ISOUtil.millisToString(getServerUptimeAsMillisecond()), loadAverage());
        p.printf ("%s   processors: %d%n", indent, r.availableProcessors());
        p.printf ("%s       drift : %d%n", indent, delay);
        p.printf ("%smemory(t/u/f): %d/%d/%d%n", indent,
                r.totalMemory()/MB, (r.totalMemory() - r.freeMemory())/MB, r.freeMemory()/MB);
        p.printf("%s     encoding: %s%n", indent, Charset.defaultCharset());
        p.printf("%s     timezone: %s (%s) %s%n", indent, zi,
                zi.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                zi.getRules().getOffset(instant).toString());
        p.printf("%swatch service: %s%n", indent, getServer().getWatchServiceClassname());
        List<ZoneOffsetTransitionRule> l = zi.getRules().getTransitionRules();
        for (ZoneOffsetTransitionRule tr : l) {
            p.printf("%s         rule: %s%n", indent, tr.toString());
        }
        ZoneOffsetTransition tran = zi.getRules().nextTransition(instant);
        if (tran != null) {
            Instant in = tran.getInstant();
            p.printf("%s   transition: %s (%s)%n", indent, in, in.atZone(zi));
        }
        p.printf("%s        clock: %d %s%n", indent, System.currentTimeMillis() / 1000L, instant);
        if (hasSecurityManager())
            p.printf("%s  sec-manager: %s%n", indent, getSecurityManager());
        p.printf("%s thread count: %d%n", indent, mxBean.getThreadCount());
        p.printf("%s peak threads: %d%n", indent, mxBean.getPeakThreadCount());
        p.printf("%s user threads: %d%n", indent, Thread.activeCount());

        showThreadGroup(Thread.currentThread().getThreadGroup(), p, newIndent);
        NameRegistrar.getInstance().dump(p, indent, detailRequired);
        for (String s : scripts) {
            p.printf("%s%s:%n", indent, s);
            exec(s, p, newIndent);
        }
        return baos.toString();
    }
    private void exec (String script, PrintStream ps, String indent) {
        try {
            Process p = Runtime.getRuntime().exec(script);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()) );
            String line;
            while ((line = in.readLine()) != null) {
                ps.printf("%s%s%n", indent, line);
            }
        } catch (Exception e) {
            e.printStackTrace(ps);
        }
    }

    private double loadAverage () {
        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        try {
            OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
              mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            return osMBean.getSystemLoadAverage();
        } catch (Throwable ignored) { }
        return -1;
    }

    private void dumpMetrics() {
        if (cfg.get("metrics-dir", null) != null) {
            File dir = new File(cfg.get("metrics-dir"));
            dir.mkdir();
            NameRegistrar.getAsMap().forEach((key, value) -> {
                if (value instanceof MetricsProvider) {
                    Metrics metrics = ((MetricsProvider) value).getMetrics();
                    if (metrics != null)
                        metrics.dumpHistograms(dir, key + "-");
                }
            });
        }
    }
}
