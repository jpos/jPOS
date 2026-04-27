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

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Helpers that build pre-tuned {@link java.util.concurrent} primitives for
 * use across jPOS, currently focused on virtual-thread-backed scheduled
 * executors with sensible shutdown semantics.
 */
public class ConcurrentUtil {
    /** Utility class; instances carry no state. */
    public ConcurrentUtil() {}
    /**
     * Returns a single-thread {@link ScheduledThreadPoolExecutor} backed by a
     * named virtual thread, configured to drop delayed/periodic tasks at shutdown.
     *
     * @return the configured scheduler
     */
    public static ScheduledThreadPoolExecutor newScheduledThreadPoolExecutor() {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1,
          r -> {
              Thread t = Thread.ofVirtual().factory().newThread(r);
              t.setName("spaceGC");
              return t;
          });
        stpe.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return stpe;
    }
}
