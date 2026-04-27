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

/**
 * MBean interface.
 * @author Alwyn Schoeman
 * @version $Revision: 2072 $ $Date: 2004-12-07 08:21:24 -0300 (Tue, 07 Dec 2004) $
 */
public interface QServerMBean extends org.jpos.q2.QBeanSupportMBean {

  /**
   * Sets the TCP port the server listens on.
   *
   * @param port TCP port number
   */
  void setPort(int port) ;

  /**
   * Returns the configured listen port.
   *
   * @return TCP port number
   */
  int getPort() ;

  /**
   * Sets the fully qualified packager class name used by accepted channels.
   *
   * @param packager packager class name
   */
  void setPackager(java.lang.String packager) ;

  /**
   * Returns the configured packager class name.
   *
   * @return packager class name
   */
  java.lang.String getPackager() ;

  /**
   * Sets the fully qualified channel class name instantiated for each accepted connection.
   *
   * @param channel channel class name
   */
  void setChannel(java.lang.String channel) ;

  /**
   * Returns the configured channel class name.
   *
   * @return channel class name
   */
  java.lang.String getChannel() ;

  /**
   * Sets the maximum number of concurrent client sessions accepted by the server.
   *
   * @param maxSessions maximum sessions
   */
  void setMaxSessions(int maxSessions) ;

  /**
   * Returns the configured maximum number of concurrent client sessions.
   *
   * @return maximum sessions
   */
  int getMaxSessions() ;

  /**
   * Sets the fully qualified socket-factory class name used to create the server socket.
   *
   * @param sFactory socket factory class name
   */
  void setSocketFactory(java.lang.String sFactory) ;

  /**
   * Returns the configured socket-factory class name.
   *
   * @return socket factory class name
   */
  java.lang.String getSocketFactory() ;
  /**
   * Returns the names of all currently-active accepted channels, comma-separated.
   *
   * @return comma-separated active channel names
   */
  String getISOChannelNames();
  /**
   * Returns aggregated counters across all active channels.
   *
   * @return counter snapshot suitable for diagnostics
   */
  String getCountersAsString ();
  /**
   * Returns counters for a single named accepted channel.
   *
   * @param isoChannelName accepted channel's name
   * @return counter snapshot suitable for diagnostics, or empty if the channel is unknown
   */
  String getCountersAsString (String isoChannelName);
}
