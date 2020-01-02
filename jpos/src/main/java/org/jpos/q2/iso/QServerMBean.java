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
 * @author Alwyn Schoeman
 * @version $Revision: 2072 $ $Date: 2004-12-07 08:21:24 -0300 (Tue, 07 Dec 2004) $
 */
public interface QServerMBean extends org.jpos.q2.QBeanSupportMBean {

  void setPort(int port) ;

  int getPort() ;

  void setPackager(java.lang.String packager) ;

  java.lang.String getPackager() ;

  void setChannel(java.lang.String channel) ;

  java.lang.String getChannel() ;

  void setMaxSessions(int maxSessions) ;

  int getMaxSessions() ;

  void setMinSessions(int minSessions) ;

  int getMinSessions() ;

  void setSocketFactory(java.lang.String sFactory) ;

  java.lang.String getSocketFactory() ;
  String getISOChannelNames();
  String getCountersAsString ();
  String getCountersAsString (String isoChannelName);
}
