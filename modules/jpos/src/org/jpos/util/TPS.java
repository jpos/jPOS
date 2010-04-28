/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

import java.util.concurrent.Semaphore;

public class TPS {
    volatile int count;
    long start;
    Semaphore sem;
    static final long TO_NANOS = 1000000L;
    long period = 1000 * TO_NANOS;
    float tps;

    public TPS() {
        this (1000L);
    }

    /**
     * @param periodInMillis ditto
     */
    public TPS(long periodInMillis) {
        super();
        this.period = periodInMillis * TO_NANOS;
        start = System.nanoTime();
        sem = new Semaphore(1);

    }

    public void tick() {
        count++;
    }
    public float floatValue() {
        if (sem.tryAcquire()) {
            long now = System.nanoTime();
            long interval = now - start;
            if (interval > period) {
                tps = (float) period * count / interval;
                count = 0;
                start = now;
            }
        }
        sem.release();
        return tps;
    }
    public int intValue() {
        return Math.round(floatValue()); 
    }
    public long getPeriodInMillis() {
        return period / TO_NANOS;
    }
    public String toString() {
        return String.format ("%.2f", floatValue());
    }
}
