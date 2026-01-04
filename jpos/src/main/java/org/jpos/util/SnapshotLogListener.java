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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A LogListener implementation that allows logging during a configurable time window
 * every configurable period. This is useful for scenarios like stress testing where
 * logging needs to be minimized while retaining some visibility.
 */
public class SnapshotLogListener implements LogListener, Configurable {
    private long windowMillis;
    private long periodMillis;
    private AtomicLong lastWindowStart = new AtomicLong(0);

    /**
     * Logs the given event if the current time falls within the active logging window.
     *
     * @param ev the LogEvent to be evaluated for logging.
     * @return the same LogEvent if logging is allowed, or {@code null} to suppress logging.
     */
    @Override
    public LogEvent log(LogEvent ev) {
        long now = Instant.now().toEpochMilli();
        long currentWindowStart = lastWindowStart.get();
        if (now >= currentWindowStart && now < currentWindowStart + windowMillis) {
            return ev;
        }

        // Check if it's time to start a new logging window
        if (now >= currentWindowStart + periodMillis) {
            if (lastWindowStart.compareAndSet(currentWindowStart, now)) {
                return ev; // Allow logging for the new window
            }
        }
        return null;
    }

    /**
     * Configures the SnapshotLogListener with the provided parameters.
     * 
     * <p>If the "window" or "period" parameters are not provided in the configuration,
     * default values of 1000 milliseconds for the logging window and 60000 milliseconds
     * for the period are used.</p>
     *
     * @param cfg the Configuration object containing the "window" and "period" parameters.
     * @throws ConfigurationException if the configuration is invalid.
     */
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        windowMillis = cfg.getLong("window", 1000L);
        periodMillis = cfg.getLong("period", 60000L);

        if (windowMillis <= 0 || periodMillis <= 0 || windowMillis >= periodMillis) {
            throw new ConfigurationException("windowMillis must be positive and less than periodMillis");
        }
    }
}
