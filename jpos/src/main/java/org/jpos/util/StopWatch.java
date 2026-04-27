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

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Simple wall-clock timer that pads a block of work to a minimum duration,
 * useful when masking timing differences between branches (e.g. successful
 * vs. failed authentications).
 */
public class StopWatch {
    long end;
    /**
     * Constructs a StopWatch that finishes no earlier than {@code period} from now.
     *
     * @param period minimum duration to elapse before {@link #finish()} returns
     * @param unit unit for {@code period}
     */
    public StopWatch (long period, TimeUnit unit) {
        end = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(period, unit);
    }
    /**
     * Convenience constructor that takes the minimum duration in milliseconds.
     *
     * @param periodInMillis minimum duration in milliseconds
     */
    public StopWatch (long periodInMillis) {
        this (periodInMillis, TimeUnit.MILLISECONDS);
    }
    /** Sleeps until the configured deadline, returning immediately when already past it. */
    public void finish() {
        long now = System.currentTimeMillis();
        if (end > now) {
            try {
                Thread.sleep(end - now);
            } catch (InterruptedException ignored) { }
        }
    }
    /**
     * Indicates whether the deadline has been reached.
     *
     * @return {@code true} if the configured period has elapsed
     */
    public boolean isFinished() {
        return System.currentTimeMillis() >= end;
    }

    /**
     * Runs {@code f} and pads its execution to at least {@code period}.
     *
     * @param <T> result type returned by {@code f}
     * @param period minimum duration to elapse
     * @param unit unit for {@code period}
     * @param f the work to perform
     * @return the value returned by {@code f}
     */
    public static <T> T get(long period, TimeUnit unit, Supplier<T> f) {
        StopWatch w = new StopWatch(period, unit);
        T t = f.get();
        w.finish();
        return t;
    }

    /**
     * Runs {@code f} and pads its execution to at least {@code period} milliseconds.
     *
     * @param <T> result type returned by {@code f}
     * @param period minimum duration in milliseconds
     * @param f the work to perform
     * @return the value returned by {@code f}
     */
    public static <T> T get(long period, Supplier<T> f) {
        return get(period, TimeUnit.MILLISECONDS, f);
    }
}
