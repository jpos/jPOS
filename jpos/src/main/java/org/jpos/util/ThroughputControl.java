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

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * @param maxTransactions Maximum number of transactions allowed.
     * @param periodInMillis Duration (in milliseconds) over which the maximum is calculated.
     */
    public ThroughputControl (int maxTransactions, int periodInMillis) {
        this (new int[] { maxTransactions },
              new int[] { periodInMillis });
    }
    /**
     * @param maxTransactions Array of maximum transactions allowed for each period.
     * @param periodInMillis Array of periods (in milliseconds) corresponding to each maximum.
     */
    public ThroughputControl (int[] maxTransactions, int[] periodInMillis) {
        super();
        int l = maxTransactions.length;
        period = new int[l];
        max = new int[l];
        cnt = new AtomicInteger[l];
        start = new long[l];
        sleep = new long[l];
        for (int i=0; i<l; i++) {
            this.max[i]    = maxTransactions[i];
            this.period[i] = periodInMillis[i];
            // Calculate sleep time, ensuring it is within the defined minimum and maximum bounds.
            this.sleep[i]  = Math.min(Math.max(periodInMillis[i] / 10, MIN_SLEEP), MAX_SLEEP);
            this.start[i]  = Instant.now().toEpochMilli();
            this.cnt[i] = new AtomicInteger(0);
        }
    }

    /**
     * control should be called on every transaction.
     * it may sleep for a while in order to control the system throughput
     *
     * @return aprox sleep time or zero if no sleep
     */
    public long control() {
        boolean delayed = false;
        long init = Instant.now().toEpochMilli();
        for (int i = 0; i < cnt.length; i++) {
            if (cnt[i].incrementAndGet() > max[i]) {
                delayed = true;
                while (cnt[i].get() > max[i]) {
                    try {
                        Thread.sleep(sleep[i]);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    checkAndReset(i);
                }
            }
        }
        return delayed ? Instant.now().toEpochMilli() - init : 0L;
    }

    private synchronized void checkAndReset(int i) {
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
