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
    /** Returns counters as a string for the named channel.
     * @param isoChannelName the channel name
     * @return counters string
     */
    String getCountersAsString(String isoChannelName);
    /** Returns the transmit counter.
     * @return TX count
     */
    int getTXCounter();
    /** Returns the receive counter.
     * @return RX count
     */
    int getRXCounter();
    /** Returns the timestamp of the last transaction in milliseconds.
     * @return last transaction timestamp
     */
    long getLastTxnTimestampInMillis();
    /** Returns the idle time in milliseconds.
     * @return idle time
     */
    long getIdleTimeInMillis();
}
