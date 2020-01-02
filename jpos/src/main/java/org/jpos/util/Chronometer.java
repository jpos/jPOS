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

import java.util.concurrent.atomic.AtomicLong;

public class Chronometer {
    private AtomicLong start;
    private AtomicLong lap;

    public Chronometer() {
        this.start = new AtomicLong(System.currentTimeMillis());
        this.lap = new AtomicLong(start.get());
    }

    public long elapsed() {
        return Math.max(System.currentTimeMillis() - start.get(), 0L);
    }

    public void reset () {
        start.set(System.currentTimeMillis());
    }

    public long lap() {
        long now = System.currentTimeMillis();
        long elapsed = now - lap.get();
        lap.set(now);
        return Math.max(elapsed, 0L);
    }

    @Override
    public String toString() {
        return "Chronometer{" +
          "elapsed=" + elapsed() +
          ", lap=" + (System.currentTimeMillis() - lap.get()) +
          '}';
    }
}

