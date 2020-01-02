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

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class StopWatch {
    long end;
    public StopWatch (long period, TimeUnit unit) {
        end = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(period, unit);
    }
    public StopWatch (long periodInMillis) {
        this (periodInMillis, TimeUnit.MILLISECONDS);
    }
    public void finish() {
        long now = System.currentTimeMillis();
        if (end > now) {
            try {
                Thread.sleep(end - now);
            } catch (InterruptedException ignored) { }
        }
    }
    public boolean isFinished() {
        return System.currentTimeMillis() >= end;
    }

    public static <T> T get(long period, TimeUnit unit, Supplier<T> f) {
        StopWatch w = new StopWatch(period, unit);
        T t = f.get();
        w.finish();
        return t;
    }

    public static <T> T get(long period, Supplier<T> f) {
        return get(period, TimeUnit.MILLISECONDS, f);
    }
}
