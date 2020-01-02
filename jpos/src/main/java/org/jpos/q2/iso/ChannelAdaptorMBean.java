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

package org.jpos.q2.iso;

import org.jpos.q2.QBeanSupportMBean;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 2241 $ $Date: 2006-01-23 11:27:32 -0200 (Mon, 23 Jan 2006) $
 */
@SuppressWarnings("unused")
public interface ChannelAdaptorMBean extends QBeanSupportMBean {
    void setReconnectDelay(long delay);
    long getReconnectDelay();
    void setInQueue(java.lang.String in);
    String getInQueue();
    void setOutQueue(java.lang.String out);
    String getOutQueue();
    void setHost(java.lang.String host);
    String getHost();
    void setPort(int port);
    int getPort();
    void setSocketFactory(java.lang.String sFac);
    String getSocketFactory();
    boolean isConnected();
    void resetCounters();
    String getCountersAsString();
    int getTXCounter();
    int getRXCounter();
    int getConnectsCounter();
    long getLastTxnTimestampInMillis();
    long getIdleTimeInMillis();
}
