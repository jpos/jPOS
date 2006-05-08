/*
 * Copyright (c) 2005 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.q2.tpl;

import org.jpos.util.NameRegistrar;
import org.jpos.tpl.ConnectionPool;
import org.jpos.q2.QBeanSupport;

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

