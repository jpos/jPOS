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

import java.time.Duration;

/**
 * ThroughputControl can be used to limit the throughput 
 * of a system to a maximum number of transactions in 
 * a given period of time.
 */
public class ThroughputControl {
    private int[] period;
    private int[] max;
    private int[] cnt;
    private Duration[] start;
    private Duration[] sleep;

    /**
     * @param maxTransactions ditto
     * @param periodInMillis ditto
     */
    public ThroughputControl (int maxTransactions, int periodInMillis) {
        this (new int[] { maxTransactions },
              new int[] { periodInMillis });
    }
    /**
     * @param maxTransactions ditto
     * @param periodInMillis ditto
     */
    public ThroughputControl (int[] maxTransactions, int[] periodInMillis) {
        super();
        int l = maxTransactions.length;
        period = new int[l];
        max = new int[l];
        cnt = new int[l];
        start = new Duration[l];
        sleep = new Duration[l];
        for (int i=0; i<l; i++) {
            this.max[i]    = maxTransactions[i];
            this.period[i] = periodInMillis[i];
            this.sleep[i]  = Duration.ofMillis(Math.min(Math.max (periodInMillis[i]/10, 500L),50L));
            this.start[i]  = Duration.ofNanos(System.nanoTime());
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
        Duration init = Duration.ofNanos(System.nanoTime());
        for (int i=0; i<cnt.length; i++) {
            synchronized (this) {
                cnt[i]++;
            }
            do {
                if (cnt[i] > max[i]) {
                    delayed = true;
                    try { 
                        Thread.sleep (sleep[i].toMillis());
                    } catch (InterruptedException e) { }
                }
                synchronized (this) {
                    Duration now = Duration.ofNanos(System.nanoTime());
                    if (now.minus(start[i]).toMillis() > period[i]) {
                        long elapsed = now.minus(start[i]).toMillis();
                        int  allowed = (int) (elapsed * max[i] / period[i]);
                        start[i] = now;
                        cnt[i] = Math.max (cnt[i] - allowed, 0);
                    }
                }
            } while (cnt[i] > max[i]);
        }
        return delayed ? Duration.ofNanos(System.nanoTime()).minus(init).toMillis() : 0L;
    }
}

