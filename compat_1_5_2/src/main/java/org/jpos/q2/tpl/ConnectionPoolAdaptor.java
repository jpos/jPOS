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

package org.jpos.q2.tpl;

import org.jpos.q2.QBeanSupport;
import org.jpos.tpl.ConnectionPool;
import org.jpos.util.NameRegistrar;

/**
 * ConnectionPoolAdaptor
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 *
 * <pre>
 * &lt;connection-pool name='jposdb'
 *   class='org.jpos.q2.tpl.ConnectionPoolAdaptor' logger="Q2"&gt;
 * 
 *  &lt;property name="jdbc.driver"         value="com.mysql.jdbc.Driver" /&gt;
 *  &lt;property name="jdbc.url"
 *           value="jdbc:mysql:///jpos?autoReconnect=true" /&gt;
 *  &lt;property name="jdbc.user"           value="myuser" /&gt;
 *  &lt;property name="jdbc.password"       value="mypass" /&gt;
 * 
 *  &lt;property name="initial-connections" value="1" /&gt;
 *  &lt;property name="max-connections"     value="10" /&gt;
 *  &lt;property name="wait-if-busy"        value="yes" /&gt;
 * &lt;/connection-pool&gt;
 * </pre>
 */
public class ConnectionPoolAdaptor extends QBeanSupport {
    String poolName;
    ConnectionPool pool;

    public ConnectionPoolAdaptor () {
        super ();
    }
    protected void initService () throws Exception {
        pool = new ConnectionPool ();
        pool.setConfiguration (getConfiguration());
        poolName = "connection.pool." + getName();
        NameRegistrar.register (poolName, pool);
    }
    protected void stopService () throws Exception {
        NameRegistrar.unregister (poolName);
    }
    protected void destroyService () throws Exception {
        pool.closeAllConnections();
    }
}

