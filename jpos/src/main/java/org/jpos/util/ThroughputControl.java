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

package org.jpos.util;

import org.jpos.iso.ISOUtil;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ThroughputControl limits the throughput 
 * of a process to a maximum number of transactions in 
 * a given period of time.
 *
 * As an example, the following code will cap the transaction count
 * at 15 every second (a.k.a. 15 TPS).
 *
 * <pre>
 *
 *  ThroughputControl throughput = new ThroughputControl(15, 1000);
 *
 *  while (isConditionTrue()) {
 *      throughput.control();
 *      // Do stuff.
 *  }
 *
 * </pre>
 */
public class ThroughputControl {
    private int[] period;
    private int[] max;
    private AtomicInteger[] cnt;
    private long[] start;
    private long[] sleep;
    private static final long MIN_SLEEP = 50L;
    private static final long MAX_SLEEP = 500L;
    private final ReentrantLock[] locks;

    /***
     * @param max Maximum TPP allowed.
     * @param periodInMillis Duration (in milliseconds) for the monitoring period.
     */
    public ThroughputControl(int max, int periodInMillis) {
        this(
          new int[]{max},
          new int[]{periodInMillis}
        );
    }

    /**
     * Constructor with multiple periods
     *
     * @param maxTPP Maximum TPS allowed.
     * @param periodInMillis Duration (in milliseconds) for each period.
     */
    public ThroughputControl(int[] maxTPP, int[] periodInMillis) {
        super();
        int l = maxTPP.length;
        this.period = new int[l];
        this.max = new int[l];
        this.cnt = new AtomicInteger[l];
        this.start = new long[l];
        this.sleep = new long[l];
        this.locks = new ReentrantLock[l];

        for (int i = 0; i < l; i++) {
            this.max[i] = maxTPP[i]; // Start at minimum Transactions per Period.
            this.period[i] = periodInMillis[i];
            this.sleep[i] = Math.min(Math.max(periodInMillis[i] / 10, MIN_SLEEP), MAX_SLEEP);
            this.start[i] = Instant.now().toEpochMilli();
            this.cnt[i] = new AtomicInteger(0);
            this.locks[i] = new ReentrantLock();
        }
    }


    /**
     * control should be called on every transaction.
     * it may sleep for a while in order to control the system throughput
     *
     * @return aprox sleep time or zero if no sleep
     */
    public long control() {
        long init = Instant.now().toEpochMilli();
        boolean delayed = false;

        for (int i = 0; i < cnt.length; i++) {
            locks[i].lock();
            try {
                cnt[i].incrementAndGet();
                if (cnt[i].get() > max[i]) {
                    delayed = true;
                    long sleepTime = calculateSleepTime(i);
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                }
                checkAndReset(i);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                locks[i].unlock();
            }
        }

        return delayed ? Instant.now().toEpochMilli() - init : 0L;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ThroughputControl [");
        for (int i = 0; i < max.length; i++) {
            sb.append(String.format(
              "%d: max = %d, period = %dms",
              i, max[i], period[i]
            ));
            if (i < max.length - 1) {
                sb.append("; ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private long calculateSleepTime(int i) {
        long now = Instant.now().toEpochMilli();
        long elapsed = now - start[i];
        if (elapsed < period[i]) {
            return period[i] - elapsed;
        }
        return 0;
    }

    private void checkAndReset(int i) {
        long now = Instant.now().toEpochMilli();
        if (now - start[i] > period[i]) {
            long elapsed = now - start[i];
            int allowed = (int) (elapsed * max[i] / period[i]);
            start[i] = now;
            cnt[i].addAndGet(-allowed);
            if (cnt[i].get() < 0) {
                cnt[i].set(0);
            }
        }
    }
}
