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

import org.jpos.q2.QBeanSupportMBean;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 2241 $ $Date: 2006-01-23 11:27:32 -0200 (Mon, 23 Jan 2006) $
 */
@SuppressWarnings("unused")
public interface ChannelAdaptorMBean extends QBeanSupportMBean {
    /**
     * Sets the reconnect delay.
     * @param delay reconnect delay in milliseconds
     */
    void setReconnectDelay(long delay);
    /**
     * Returns the reconnect delay.
     * @return reconnect delay in milliseconds
     */
    long getReconnectDelay();
    /**
     * Sets the incoming space queue name.
     * @param in name of the incoming space queue
     */
    void setInQueue(java.lang.String in);
    /**
     * Returns the incoming space queue name.
     * @return name of the incoming space queue
     */
    String getInQueue();
    /**
     * Sets the outgoing space queue name.
     * @param out name of the outgoing space queue
     */
    void setOutQueue(java.lang.String out);
    /**
     * Returns the outgoing space queue name.
     * @return name of the outgoing space queue
     */
    String getOutQueue();
    /**
     * Sets the remote host name or address.
     * @param host remote host
     */
    void setHost(java.lang.String host);
    /**
     * Returns the remote host name or address.
     * @return remote host
     */
    String getHost();
    /**
     * Sets the remote port number.
     * @param port remote port
     */
    void setPort(int port);
    /**
     * Returns the remote port number.
     * @return remote port
     */
    int getPort();
    /**
     * Sets the socket factory class name.
     * @param sFac socket factory class name
     */
    void setSocketFactory(java.lang.String sFac);
    /**
     * Returns the socket factory class name.
     * @return socket factory class name
     */
    String getSocketFactory();
    /**
     * Returns true if the channel is currently connected.
     * @return true if connected
     */
    boolean isConnected();
    /** Resets all message counters to zero. */
    void resetCounters();
    /**
     * Returns all counters as a human-readable string.
     * @return counters string
     */
    String getCountersAsString();
    /**
     * Returns the transmitted message count.
     * @return TX count
     */
    int getTXCounter();
    /**
     * Returns the received message count.
     * @return RX count
     */
    int getRXCounter();
    /**
     * Returns the number of successful connections since last reset.
     * @return connect count
     */
    int getConnectsCounter();
    /**
     * Returns the timestamp of the last transaction.
     * @return timestamp in milliseconds
     */
    long getLastTxnTimestampInMillis();
    /**
     * Returns the idle time since the last transaction.
     * @return idle time in milliseconds
     */
    long getIdleTimeInMillis();
}
