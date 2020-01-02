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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TPS can be used to measure Transactions Per Second (or transactions during
 * other period of time).
 *
 * <p>It can operate in two different modes: <ul> <li>Auto update</li>
 * <li>Manual update</li> </ul></p>
 *
 * <p>When operating in <b>auto update</b> mode, a Timer is created and the
 * number of transactions (calls to tick()) is automatically calculated for
 * every period. Under this mode, user has to call the <b>stop()</b> method when
 * this TPS object is no longer needed, otherwise it will keep a Thread
 * lingering around.</p>
 *
 * <p>When operating in <b>manual update</b> mode, user has to call one of its
 * floatValue() or intValue() method at regular intervals. The returned value
 * will be the average TPS for the given period since the last call</p>.
 *
 * @author Alejandro Revilla, Jeronimo Paolleti and Thiago Moretto
 * @since 1.6.7 r2912
 */
@SuppressWarnings("unused")
public class TPS implements Loggeable {
    AtomicInteger count;
    AtomicLong start;
    AtomicLong readings;
    int peak;
    long peakWhen;
    static final long FROM_NANOS = 1000000L;
    long period;
    float tps;
    float avg;
    Timer timer;
    boolean autoupdate;
    final ReentrantLock lock = new ReentrantLock();
    protected long simulatedNanoTime = 0L;

    public TPS() {
        this(1000L, false);
    }

    /**
     *
     * @param autoupdate true to auto update
     */
    public TPS(boolean autoupdate) {
        this(1000L, autoupdate);
    }

    /**
     * @param period in millis
     * @param autoupdate true to autoupdate
     */
    public TPS(final long period, boolean autoupdate) {
        super();
        count = new AtomicInteger(0);
        start = new AtomicLong(0L);
        readings = new AtomicLong(0L);
        this.period = period;
        this.autoupdate = autoupdate;
        start.set(System.nanoTime() / FROM_NANOS);
        if (autoupdate) {
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        public void run() {
                            calcTPS(period);
                        }
                    }, period, period);
        }
    }

    public void tick() {
        count.incrementAndGet();
    }

    public float floatValue() {
        return autoupdate ? tps : calcTPS();
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
        return peakWhen;
    }

    /**
     * resets average and peak
     */
    public void reset() {
        lock.lock();
        try {
            avg = 0f;
            peak = 0;
            peakWhen = 0L;
            readings.set(0L);
        } finally {
            lock.unlock();
        }
    }

    public long getPeriod() {
        return period;
    }

    public long getElapsed() {
        return (System.nanoTime() / FROM_NANOS) - start.get();
    }

    public String toString() {
        return String.format("tps=%d, peak=%d, avg=%.2f", intValue(), getPeak(), getAvg());

    }

    public void stop() {
        lock.lock();
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
                autoupdate = false; // can still use it in manual mode
            }
        } finally {
            lock.unlock();
        }
    }

    public void dump(PrintStream p, String indent) {
        p.println(indent
                + "<tps"
                + (autoupdate ? " auto='true'>" : ">")
                + this.toString()
                + "</tps>");
    }

    private float calcTPS(long interval) {
        lock.lock();
        try {
            tps = (float) period * count.get() / interval;
            if (period != 1000L) {
                tps = tps*1000L/period;
            }
            long r = readings.getAndIncrement();
            avg = (r * avg + tps) / ++r;
            if (tps > peak) {
                peak = Math.round(tps);
                peakWhen = System.currentTimeMillis();
            }
            count.set(0);
            return tps;
        } finally {
            lock.unlock();
        }
    }

    private float calcTPS() {
        lock.lock();
        try {
            long now = getNanoTime() / FROM_NANOS;
            long interval = now - start.get();
            if (interval >= period) {
                calcTPS(interval);
                start.set(now);
            }
            return tps;
        } finally {
            lock.unlock();
        }
    }

    public void setSimulatedNanoTime(long simulatedNanoTime) {
        if (this.simulatedNanoTime == 0L)
            start.set(simulatedNanoTime / FROM_NANOS);

        this.simulatedNanoTime = simulatedNanoTime;
    }

    protected long getNanoTime() {
        return simulatedNanoTime > 0L ? simulatedNanoTime : System.nanoTime();
    }
}
