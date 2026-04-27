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

package org.jpos.transaction;

import org.jpos.q2.QBeanSupportMBean;
import java.util.Date;

/**
 * JMX management interface exposing the TransactionManager's queue counters
 * and TPS (transactions-per-second) metrics.
 */
@SuppressWarnings("unused")
public interface TransactionManagerMBean extends QBeanSupportMBean {
    /**
     * Returns the current head index of the transaction queue.
     *
     * @return queue head index
     */
    long getHead();
    /**
     * Returns the current tail index of the transaction queue.
     *
     * @return queue tail index
     */
    long getTail();
    /**
     * Returns the number of in-flight transactions ({@code tail - head}).
     *
     * @return outstanding transaction count
     */
    int getOutstandingTransactions();
    /**
     * Returns the number of session threads currently processing transactions.
     *
     * @return live session count
     */
    int getActiveSessions();
    /**
     * Returns the configured maximum number of concurrent session threads.
     *
     * @return maximum session count
     */
    int getMaxSessions();
    /**
     * Returns a human-readable summary of TPS statistics.
     *
     * @return TPS snapshot suitable for diagnostics
     */
    String getTPSAsString();
    /**
     * Returns the average transactions-per-second since the last reset.
     *
     * @return mean TPS
     */
    float getTPSAvg();
    /**
     * Returns the peak transactions-per-second observed since the last reset.
     *
     * @return peak TPS
     */
    int getTPSPeak();
    /**
     * Returns the wall-clock instant at which {@link #getTPSPeak()} was reached.
     *
     * @return timestamp of the TPS peak, or {@code null} if not yet observed
     */
    Date getTPSPeakWhen();
    /**
     * Returns the elapsed time in milliseconds covered by the current TPS window.
     *
     * @return elapsed window in milliseconds
     */
    long getTPSElapsed();
    /** Resets the TPS counters and start time. */
    void resetTPS();
}
