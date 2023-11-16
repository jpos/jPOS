/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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
package org.jpos.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.search.Search;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class MeterFactory {
    private static final Lock metersLock = new ReentrantLock();

    public static Timer timer(MeterRegistry registry, MeterInfo meterInfo, Tags tags) {
        return createMeter(registry, meterInfo, tags,
          () -> Timer.builder(meterInfo.id()).tags(tags).description(meterInfo.description())
            .publishPercentiles(0.5, 0.95)
            .publishPercentileHistogram()
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(60))
            .register(registry));
    }

    public static Counter counter(MeterRegistry registry, MeterInfo meterInfo, Tags tags) {
        return createMeter(registry, meterInfo, tags,
          () -> Counter.builder(meterInfo.id()).tags(tags).description(meterInfo.description()).register(registry));
    }

    public static Gauge gauge(MeterRegistry registry, MeterInfo meterInfo, Tags tags, String unit, Supplier<Number> n) {
        return createMeter(registry, meterInfo, tags,
          () -> Gauge.builder(meterInfo.id(), n)
            .tags(tags)
            .description(meterInfo.description())
            .baseUnit(unit)
            .register(registry));
    }

    public static void remove (MeterRegistry registry, Meter meter) {
        registry.getMeters().remove(meter);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Meter> T createMeter(MeterRegistry registry, MeterInfo meterInfo, Tags tags, Callable<T> creator) {
        try {
            metersLock.lock();
            T meter = (T) Search.in(registry).name(meterInfo.id()).tags(tags).meter();
            if (meter == null) {
                try {
                    meter = creator.call();
                } catch (Exception e) {
                    throw new RuntimeException (e);
                }
            }
            return meter;
        } finally {
            metersLock.unlock();
        }
    }
}
