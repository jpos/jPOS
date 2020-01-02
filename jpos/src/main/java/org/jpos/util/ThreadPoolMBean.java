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

public interface ThreadPoolMBean {
    /**
     * @return number of jobs processed by this pool
     */
    int getJobCount();

    /**
     * @return number of active threads
     */
    int getPoolSize();

    /**
     * @return max number of active threads allowed
     */
    int getMaxPoolSize();

    /**
     * @return number of idle threads
     */
    int getIdleCount();

    /**
     * @return number of Pending jobs
     */
    int getPendingCount();
}
