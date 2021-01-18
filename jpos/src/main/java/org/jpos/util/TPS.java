/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
    Instant start;
    AtomicLong readings;
    int peak;
    Instant peakWhen;
    static final long FROM_NANOS = 1000000L;
    Duration period;
    float tps;
    float avg;
    Timer timer;
    boolean autoupdate;
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
        start = peakWhen = Instant.now();
        readings = new AtomicLong(0L);
        this.period = Duration.ofMillis(period);
        this.autoupdate = autoupdate;
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
        return peakWhen.toEpochMilli();
    }

    /**
     * resets average and peak
     */
    public void reset() {
        synchronized(this) {
            avg = 0f;
            peak = 0;
            peakWhen = Instant.EPOCH;
            readings.set(0L);
        }
    }

    public long getPeriod() {
        return period.toMillis();
    }

    public long getElapsed() {
        return Duration.between(start, Instant.now()).toMillis();
    }

    public String toString() {
        return String.format("tps=%d, peak=%d, avg=%.2f", intValue(), getPeak(), getAvg());

    }

    public void stop() {
        synchronized(this) {
            if (timer != null) {
                timer.cancel();
                timer = null;
                autoupdate = false; // can still use it in manual mode
            }
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
        return calcTPS(Duration.ofMillis(interval));
    }

    private float calcTPS(Duration interval) {
        synchronized(this) {
            tps = (float) period.toNanos() * count.get() / interval.toNanos();
            if (period.toNanos() != 1000000000L) {
                tps = tps/period.toNanos();
            }
            long r = readings.getAndIncrement();
            avg = (r * avg + tps) / ++r;
            if (tps > peak) {
                peak = Math.round(tps);
                peakWhen = Instant.now();
            }
            count.set(0);
            return tps;
        }
    }

    private float calcTPS() {
        synchronized(this) {
            Instant now = Instant.now();
            Duration interval = Duration.between(start, now);
            if (interval.compareTo(period) >= 0) {
                calcTPS(interval);
                start = now;
            }
            return tps;
        }
    }

    public void setSimulatedNanoTime(long simulatedNanoTime) {
        if (this.simulatedNanoTime == 0L)
            start = Instant.ofEpochMilli(simulatedNanoTime / FROM_NANOS);

        this.simulatedNanoTime = simulatedNanoTime;
    }

    protected long getNanoTime() {
        return simulatedNanoTime > 0L ? simulatedNanoTime : System.nanoTime();
    }
}
