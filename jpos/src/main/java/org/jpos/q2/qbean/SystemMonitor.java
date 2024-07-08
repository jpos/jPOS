/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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
import org.jpos.core.annotation.Config;
import org.jpos.iso.ISOUtil;
import org.jpos.log.AuditLogEvent;
import org.jpos.log.evt.KV;
import org.jpos.log.evt.ProcessOutput;
import org.jpos.log.evt.SysInfo;
import org.jpos.q2.Q2;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.*;

import javax.crypto.Cipher;
import javax.management.MBeanServerConnection;
import java.io.*;
import java.lang.management.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Periodically dumps Thread and memory usage
 * 
 * @author apr@cs.com.uy
 * @version $Id$
 * @see Logger
 */
public class SystemMonitor extends QBeanSupport
        implements Runnable, SystemMonitorMBean
{
    private long sleepTime = 60 * 60 * 1000;
    private long drift = 0;
    private boolean detailRequired = false;
    private Thread me = null;
    private static final int MB = 1024*1024;
    private String[] scripts;
    private String frozenDump;
    private String localHost;
    private String processName;
    private Lock dumping = new ReentrantLock();
    @Config("metrics-dir")
    private String metricsDir;

    @Config("dump-stacktrace")
    boolean dumpStackTrace;

    int stackTraceDepth;
    @Override
    public void initService() { }

    @Override
    public void startService() {
        try {
            me = Thread.ofVirtual().name("SystemMonitor").start(this);
        } catch (Exception e) {
            log.warn("error starting service", e);
        }
    }

    public void stopService() {
        interruptMainThread();
    }
    public void destroyService() throws InterruptedException {
        me.join(Duration.ofMillis(5000L));
    }

    public synchronized void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
        setModified(true);
        interruptMainThread();
    }

    public synchronized long getSleepTime() {
        return sleepTime;
    }

    public synchronized void setDetailRequired(boolean detail) {
        this.detailRequired = detail;
        setModified(true);
        interruptMainThread();
    }

    public synchronized boolean getDetailRequired() {
        return detailRequired;
    }

    private List<KV> generateThreadInfo () {
        List<KV> threads = new ArrayList<>();
        Thread.getAllStackTraces().entrySet().stream()
          .sorted(Comparator.comparingLong(e -> e.getKey().threadId()))
          .forEach((e -> {
            Thread t = e.getKey();
            StackTraceElement[] stackTrace = e.getValue();
            String currentMethodInfo = stackTrace.length > 0 ?
              "%s.%s(%s:%d)".formatted(stackTrace[0].getClassName(), stackTrace[0].getMethodName(),
              stackTrace[0].getFileName(), stackTrace[0].getLineNumber()) :
              "";
              threads.add(new KV(
                "%s:%d".formatted(t.getThreadGroup(), t.threadId()),
                "%s - %s".formatted(t.getName(), currentMethodInfo)
              ));
          }));
        return threads;
    }

    public void run() {
        localHost = getLocalHost();
        processName = ManagementFactory.getRuntimeMXBean().getName();
        while (running()) {
            dumpSystemInfo();
            try {
                long expected = System.currentTimeMillis() + sleepTime;
                Thread.sleep(sleepTime);
                drift = System.currentTimeMillis() - expected;
            } catch (InterruptedException ignored) { }
        }
        dumpSystemInfo();
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        scripts = cfg.getAll("script");
        stackTraceDepth = cfg.getInt("stacktrace-depth", Integer.MAX_VALUE);
    }
    
    private Runtime getRuntimeInstance() {
	    return Runtime.getRuntime();
    }

    private long getServerUptimeAsMillisecond() {
        return getServer().getUptime().toMillis();
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
    private void exec (String script, PrintStream ps, String indent) {
        try {
            ProcessBuilder pb = new ProcessBuilder (QExec.parseCommandLine(script));
            Process p = pb.start();
            BufferedReader in = p.inputReader();
            String line;
            while ((line = in.readLine()) != null) {
                ps.printf("%s%s%n", indent, line);
            }
            p.waitFor();
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
        if (metricsDir != null) {
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

    private void interruptMainThread() {
        if (me != null) {
            dumping.lock();
            try {
                me.interrupt();
            } finally {
                dumping.unlock();
            }
        }
    }

    private LogEvent generateSystemInfo () {
        LogEvent evt = new LogEvent().withTraceId(getServer().getInstanceId());
        List<AuditLogEvent> events = new ArrayList<>();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        Runtime r = getRuntimeInstance();
        ZoneId zi = ZoneId.systemDefault();
        Instant instant = Instant.now();
        File cwd = new File(".");
        String freeSpace = ISOUtil.readableFileSize(cwd.getFreeSpace());
        String usableSpace = ISOUtil.readableFileSize(cwd.getUsableSpace());
        int maxKeyLength = 0;
        try {
            maxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
        } catch (NoSuchAlgorithmException ignored) { }

        long totalMemory = r.totalMemory();
        long freeMemory = r.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = r.maxMemory();
        long gcTotalCnt = 0;
        long gcTotalTime = 0;
        for(GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcTotalCnt += Math.max(gc.getCollectionCount(), 0L);
            gcTotalTime += Math.max(gc.getCollectionTime(), 0L);
        }
        evt.addMessage (new SysInfo(
          System.getProperty("os.name"),
          System.getProperty("os.version"),
          System.getProperty("java.version"),
          System.getProperty("java.vendor"),
          maxKeyLength == Integer.MAX_VALUE ? "secure" : Integer.toString(maxKeyLength),
          localHost,
          System.getProperty("user.name"),
          System.getProperty("user.dir"),
          getServer().getWatchServiceClassname(),
          Environment.getEnvironment().getName(),
          String.join(",", runtimeMXBean.getInputArguments()),
          Charset.defaultCharset(),
          String.format("%s (%s) %s %s%s",
            zi, zi.getDisplayName(TextStyle.FULL, Locale.getDefault()),
            zi.getRules().getOffset(instant),
            zi.getRules().getTransitionRules().toString(),
            Optional.ofNullable(zi.getRules().nextTransition(instant))
              .map(transition -> " " + transition)
              .orElse("")
          ),
          processName,
          freeSpace,
          usableSpace,
          Q2.getVersion(),
          Q2.getRevision(),
          getServer().getInstanceId(),
          getServer().getUptime(),
          loadAverage(),
          r.availableProcessors(),
          drift,
          maxMemory/MB,
          totalMemory/MB,
          freeMemory/MB,
          usedMemory/MB,
          gcTotalCnt,
          gcTotalTime,
          mxBean.getThreadCount(),
          mxBean.getPeakThreadCount(),
          NameRegistrar.getAsMap()
            .entrySet()
            .stream()
            .map(entry -> new KV(entry.getKey(), entry.getValue().toString()))
            .collect(Collectors.toList()),
          generateThreadInfo(),
          runScripts()
        ));
        return evt;

    }

    private List<ProcessOutput> runScripts() {
        List<ProcessOutput> l = new ArrayList<>();
        for (String s : scripts) {
            l.add(exec(s));
        }
        return l;
    }

    private ProcessOutput exec(String script) {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(QExec.parseCommandLine(script));
            Process p = pb.start();
            // Capture standard output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                stdout.append(reader.lines().collect(Collectors.joining(System.lineSeparator())));
            }
            // Capture error output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                String errorOutput = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                if (!errorOutput.isEmpty()) {
                    stderr.append(reader.lines().collect(Collectors.joining(System.lineSeparator())));
                }
            }
            p.waitFor();
        } catch (Exception e) {
            stderr.append("Exception: ").append(e.getMessage());
            e.printStackTrace(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    stdout.append((char) b);
                }
            }));
        }
        return new ProcessOutput (script, stdout.toString(), stderr.isEmpty() ? null : stderr.toString());
    }

    private void dumpSystemInfo () {
        dumping.lock();
        try {
            Logger.log(generateSystemInfo());
        } finally {
            dumping.unlock();
        }
    }
}
