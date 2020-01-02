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

package org.jpos.q2.security;

/**
 * MBean interface.
 * @author Hani Kirollos
 * @author Alejandro Revilla
 * @version $Revision: 1587 $ $Date: 2003-06-03 09:09:37 -0300 (Tue, 03 Jun 2003) $
 */
public interface KeyStoreAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setImpl(java.lang.String clazz) ;

  java.lang.String getImpl() ;

}
