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

package org.jpos.q2.qbean;

/**
 * MBean interface.
 * @author apr@cs.com.uy
 * @version $Id: SystemMonitor.java,v 1.2 2005/11/07 20:12:11 marklsalter Exp $
 */
public interface SystemMonitorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setSleepTime(long sleepTime) ;

  long getSleepTime() ;

  void setDetailRequired(boolean detail) ;

  boolean getDetailRequired() ;

}
