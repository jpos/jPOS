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

package org.jpos.iso;

import java.io.IOException;
import java.io.EOFException;
import java.util.Vector;
import java.util.Iterator;
import java.util.Observer;
import java.util.Observable;
import java.util.Collection;
import java.net.ServerSocket;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

/**
 * Accept ServerChannel sessions and forwards them to ISORequestListeners
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ISOServer extends Observable 
    implements LogSource, Runnable, Observer
{
    int port;
    ISOChannel clientSideChannel;
    Class clientSideChannelClass;
    ISOPackager clientPackager;
    Collection clientOutgoingFilters, clientIncomingFilters, listeners;
    ThreadPool pool;
    public static final int DEFAULT_MAX_THREADS = 100;
    String name;
    protected Logger logger;
    protected String realm;

   /**
    * @param port port to listen
    * @param clientSide client side ISOChannel (where we accept connections)
    * @param pool ThreadPool (created if null)
    */
    public ISOServer(int port, ServerChannel clientSide, ThreadPool pool) {
	super();
	this.port = port;
	this.clientSideChannel = clientSide;
	this.clientSideChannelClass = clientSide.getClass();
	this.clientPackager = clientSide.getPackager();
	if (clientSide instanceof FilteredChannel) {
	    FilteredChannel fc = (FilteredChannel) clientSide;
	    this.clientOutgoingFilters = fc.getOutgoingFilters();
	    this.clientIncomingFilters = fc.getIncomingFilters();
	}
	this.pool = (pool == null) ?  
	    new ThreadPool (1, DEFAULT_MAX_THREADS) : pool;
	listeners = new Vector();
	name = "";
    }

    protected class Session implements Runnable, LogSource {
        ServerChannel channel;
        protected Session(ServerChannel channel) {
            this.channel = channel;
        }
        public void run() {
	    Logger.log (new LogEvent (this, "SessionStart"));
            try {
                for (;;) {
                    ISOMsg m = channel.receive();
		    Iterator iter = listeners.iterator();
		    while (iter.hasNext())
			if (((ISORequestListener)iter.next()).process
			    (channel, m)) 
			    break;
                }
	    } catch (EOFException e) {
            } catch (Exception e) { 
		Logger.log (new LogEvent (this, "SessionError", e));
            } 
	    Logger.log (new LogEvent (this, "SessionEnd"));
        }
	public void setLogger (Logger logger, String realm) { 
	}
	public String getRealm () {
	    return ISOServer.this.getRealm() + ".session";
	}
	public Logger getLogger() {
	    return ISOServer.this.getLogger();
	}
    }
   /**
    * add an ISORequestListener
    * @param l request listener to be added
    * @see ISORequestListener
    */
    public void addISORequestListener(ISORequestListener l) {
	listeners.add (l);
    }
   /**
    * remove an ISORequestListener
    * @param l a request listener to be removed
    * @see ISORequestListener
    */
    public void removeISORequestListener(ISORequestListener l) {
	listeners.remove (l);
    }

    public void run() {
        ServerChannel  channel;
	for (;;) {
	    try {
		ServerSocket serverSocket = new ServerSocket(port);
		Logger.log (new LogEvent (this, "iso-server", 
		    "listening on port "+port));
		for (;;) {
		    try {
			channel = (ServerChannel) 
			    clientSideChannelClass.newInstance();
			channel.setPackager (clientPackager);
			if (clientSideChannel instanceof BaseChannel)
			    ((BaseChannel)channel).setHeader (
				((BaseChannel)clientSideChannel).getHeader());
			setFilters (channel);
			if (channel instanceof LogSource) {
			    ((LogSource)channel) .
				setLogger (getLogger(), getRealm()+".channel");
			}
			if (channel instanceof Observable)
			    ((Observable)channel).addObserver (this);
			channel.accept (serverSocket);
			pool.execute (new Session(channel));
		    } catch (IOException e) {
			Logger.log (new LogEvent (this, "iso-server", e));
			relax();
		    } catch (InstantiationException e) {
			Logger.log (new LogEvent (this, "iso-server", e));
			relax();
		    } catch (IllegalAccessException e) {
			Logger.log (new LogEvent (this, "iso-server", e));
			relax();
		    }
		}
	    } catch (Throwable e) {
		Logger.log (new LogEvent (this, "iso-server", e));
		relax();
	    }
	}
    }

    private void setFilters (ISOChannel channel) {
	if (clientOutgoingFilters != null)
	    ((FilteredChannel)channel) .
		setOutgoingFilters (clientOutgoingFilters);
	if (clientIncomingFilters != null)
	    ((FilteredChannel)channel) .
		setIncomingFilters (clientOutgoingFilters);
    }
    private void relax() {
	try {
	    Thread.sleep (5000);
	} catch (InterruptedException e) { }
    }

    /**
     * associates this ISOServer with a name using NameRegistrar
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName (String name) {
	this.name = name;
	NameRegistrar.register ("server."+name, this);
    }
    /**
     * @return ISOMUX instance with given name.
     * @throws NameRegistrar.NotFoundException;
     * @see NameRegistrar
     */
    public static ISOServer getServer (String name)
	throws NameRegistrar.NotFoundException
    {
	return (ISOServer) NameRegistrar.get ("server."+name);
    }
    /**
     * @return this ISOServer's name ("" if no name was set)
     */
    public String getName() {
	return this.name;
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
    public void update(Observable o, Object arg) {
	setChanged ();
	notifyObservers (arg);
    }
}

