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

package org.jpos.iso;

/** MBean interface for ISOServer management. */
public interface ISOServerMBean {
    /** Returns the port this server is listening on.
     * @return the port number
     */
    int getPort();
    /** Resets all server counters. */
    void resetCounters();
    /** Returns the current connection count.
     * @return number of active connections
     */
    int getConnectionCount();
    /** Returns the names of connected ISO channels.
     * @return channel names string
     */
    String getISOChannelNames();
    String getCountersAsString(String isoChannelName);
    int getTXCounter();
    int getRXCounter();
    long getLastTxnTimestampInMillis();
    long getIdleTimeInMillis();
}
