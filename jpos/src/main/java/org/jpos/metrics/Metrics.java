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

import io.micrometer.core.instrument.MeterRegistry;
import org.jpos.q2.Q2;

/**
 * Utility class for accessing Micrometer metrics in jPOS components.
 *
 * <p>This class provides convenient static access to the MeterRegistry for components
 * that don't extend QBeanSupport or otherwise have direct access to the Q2 container.</p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>For QBeans (components extending QBeanSupport)</h3>
 * <pre>
 * MeterRegistry registry = getServer().getMeterRegistry();
 * </pre>
 *
 * <h3>For Non-QBean components (e.g., providers, participants)</h3>
 * <pre>
 * import org.jpos.metrics.Metrics;
 *
 * MeterRegistry registry = Metrics.getMeterRegistry();
 * Counter counter = Counter.builder("my.metric")
 *     .description("My metric description")
 *     .register(registry);
 * </pre>
 *
 * <h3>Using MeterFactory for consistent metrics</h3>
 * <pre>
 * MeterRegistry registry = Metrics.getMeterRegistry();
 * Counter counter = MeterFactory.counter(registry, myMeterInfo, Tags.of("key", "value"));
 * </pre>
 *
 * @see MeterFactory
 * @see MeterInfo
 * @see Q2#getMeterRegistry()
 */
public class Metrics {

    /**
     * Get the MeterRegistry from the default Q2 instance.
     *
     * <p>For use by components that don't have direct access to Q2 (non-QBeans).
     * This method looks up the Q2 instance registered as "Q2" in NameRegistrar
     * and returns its MeterRegistry, or null if Q2 is not available.</p>
     *
     * <p>Note: Q2.getQ2() returns null if Q2 is not found (uses getIfExists internally).</p>
     *
     * @return MeterRegistry from the default Q2 instance, or null if Q2 not available
     */
    public static MeterRegistry getMeterRegistry() {
        Q2 q2 = Q2.getQ2();
        return q2 != null ? q2.getMeterRegistry() : null;
    }

    /**
     * Get the MeterRegistry from the default Q2 instance, waiting up to timeout milliseconds.
     *
     * <p>This variant blocks until Q2 is available or the timeout expires.
     * Useful during initialization when Q2 might not be fully started yet.</p>
     *
     * @param timeout maximum time to wait in milliseconds
     * @return MeterRegistry from Q2
     * @throws org.jpos.util.NameRegistrar.NotFoundException if Q2 not found within timeout
     */
    public static MeterRegistry getMeterRegistry(long timeout) {
        return Q2.getQ2(timeout).getMeterRegistry();
    }

    /**
     * Get the MeterRegistry from a named Q2 instance.
     *
     * <p>Use this when multiple Q2 instances are running and you need to access
     * a specific instance's MeterRegistry.</p>
     *
     * @param q2Name Q2 instance name (e.g., "Q2", "Q2-1", etc.)
     * @return MeterRegistry from the named Q2 instance, or null if not found
     */
    public static MeterRegistry getMeterRegistry(String q2Name) {
        Q2 q2 = org.jpos.util.NameRegistrar.getIfExists(q2Name);
        return q2 != null ? q2.getMeterRegistry() : null;
    }

    /**
     * Get the MeterRegistry from a named Q2 instance, waiting up to timeout milliseconds.
     *
     * @param q2Name Q2 instance name
     * @param timeout maximum time to wait in milliseconds
     * @return MeterRegistry from named Q2 instance
     * @throws org.jpos.util.NameRegistrar.NotFoundException if Q2 not found within timeout
     */
    public static MeterRegistry getMeterRegistry(String q2Name, long timeout) {
        return org.jpos.util.NameRegistrar.<Q2>get(q2Name, timeout).getMeterRegistry();
    }
}
