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

package org.jpos.q2;

import org.jdom2.Element;

import java.net.URL;

/**
 * JMX management interface for {@link QBeanSupport}-based QBeans.
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface QBeanSupportMBean extends QBean, QPersist {
    /**
     * Sets the Q2 server instance for this QBean.
     * @param server the Q2 server
     */
    void setServer(Q2 server);
    /**
     * Returns the Q2 server instance.
     * @return the Q2 server
     */
    Q2 getServer();
    /**
     * Sets the persistent configuration element.
     * @param e the configuration element
     */
    void setPersist(Element e);
    /**
     * Sets the bean name.
     * @param name the bean name
     */
    void setName(String name);
    /**
     * Returns the bean name.
     * @return the bean name
     */
    String getName();
    /**
     * Sets the logger by name.
     * @param name the logger name
     */
    void setLogger(String name);
    /**
     * Sets the logging realm.
     * @param realm the realm string
     */
    void setRealm(String realm);
    /**
     * Returns the logging realm.
     * @return the realm string
     */
    String getRealm();
    /**
     * Returns the logger name.
     * @return the logger name
     */
    String getLogger();
    /**
     * Returns the URLs registered with the class loader.
     * @return array of loader URLs
     */
    URL[] getLoaderURLS();
    /**
     * Returns the QClassLoader for this bean.
     * @return the class loader
     */
    QClassLoader getLoader();
    /**
     * Returns a human-readable dump of this bean's state.
     * @return dump string
     */
    String getDump();
}
