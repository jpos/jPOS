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

package org.jpos.q2.qbean;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 1.3 $ $Date: 2004/12/17 20:12:57 $
 */
public interface SpaceAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  /**
   * Sets the configured space name managed by this adaptor.
   *
   * @param spaceName SpaceFactory-style space URI
   */
  void setSpaceName(java.lang.String spaceName) ;

  /**
   * Returns the configured space name.
   *
   * @return SpaceFactory-style space URI
   */
  java.lang.String getSpaceName() ;

  /**
   * Returns a snapshot of the keys currently held by the managed space.
   *
   * @return current key set
   */
  java.util.Set getKeys() ;

}
