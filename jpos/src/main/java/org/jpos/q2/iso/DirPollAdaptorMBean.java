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
 * @version $Revision: 1859 $ $Date: 2003-12-05 23:52:20 -0300 (Fri, 05 Dec 2003) $
 */
public interface DirPollAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setPath(java.lang.String path) ;

  void setPoolSize(int size) ;

  int getPoolSize() ;

  java.lang.String getPath() ;

  void setPollInterval(long pollInterval) ;

  long getPollInterval() ;

  void setPriorities(java.lang.String priorities) ;

  java.lang.String getPriorities() ;

  void setProcessor(java.lang.String processor) ;

  java.lang.String getProcessor() ;

}
