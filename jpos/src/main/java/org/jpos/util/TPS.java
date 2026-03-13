/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.util.concurrent.Executors;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TPS can be used to measure Transactions Per Second (or transactions during
 * other period of time).
 *
 * <p>It can operate in two different modes:
 * <ul>
 *   <li>Auto update.</li>
 *   <li>Manual update.</li>
 * </ul>
 *
 * <p>When operating in <b>auto update</b> mode, a shared scheduler is used and the
 * number of transactions (calls to tick()) is automatically calculated for
 * every period. In this mode, callers should invoke <b>stop()</b> (or
 * <b>close()</b>) when the TPS object is no longer needed to cancel its scheduled
 * task. The shared scheduler thread is daemon and reused across instances.</p>
 *
 * <p>When operating in <b>manual update</b> mode, user has to call one of its
 * floatValue() or intValue() method at regular intervals. The returned value
 * will be the average TPS for the given period since the last call.</p>
 *
 * @author Alejandro Revilla, Jeronimo Paolleti and Thiago Moretto
 * @since 1.6.7 r2912
 */
@SuppressWarnings("unused")
public class TPS implements Loggeable, AutoCloseable {
    /**
     * If set (via setSimulatedNanoTime), getNanoTime() returns this value.
     * This is intended for deterministic tests.
     */
    protected volatile long simulatedNanoTime = 0L;

    static final long FROM_NANOS = 1_000_000L; // nanos -> millis conversion factor (kept for compatibility)

    private final AtomicLong count = new AtomicLong(0L);

    private final Duration period;

    private volatile boolean autoupdate;

    // Published metrics: written under lock, read lock-free.
    private volatile float tps;
    private volatile float avg;
    private volatile int peak;
    private volatile Instant peakWhen;

    // State for manual/auto interval measurement (monotonic).
    private volatile long lastSampleNanos;

    // State for getElapsed() (wall clock), updated when sampling.
    private volatile Instant startWall;

    // Average computation state (guarded by lock).
    private long readings;

    // Auto-update scheduler (shared across TPS instances).
    private static final ScheduledExecutorService SHARED_SCHEDULER = Executors.newSingleThreadScheduledExecutor(
      new ThreadFactory() {
          @Override
          public Thread newThread(Runnable r) {
              Thread t = new Thread(r, "TPS-auto-update");
              t.setDaemon(true);
              return t;
          }
      }
    );
    private ScheduledFuture<?> scheduledTask;

    // Pluggable time source (monotonic nanos).
    private final LongSupplier nanoTimeSource;

    public TPS() {
        this(1000L, false);
    }

    /**
     * @param autoupdate true to auto update.
     */
    public TPS(boolean autoupdate) {
        this(1000L, autoupdate);
    }

    /**
     * @param period in millis.
     * @param autoupdate true to autoupdate.
     */
    public TPS(final long period, boolean autoupdate) {
        this(period, autoupdate, null);
    }

    /**
     * Internal constructor that allows injecting a nano time source for tests.
     * If nanoTimeSource is null, System.nanoTime() is used (or simulatedNanoTime when set).
     */
    TPS(final long periodMillis, boolean autoupdate, LongSupplier nanoTimeSource) {
        if (periodMillis <= 0L)
            throw new IllegalArgumentException("period must be > 0 ms");

        this.period = Duration.ofMillis(periodMillis);
        this.autoupdate = autoupdate;

        this.nanoTimeSource = nanoTimeSource != null ? nanoTimeSource : this::getNanoTime;

        Instant nowWall = Instant.now();
        this.startWall = nowWall;
        this.peakWhen = nowWall;

        long nowNanos = this.nanoTimeSource.getAsLong();
        this.lastSampleNanos = nowNanos;

        if (autoupdate) {
            startScheduler(periodMillis);
        }
    }

    public void tick() {
        count.incrementAndGet();
    }

    public float floatValue() {
        if (autoupdate) {
            return tps;
        } else {
            return calcTPSIfDue();
        }
    }

    public int intValue() {
        return Math.round(floatValue());
    }

    public float getAvg() {
        return avg;
    }

    public int getPeak() {
        return peak;
    }

    public long getPeakWhen() {
        Instant pw = peakWhen;
        return pw != null ? pw.toEpochMilli() : -1L;
    }

