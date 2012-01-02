/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jdom.Element;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.security.SMAdapter;
import org.jpos.util.NameRegistrar;

/**
 * SMAdaptor Adaptor
 *
 * <b>Sample Configuration</b>
 * <pre>
 * &lt;s-m-adaptor class="org.jpos.q2.security.SMAdaptor" logger="Q2"&gt;
 *  &lt;attr name="impl"&gt;org.jpos.security.jceadapter.JCESecurityModule&lt;/attr&gt;
 * 
 *  &lt;property name="provider" value="com.sun.crypto.provider.SunJCE" /&gt;
 * &lt;property name="lmk" value="path/to/your/lmk" /&gt;
 * &lt;/s-m-adaptor&gt;
 * </pre>
 * 
 * @author Hani Kirollos
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @jmx:mbean description="SMAdaptor QBean"
 *                extends="org.jpos.q2.QBeanSupportMBean"
 */
public class SMAdaptor extends QBeanSupport implements SMAdaptorMBean
{
    String clazz;
    SMAdapter sm;
    public SMAdaptor () {
        super ();
    }
    protected void initService () throws Exception {
        Element e = getPersist ();
        QFactory factory = getServer().getFactory();
        sm = (SMAdapter) factory.newInstance (getImpl ());
        factory.setLogger  (sm, e);
        factory.setConfiguration (sm, e);
    }

    /**
     * @jmx:managed-attribute description="Implementation class name"
     */
    public void setImpl (String clazz) {
        this.clazz = clazz;
    }
    /**
     * @jmx:managed-attribute description="Implementation class name"
     */
    public String getImpl() {
        return clazz;
    }
    protected void startService () throws Exception {
        NameRegistrar.register (getName (), sm);
    }
    protected void stopService () throws Exception {
        NameRegistrar.unregister (getName ());
    }
}

