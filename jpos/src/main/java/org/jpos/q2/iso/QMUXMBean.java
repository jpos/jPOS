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

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 2223 $ $Date: 2005-11-29 21:04:41 -0200 (Tue, 29 Nov 2005) $
 */
public interface QMUXMBean extends org.jpos.q2.QBeanSupportMBean {
    void setInQueue(java.lang.String in) ;
    String getInQueue() ;
    void setOutQueue(java.lang.String out) ;
    String getOutQueue() ;
    void setUnhandledQueue(java.lang.String unhandled) ;
    String getUnhandledQueue() ;
    void resetCounters();
    String getCountersAsString();
    int getTXCounter();
    int getRXCounter();
    int getTXExpired();
    int getTXPending();
    int getRXExpired();
    int getRXPending();
    int getRXUnhandled();
    int getRXForwarded();
    long getLastTxnTimestampInMillis();
    long getIdleTimeInMillis();
}
