/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.space;

import java.util.Set;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.registry.LocateRegistry;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jpos.util.SimpleLogSource;
import org.jpos.core.ReConfigurable;
import org.jpos.core.Configuration;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;

/**
 * RMI Space Proxy 
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class SpaceProxy extends SimpleLogSource 
    implements RemoteSpace, ReConfigurable 
{
    LocalSpace sp;
    Configuration cfg;
    public SpaceProxy () throws RemoteException {
        super();
        sp = TransientSpace.getSpace ();
        startService ();
    }
    public SpaceProxy (String spaceName) throws RemoteException {
        super ();
        sp = TransientSpace.getSpace (spaceName);
        startService ();
    }

    private void startService () throws RemoteException 
    {
        try {
            LocateRegistry.createRegistry (Registry.REGISTRY_PORT);
        } catch (ExportException e) {
            // registry already exists
        }
        UnicastRemoteObject.exportObject (this);
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
    {
        this.cfg = cfg;
        try {
            InitialContext ctx = new InitialContext ();
            ctx.rebind (cfg.get ("name"), this);
        } catch (NamingException e) {
            Logger.log (new LogEvent (this, "configuration", e));
        }
    }
    public Set getKeySet () {
        return sp.getKeySet ();
    }
}

