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

package org.jpos.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.search.Search;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Factory helpers that create or look up Micrometer meters defined by
 * {@link MeterInfo}, ensuring duplicate registrations resolve to the same instance.
 */
public class MeterFactory {
    /** Default constructor; no instance state to initialise. */
    public MeterFactory() {}
    private static final Lock metersLock = new ReentrantLock();

    /**
     * Returns the {@link Timer} associated with {@code meterInfo} and {@code tags},
     * creating it (with histogram and 50/95 percentiles) when absent.
     *
     * @param registry the Micrometer registry
     * @param meterInfo meter id/description/default-tag descriptor
     * @param tags extra tags to combine with {@link MeterInfo#add(Tags)}
     * @return the (possibly existing) Timer
     */
    public static Timer timer(MeterRegistry registry, MeterInfo meterInfo, Tags tags) {
        return createMeter(registry, meterInfo, tags,
          () -> Timer.builder(meterInfo.id()).tags(meterInfo.add(tags)).description(meterInfo.description())
            .publishPercentiles(0.5, 0.95)
            .publishPercentileHistogram()
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(60))
            .register(registry));
    }

    /**
     * Returns the {@link Counter} associated with {@code meterInfo} and {@code tags},
     * creating it when absent.
     *
     * @param registry the Micrometer registry
     * @param meterInfo meter id/description/default-tag descriptor
     * @param tags extra tags to combine with {@link MeterInfo#add(Tags)}
     * @return the (possibly existing) Counter
     */
    public static Counter counter(MeterRegistry registry, MeterInfo meterInfo, Tags tags) {
        return createMeter(registry, meterInfo, tags,
          () -> Counter.builder(meterInfo.id()).tags(meterInfo.add(tags)).description(meterInfo.description()).register(registry));
    }

    /**
     * Registers (or updates) a freely-named {@link Counter}, bypassing the
     * {@link MeterInfo} catalog.
     *
     * @param registry the Micrometer registry
     * @param meterName meter id
     * @param tags meter tags
     * @param description meter description
     * @return the registered Counter
     */
    public static Counter updateCounter(MeterRegistry registry, String meterName, Tags tags, String description) {
        return Counter.builder(meterName).tags(tags).description(description).register(registry);
    }

    /**
     * Registers (or updates) the {@link Counter} identified by {@code meterInfo}.
     *
     * @param registry the Micrometer registry
     * @param meterInfo meter id/description/default-tag descriptor
     * @param tags extra tags to combine with {@link MeterInfo#add(Tags)}
     * @return the registered Counter
     */
    public static Counter updateCounter(MeterRegistry registry, MeterInfo meterInfo, Tags tags) {
        return updateCounter(registry, meterInfo.id(), meterInfo.add(tags), meterInfo.description());
    }

    /**
     * Returns the {@link Gauge} associated with {@code meterInfo} and {@code tags},
     * creating one bound to {@code n} when absent.
     *
     * @param registry the Micrometer registry
     * @param meterInfo meter id/description/default-tag descriptor
     * @param tags extra tags to combine with {@link MeterInfo#add(Tags)}
     * @param unit base unit, or {@code null} for none
     * @param n supplier called to read the current gauge value
     * @return the (possibly existing) Gauge
     */
    public static Gauge gauge(MeterRegistry registry, MeterInfo meterInfo, Tags tags, String unit, Supplier<Number> n) {
        return createMeter(registry, meterInfo, tags,
          () -> Gauge.builder(meterInfo.id(), n)
            .tags(meterInfo.add(tags))
            .description(meterInfo.description())
            .baseUnit(unit)
            .register(registry));
    }

    /**
     * Removes the supplied meters from the registry, skipping {@code null} entries.
     *
     * @param registry the Micrometer registry
     * @param meters meters to remove
     */
    public static void remove  (MeterRegistry registry, Meter... meters) {
        Arrays.stream(meters).filter(Objects::nonNull).forEach(registry::remove);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Meter> T createMeter(MeterRegistry registry, MeterInfo meterInfo, Tags tags, Callable<T> creator) {
        metersLock.lock();
        try {
            T meter = (T) Search.in(registry).name(meterInfo.id()).tags(meterInfo.add(tags)).meter();
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
