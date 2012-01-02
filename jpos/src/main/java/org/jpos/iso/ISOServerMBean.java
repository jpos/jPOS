/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jpos.util.ThreadPoolMBean;

public interface ISOServerMBean extends ThreadPoolMBean {
    public int getPort ();
    public void resetCounters ();
    public int getConnectionCount ();
    public String getISOChannelNames();
    public String getCountersAsString (String isoChannelName);
    public int getTXCounter();
    public int getRXCounter();
    public long getLastTxnTimestampInMillis();
    public long getIdleTimeInMillis();
}

