/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
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

import mx4j.tools.naming.NamingService;

import org.jdom.Element;
import org.jpos.q2.QBeanSupport;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 * @jmx:mbean description="Naming Service" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class Naming
            extends QBeanSupport implements NamingMBean
{
    NamingService naming;

    private int port = 1099;

    public Naming () {
        super();
        naming = new NamingService();
    }

    /**
     * @jmx:managed-operation description="QBean init"
     */
    public void init () {
        log.info ("init");
        super.init ();
    }
    /**
     * @jmx:managed-operation description="QBean start"
     */
    public void start() {
        log.info ("start");
        super.start ();
    }
    /**
     * @jmx:managed-operation description="QBean stop"
     */
    public void stop () {
        log.info ("stop");
        super.stop ();
    }
    /**
     * @jmx:managed-operation description="QBean destroy"
     */
    public void destroy () {
        log.info ("destroy");
        log = null;
    }

    public Element getPersist () {
        setModified (false);
        log.info ("getPersist");
        return createElement ("naming", NamingMBean.class);
    }

    /**
     * @jmx:managed-attribute description="set port"
     */
    public void setPort (int port) {
        this.port = port;
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="get port"
     */
    public int getPort () {
        return port;
    }

    public void startService()
            throws Exception
    {
        log.info("start listening on port:" + port);
        naming.setPort(port);
        naming.start();
    }


    public void stopService()
            throws Exception
    {
        naming.stop();
    }
}
