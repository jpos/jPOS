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

/**
 * Exposes thread-pool metrics via JMX.
 */
public interface ThreadPoolMBean {
    /**
     * Returns the number of jobs processed by this pool.
     *
     * @return number of processed jobs
     */
    int getJobCount();

    /**
     * Returns the number of running threads in the pool.
     *
     * @return current pool size
     */
    int getPoolSize();

    /**
     * Returns the maximum number of threads allowed in the pool.
     *
     * @return maximum pool size
     */
    int getMaxPoolSize();

    /**
     * Returns the number of idle threads.
     *
     * @return idle thread count
     */
    int getIdleCount();

    /**
     * Returns the number of pending jobs.
     *
     * @return pending job count
     */
    int getPendingCount();
}
