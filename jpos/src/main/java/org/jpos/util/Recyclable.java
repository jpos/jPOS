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
import java.util.function.Supplier;

/**
 * Caches a supplier's result up to approximately <code>maxCycles</code>.
 *
 * <p>After approximately <code>maxCycles</code> calls to its <code>get()</code> operation,
 * <code>Recyclable</code> fetches a new result from its underlying Supplier.
 * </p>
 *
 * @param <T> the type of results supplied by this supplier
 * @since 2.1.3
 */
public class Recyclable<T> implements Supplier<T> {
    private Supplier<T> supplier;
    private long maxCycles;
    private AtomicLong cycles = new AtomicLong();
    private volatile T obj;

    public Recyclable(Supplier<T> supplier, long maxCycles) {
        this.supplier = supplier;
        this.maxCycles = maxCycles;
    }

    @Override
    public T get() {
        if (cycles.getAndIncrement() == maxCycles || obj == null) {
            obj = supplier.get();
            cycles.set(0);
        }
        return obj;
    }
}
