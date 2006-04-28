/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.space;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.rmi.server.RemoteObject;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;

/**
 * RMI Space Proxy 
 * @author Alejandro Revilla
 * @author Niclas Hedhman
 * @version $Revision$ $Date$
 * @since 1.4.9
 */
public class SpaceProxy implements RemoteSpace, ReConfigurable {
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
        } catch (ExportException e) {
            // registry already exists
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
        } catch (Exception e) {
            // nothing to do .. we're shutting down ...
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

