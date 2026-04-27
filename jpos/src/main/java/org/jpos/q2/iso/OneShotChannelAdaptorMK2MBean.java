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
 *
 * @author Alejandro Revilla
 * @author Thomas L. Kjeldsen
 * @author Victor Salaman
 */
public interface OneShotChannelAdaptorMK2MBean extends QBeanSupportMBean
{
    /**
     * Indicates whether the adaptor's underlying channel is currently connected.
     *
     * @return {@code true} if the channel is up
     */
    boolean isConnected();

    /**
     * Returns the inbound queue name.
     *
     * @return queue name
     */
    java.lang.String getInQueue();

    /**
     * Sets the inbound queue name.
     *
     * @param in queue name
     */
    void setInQueue(java.lang.String in);

    /**
     * Returns the outbound queue name.
     *
     * @return queue name
     */
    java.lang.String getOutQueue();

    /**
     * Sets the outbound queue name.
     *
     * @param out queue name
     */
    void setOutQueue(java.lang.String out);

    /**
     * Returns the configured remote host.
     *
     * @return host name or address
     */
    java.lang.String getHost();

    /**
     * Sets the remote host.
     *
     * @param host host name or address
     */
    void setHost(java.lang.String host);

    /**
     * Returns the configured remote port.
     *
     * @return TCP port number
     */
    int getPort();

    /**
     * Sets the remote port.
     *
     * @param port TCP port number
     */
    void setPort(int port);

    /**
     * Returns the configured socket-factory class name.
     *
     * @return socket factory class name
     */
    java.lang.String getSocketFactory();

    /**
     * Sets the socket-factory class name.
     *
     * @param sFac socket factory class name
     */
    void setSocketFactory(java.lang.String sFac);

}
