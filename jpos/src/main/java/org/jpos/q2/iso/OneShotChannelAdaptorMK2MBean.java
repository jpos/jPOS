/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2014 Alejandro P. Revilla
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
    public boolean isConnected();

    public java.lang.String getInQueue();

    public void setInQueue(java.lang.String in);

    public java.lang.String getOutQueue();

    public void setOutQueue(java.lang.String out);

    public java.lang.String getHost();

    public void setHost(java.lang.String host);

    public int getPort();

    public void setPort(int port);

    public java.lang.String getSocketFactory();

    public void setSocketFactory(java.lang.String sFac);

}
