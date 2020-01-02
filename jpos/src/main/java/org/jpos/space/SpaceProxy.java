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

package org.jpos.space;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Configurable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.Set;

/**
 * RMI Space Proxy 
 * @author Alejandro Revilla
 * @author Niclas Hedhman
 * @version $Revision$ $Date$
 * @since 1.4.9
 */
@SuppressWarnings("unchecked")
public class SpaceProxy implements RemoteSpace, Configurable {
    Space sp;
    Configuration cfg;
    private RemoteRef ref;
    private RemoteStub stub;
    public SpaceProxy () throws RemoteException {
        super();
        sp = SpaceFactory.getSpace ();
        startService ();
    }
    public SpaceProxy (String spaceUri) throws RemoteException {
        super ();
        sp = SpaceFactory.getSpace (spaceUri);
        startService ();
    }

    private void startService () throws RemoteException 
    {
        try {
            LocateRegistry.createRegistry (Registry.REGISTRY_PORT);
        } catch (ExportException ignored) {
            // NOPMD: ok to happen
        }
        stub = UnicastRemoteObject.exportObject (this);
        ref  = stub.getRef();
    }

    public void out (Serializable key, Serializable value) 
        throws RemoteException
    {
        sp.out (key, value);
    }
    public void out (Serializable key, Serializable value, long timeout)
        throws RemoteException
    {
        sp.out (key, value, timeout);
    }
    public Serializable in (Serializable key)
        throws RemoteException
    {
        return (Serializable) sp.in (key);
    }
    public Serializable rd  (Serializable key)
        throws RemoteException
    {
        return (Serializable) sp.rd (key);
    }
    public Serializable in  (Serializable key, long timeout)
        throws RemoteException
    {
        return (Serializable) sp.in (key, timeout);
    }
    public Serializable rd  (Serializable key, long timeout)
        throws RemoteException
    {
        return (Serializable) sp.rd (key, timeout);
    }
    public Serializable inp (Serializable key)
        throws RemoteException
    {
        return (Serializable) sp.inp (key);
    }
    public Serializable rdp (Serializable key)
        throws RemoteException
    {
        return (Serializable) sp.rdp (key);
    }
    public void shutdown() {
        try {
            if (UnicastRemoteObject.unexportObject (this, false)) 
                return;
            Thread.sleep (5000);
            UnicastRemoteObject.unexportObject (this, true);
        } catch (Exception ignored) {
            // NOPMD: nothing to do .. we're shutting down ...
        }
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        try {
            InitialContext ctx = new InitialContext ();
            ctx.rebind (cfg.get ("name"), stub);
        } catch (NamingException e) {
            throw new ConfigurationException (e);
        }
    }
    public Set getKeySet () {
        if (sp instanceof LocalSpace) 
            return ((LocalSpace)sp).getKeySet ();
        else
            return null;
    }
    public String toString() {
        if (ref == null)
            return getClass().getName() + "[<unexported>]";
        else
            return getClass().getName() + "[" + ref.remoteToString() + "]";
    }
    public int hashCode() {
        if (ref == null )
            return super.hashCode();
        else
            return ref.remoteHashCode();
    }
    public boolean equals (Object obj) {
        if (obj instanceof RemoteObject) {
            if (ref == null) {
                return obj == this;
            } else {
                RemoteRef otherRef = ((RemoteObject)obj).getRef();
                return ref.remoteEquals (otherRef);
            }
        } else if (obj != null) {
            return obj.equals (this);
        } else {
            return false;
        }
    }
}

