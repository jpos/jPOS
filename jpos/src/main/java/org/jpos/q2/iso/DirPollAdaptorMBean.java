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
 * JMX management interface for {@link DirPollAdaptor}.
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision: 1859 $ $Date: 2003-12-05 23:52:20 -0300 (Fri, 05 Dec 2003) $
 */
public interface DirPollAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  /**
   * Sets the base directory path.
   * @param path the directory path
   */
  void setPath(java.lang.String path);

  /**
   * Sets the thread pool size.
   * @param size the number of threads
   */
  void setPoolSize(int size);

  /**
   * Returns the thread pool size.
   * @return pool size
   */
  int getPoolSize();

  /**
   * Returns the base directory path.
   * @return directory path
   */
  java.lang.String getPath();

  /**
   * Sets the polling interval.
   * @param pollInterval interval in milliseconds
   */
  void setPollInterval(long pollInterval);

  /**
   * Returns the polling interval.
   * @return interval in milliseconds
   */
  long getPollInterval();

  /**
   * Sets the file extension priorities.
   * @param priorities blank-separated list of extensions
   */
  void setPriorities(java.lang.String priorities);

  /**
   * Returns the file extension priorities.
   * @return blank-separated priority list
   */
  java.lang.String getPriorities();

  /**
   * Sets the processor class name.
   * @param processor fully qualified class name
   */
  void setProcessor(java.lang.String processor);

  /**
   * Returns the processor class name.
   * @return fully qualified class name
   */
  java.lang.String getProcessor();

}
