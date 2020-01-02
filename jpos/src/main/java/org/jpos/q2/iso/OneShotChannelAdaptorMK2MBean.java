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
 *
 * @author Alejandro Revilla
 * @author Thomas L. Kjeldsen
 * @author Victor Salaman
 */
public interface OneShotChannelAdaptorMK2MBean extends QBeanSupportMBean
{
    boolean isConnected();

    java.lang.String getInQueue();

    void setInQueue(java.lang.String in);

    java.lang.String getOutQueue();

    void setOutQueue(java.lang.String out);

    java.lang.String getHost();

    void setHost(java.lang.String host);

    int getPort();

    void setPort(int port);

    java.lang.String getSocketFactory();

    void setSocketFactory(java.lang.String sFac);

}
