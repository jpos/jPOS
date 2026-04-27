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
 * @author Alejandro Revilla
 * @author Thomas L. Kjeldsen
 * @version $Revision: 2245 $ $Date: 2006-01-31 08:27:10 -0200 (Tue, 31 Jan 2006) $
 */
public interface OneShotChannelAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  /**
   * Sets the inbound queue name.
   *
   * @param in queue name
   */
  void setInQueue(java.lang.String in) ;

  /**
   * Returns the inbound queue name.
   *
   * @return queue name
   */
  java.lang.String getInQueue() ;

  /**
   * Sets the outbound queue name.
   *
   * @param out queue name
   */
  void setOutQueue(java.lang.String out) ;

  /**
   * Returns the outbound queue name.
   *
   * @return queue name
   */
  java.lang.String getOutQueue() ;

  /**
   * Sets the remote host.
   *
   * @param host host name or address
   */
  void setHost(java.lang.String host) ;

  /**
   * Returns the configured remote host.
   *
   * @return host name or address
   */
  java.lang.String getHost() ;

  /**
   * Sets the remote port.
   *
   * @param port TCP port number
   */
  void setPort(int port) ;

  /**
   * Returns the configured remote port.
   *
   * @return TCP port number
   */
  int getPort() ;

  /**
   * Sets the socket-factory class name.
   *
   * @param sFac socket factory class name
   */
  void setSocketFactory(java.lang.String sFac) ;

  /**
   * Returns the configured socket-factory class name.
   *
   * @return socket factory class name
   */
  java.lang.String getSocketFactory() ;

}
