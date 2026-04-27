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

package org.jpos.q2.iso;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 2223 $ $Date: 2005-11-29 21:04:41 -0200 (Tue, 29 Nov 2005) $
 */
public interface QMUXMBean extends org.jpos.q2.QBeanSupportMBean {
    /**
     * Sets the inbound (response) queue name.
     *
     * @param in queue name
     */
    void setInQueue(java.lang.String in) ;
    /**
     * Returns the inbound (response) queue name.
     *
     * @return queue name
     */
    String getInQueue() ;
    /**
     * Sets the outbound (request) queue name.
     *
     * @param out queue name
     */
    void setOutQueue(java.lang.String out) ;
    /**
     * Returns the outbound (request) queue name.
     *
     * @return queue name
     */
    String getOutQueue() ;
    /**
     * Sets the queue name where unmatched inbound messages are forwarded.
     *
     * @param unhandled queue name
     */
    void setUnhandledQueue(java.lang.String unhandled) ;
    /**
     * Returns the queue name where unmatched inbound messages are forwarded.
     *
     * @return queue name, or {@code null} if not configured
     */
    String getUnhandledQueue() ;
    /** Resets all transaction counters and the last-transaction timestamp. */
    void resetCounters();
    /**
     * Returns the current counters formatted as a single human-readable string.
     *
     * @return counter snapshot suitable for diagnostics
     */
    String getCountersAsString();
    /**
     * Returns the number of messages transmitted since the last reset.
     *
     * @return TX message count
     */
    int getTXCounter();
    /**
     * Returns the number of messages received since the last reset.
     *
     * @return RX message count
     */
    int getRXCounter();
    /**
     * Returns the number of TX requests that expired without a matching response.
     *
     * @return expired TX count
     */
    int getTXExpired();
    /**
     * Returns the number of TX requests still awaiting a response.
     *
     * @return pending TX count
     */
    int getTXPending();
    /**
     * Returns the number of received responses that arrived too late and were discarded.
     *
     * @return expired RX count
     */
    int getRXExpired();
    /**
     * Returns the number of in-flight responses awaiting matching.
     *
     * @return pending RX count
     */
    int getRXPending();
    /**
     * Returns the number of received messages that did not match any pending request.
     *
     * @return unhandled RX count
     */
    int getRXUnhandled();
    /**
     * Returns the number of unmatched messages successfully forwarded to listeners.
     *
     * @return forwarded RX count
     */
    int getRXForwarded();
    /**
     * Returns the wall-clock timestamp of the last successful transaction.
     *
     * @return milliseconds since the epoch, or {@code 0} if no transaction has completed
     */
    long getLastTxnTimestampInMillis();
    /**
     * Returns the time elapsed since the last successful transaction.
     *
     * @return idle time in milliseconds, or {@code -1} if no transaction has completed
     */
    long getIdleTimeInMillis();
}
