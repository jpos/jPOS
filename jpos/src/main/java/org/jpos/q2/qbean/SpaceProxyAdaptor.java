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

package org.jpos.q2.qbean;

import org.jpos.q2.QBeanSupport;
import org.jpos.space.SpaceProxy;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * Remote Space Proxy Adaptor.
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 * @jmx:mbean description="SpaceProxy QBean"
 *                  extends="org.jpos.q2.QBeanSupportMBean"
 */

public class SpaceProxyAdaptor 
    extends QBeanSupport
    implements SpaceProxyAdaptorMBean
{
    private SpaceProxy sp = null;
    private String spaceName = null;

    public SpaceProxyAdaptor () {
        super ();
    }

    protected void startService () throws RemoteException, NamingException {
        if (spaceName == null) 
            sp = new SpaceProxy ();
        else 
            sp = new SpaceProxy (spaceName);
        InitialContext ctx = new InitialContext ();
        ctx.rebind (getName (), sp);
    }

    protected void stopService () {
        sp.shutdown ();
    }

    /**
     * @jmx:managed-attribute description="Space Name"
     */
    public synchronized void setSpaceName (String spaceName) {
        this.spaceName = spaceName;
        setAttr (getAttrs (), "spaceName", spaceName);
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Space Name"
     */
    public String getSpaceName () {
        return spaceName;
    }
    
    /**
     * @jmx:managed-attribute description="Space Keys"
     */
    public Set getKeys () {
        return sp.getKeySet ();
    }
}

   
