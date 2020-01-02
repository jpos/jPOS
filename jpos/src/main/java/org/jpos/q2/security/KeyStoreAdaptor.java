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

import org.jdom2.Element;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.security.SecureKeyStore;
import org.jpos.util.NameRegistrar;

/**
 * KeyStoreAdaptor
 *
 * <b>Sample Configuration</b>
 *
 * <pre>
 * &lt;key-store class="org.jpos.q2.security.KeyStoreAdaptor" logger="Q2"&gt;
 *  &lt;attr name="impl"&gt;org.jpos.security.SimpleKeyFile&lt;/attr&gt;
 *  &lt;property name="key-file" value="deploy/keys" /&gt;
 * &lt;/key-store&gt;
 * </pre>
 *
 * @author Hani Kirollos
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class KeyStoreAdaptor extends QBeanSupport implements KeyStoreAdaptorMBean
{
    public static final String DEFAULT_IMPL="org.jpos.security.SimpleKeyFile";
    String clazz;
    SecureKeyStore ks;
    public KeyStoreAdaptor () {
        super ();
        clazz = DEFAULT_IMPL;
    }
    protected void initService () throws Exception {
        Element e = getPersist ();
        QFactory factory = getServer().getFactory();
        ks = (SecureKeyStore) factory.newInstance (getImpl ());
        factory.setLogger  (ks, e);
        factory.setConfiguration (ks, e);
        NameRegistrar.register (getName (), ks);
    }

    public void setImpl (String clazz) {
        this.clazz = clazz;
    }

    public String getImpl() {
        return clazz;
    }

    protected void destroyService () throws Exception {
        NameRegistrar.unregister (getName ());
    }
}