    /**
     * resets average and peak.
     */
    public void reset() {
        synchronized (this) {
            avg = 0f;
            peak = 0;
            peakWhen = null;
            readings = 0L;
        }
    }

    public long getPeriod() {
        return period.toMillis();
    }

    public long getElapsed() {
        // Wall-clock elapsed since last sampling start (manual) or since construction (auto).
        Instant sw = startWall;
        return sw != null ? Duration.between(sw, Instant.now()).toMillis() : 0L;
    }

    public String toString() {
        return String.format("tps=%d, peak=%d, avg=%.2f", intValue(), getPeak(), getAvg());
    }

    public void stop() {
        synchronized (this) {
            autoupdate = false; // can still use it in manual mode
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
                scheduledTask = null;
            }
        }
    }

    @Override
    public void close() {
        stop();
    }

    public void dump(PrintStream p, String indent) {
        p.println(indent
          + "<tps"
          + (autoupdate ? " auto='true'>" : ">")
          + this
          + "</tps>");
    }

    private void startScheduler(long periodMillis) {
        final WeakReference<TPS> ref = new WeakReference<>(this);
        final AtomicReference<ScheduledFuture<?>> self = new AtomicReference<>();

        // scheduleAtFixedRate keeps cadence; we still compute based on actual elapsed nanos
        // to be robust against pauses or scheduling delays.
        ScheduledFuture<?> future = SHARED_SCHEDULER.scheduleAtFixedRate(() -> {
            TPS t = ref.get();
            if (t == null) {
                ScheduledFuture<?> f = self.get();
                if (f != null) {
                    f.cancel(false);
                }
                return;
            }
            try {
                t.calcTPSSampled();
            } catch (Throwable ignored) {
                // Avoid terminating the scheduler thread due to an unchecked exception.
                // Intentionally swallow: TPS is best-effort telemetry.
            }
        }, periodMillis, periodMillis, TimeUnit.MILLISECONDS);

        self.set(future);
        scheduledTask = future;
    }

    /**
     * Manual mode: compute and publish TPS only when at least one period has elapsed.
     */
    private float calcTPSIfDue() {
        long nowNanos = nanoTimeSource.getAsLong();
        long elapsedNanos = nowNanos - lastSampleNanos;

        if (elapsedNanos >= period.toNanos()) {
            return calcTPS(elapsedNanos, nowNanos);
        }
        return tps;
    }

    /**
     * Auto mode: compute and publish TPS every scheduler tick, based on actual elapsed nanos.
     */
    private void calcTPSSampled() {
        if (!autoupdate)
            return;

        long nowNanos = nanoTimeSource.getAsLong();
        long elapsedNanos = nowNanos - lastSampleNanos;

        // If for any reason elapsed is non-positive (e.g., simulated time misuse), skip safely.
        if (elapsedNanos <= 0L)
            return;

        calcTPS(elapsedNanos, nowNanos);
    }

    /**
     * Computes TPS as transactions per second (count / elapsedSeconds) using monotonic time.
     * Publishes tps/avg/peak/peakWhen atomically under lock; count is atomically captured.
     */
    private float calcTPS(long elapsedNanos, long nowNanos) {
        final long c = count.getAndSet(0L); // atomic capture-and-reset; avoids lost ticks
        final float newTps = (c <= 0L) ? 0f : (c * 1_000_000_000f) / (float) elapsedNanos;

        synchronized (this) {
            tps = newTps;

            // Online average of sampled TPS values.
            long r = readings++;
            avg = (r * avg + newTps) / (r + 1L);

            int rounded = Math.round(newTps);
            if (rounded > peak) {
                peak = rounded;
                peakWhen = Instant.now();
            }

            lastSampleNanos = nowNanos;
            startWall = Instant.now();
            return tps;
        }
    }

    public void setSimulatedNanoTime(long simulatedNanoTime) {
        // This is a monotonic nano time value intended for tests.
        // We do not attempt to convert it to epoch-based Instants.
        if (this.simulatedNanoTime == 0L) {
            long now = simulatedNanoTime;
            if (now > 0L) {
                lastSampleNanos = now;
            }
        }
        this.simulatedNanoTime = simulatedNanoTime;
    }

    protected long getNanoTime() {
        long s = simulatedNanoTime;
        return s > 0L ? s : System.nanoTime();
    }
}
