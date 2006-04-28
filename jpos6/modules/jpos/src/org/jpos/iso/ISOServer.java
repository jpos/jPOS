/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.iso;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Stack;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Vector;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;

/**
 * Accept ServerChannel sessions and forwards them to ISORequestListeners
 * @author Alejandro P. Revilla
 * @author Bharavi Gade
 * @version $Revision$ $Date$
 */
public class ISOServer extends Observable 
    implements LogSource, Runnable, Observer, ISOServerMBean, ReConfigurable
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
    protected String realmChannel;
    protected ISOServerSocketFactory socketFactory = null; 
    public static final int CONNECT      = 0;
    public static final int SIZEOF_CNT   = 1;
    private int[] cnt;
    private String[] allow;
    protected Configuration cfg;
    private boolean shutdown = false;
    private ServerSocket serverSocket;
    private Stack channels;

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
        channels = new Stack();
        cnt = new int[SIZEOF_CNT];
    }

    protected Session createSession (ServerChannel channel) {
        return new Session (channel);
    }

    protected class Session implements Runnable, LogSource {
        ServerChannel channel;
        String realm;
        protected Session(ServerChannel channel) {
            this.channel = channel;
            realm = ISOServer.this.getRealm() + ".session";
        }
        public void run() {
            if (channel instanceof BaseChannel) {
                LogEvent ev = new LogEvent (this, "session-start");
                Socket socket = ((BaseChannel)channel).getSocket ();
                realm = realm + socket.getInetAddress();
                try {
                    checkPermission (socket, ev);
                } catch (ISOException e) {
                    try {
                        int delay = 1000 + new Random().nextInt (4000);
                        ev.addMessage (e.getMessage());
                        ev.addMessage ("delay=" + delay);
                        ISOUtil.sleep (delay);
                        socket.close ();
                    } catch (IOException ioe) {
                        ev.addMessage (ioe);
                    }
                    return;
                } finally {
                    Logger.log (ev);
                }
            }
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
                Logger.log (new LogEvent (this, "session-warning", "<eof/>"));
            } catch (SocketException e) {
                if (!shutdown) 
                    Logger.log (new LogEvent (this, "session-warning", e));
            } catch (InterruptedIOException e) {
                // nothing to log
            } catch (Throwable e) { 
                Logger.log (new LogEvent (this, "session-error", e));
            } 
            try {
                channel.disconnect();
            } catch (IOException ex) { 
                Logger.log (new LogEvent (this, "session-error", ex));
            }
            Logger.log (new LogEvent (this, "session-end"));
        }
        public void setLogger (Logger logger, String realm) { 
        }
        public String getRealm () {
            return realm;
        }
        public Logger getLogger() {
            return ISOServer.this.getLogger();
        }
        public void checkPermission (Socket socket, LogEvent evt) 
            throws ISOException 
        {
            if (allow != null && allow.length > 0) {
                String ip = socket.getInetAddress().getHostAddress ();
                for (int i=0; i<allow.length; i++) {
                    if (ip.equals (allow[i])) {
                        evt.addMessage ("access granted, ip=" + ip);
                        return;
                    }
                }
                throw new ISOException ("access denied, ip=" + ip);
            }
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
    /**
     * Shutdown this server
     */
    public void shutdown () {
        shutdown = true;
        new Thread () {
            public void run () {
                shutdownServer ();
                shutdownChannels ();
            }
        }.start();
    }
    private void shutdownServer () {
        try {
            if (serverSocket != null)
                serverSocket.close ();
        } catch (IOException e) {
            Logger.log (new LogEvent (this, "shutdown", e));
        }
    }
    private void shutdownChannels () {
        Iterator iter = channels.iterator();
        while (iter.hasNext()) {
            ISOChannel c = (ISOChannel) ((WeakReference) iter.next()).get ();
            if (c != null) {
                try {
                    c.disconnect ();
                } catch (IOException e) {
                    Logger.log (new LogEvent (this, "shutdown", e));
                }
            }
        }
    }
    private void purgeChannels () {
        Iterator iter = channels.iterator();
        while (iter.hasNext()) {
            WeakReference ref = (WeakReference) iter.next();
            ISOChannel c = (ISOChannel) ref.get ();
            if (c == null || (!c.isConnected()))
                iter.remove ();
        }
    }

    public void run() {
        ServerChannel  channel;
        while  (!shutdown) {
            try {
                serverSocket = socketFactory != null ?
                        socketFactory.createServerSocket(port) :
                        (new ServerSocket (port));
                
                Logger.log (new LogEvent (this, "iso-server", 
                    "listening on port "+port));
                while (!shutdown) {
                    try {
                        channel = (ServerChannel) 
                            clientSideChannelClass.newInstance();
                        channel.setPackager (clientPackager);
                        if (channel instanceof LogSource) {
                            ((LogSource)channel) .
                                setLogger (getLogger(), realmChannel);
                        }
                        if (clientSideChannel instanceof BaseChannel) {
                            ((BaseChannel)channel).setHeader (
                                ((BaseChannel)clientSideChannel).getHeader());
                            ((BaseChannel)channel).setTimeout (
                                ((BaseChannel)clientSideChannel).getTimeout());
                        }
                        setFilters (channel);
                        if (channel instanceof Observable)
                            ((Observable)channel).addObserver (this);
                        for (int i=0; pool.getAvailableCount() <= 0; i++) {
                            ISOUtil.sleep (250);
                            if (i % 240 == 0) {
                                LogEvent evt = new LogEvent (this, "warn");
                                evt.addMessage (
                                    "pool exahusted " + serverSocket.toString()
                                );
                                evt.addMessage (pool);
                                Logger.log (evt);
                            }
                        }
                        channel.accept (serverSocket);
                        if ((cnt[CONNECT]++) % 100 == 0)
                            purgeChannels ();
                        channels.push (new WeakReference (channel));
                        setChanged ();
                        notifyObservers (channel);
                        pool.execute (createSession(channel));
                    } catch (SocketException e) {
                        if (!shutdown)
                            Logger.log (new LogEvent (this, "iso-server", e));
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
                setIncomingFilters (clientIncomingFilters);
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
        this.realmChannel = realm + ".channel";
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
   /**
    * Gets the ISOClientSocketFactory (may be null)
    * @see     ISOClientSocketFactory
    * @since 1.3.3
    */
    public ISOServerSocketFactory getSocketFactory() {
        return socketFactory;
    }
   /**
    * Sets the specified Socket Factory to create sockets
    * @param         socketFactory the ISOClientSocketFactory
    * @see           ISOClientSocketFactory
    * @since 1.3.3
    */
    public void setSocketFactory(ISOServerSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }
    public int getPort () {
        return port;
    }
    public void resetCounters () {
        cnt = new int[SIZEOF_CNT];
    }
    /**
     * @return number of connections accepted by this server
     */
    public int getConnectionCount () {
        return cnt[CONNECT];
    }

    // ThreadPoolMBean implementation (delegate calls to pool)
    public int getJobCount () {
        return pool.getJobCount();
    }
    public int getPoolSize () {
        return pool.getPoolSize();
    }
    public int getMaxPoolSize () {
        return pool.getMaxPoolSize();
    }
    public int getIdleCount() {
        return pool.getIdleCount();
    }
    public int getPendingCount () {
        return pool.getPendingCount();
    }
    /**
     * @return most recently connected ISOChannel or null
     */
    public ISOChannel getLastConnectedISOChannel () {
        if (!channels.empty()) {
            WeakReference ref = (WeakReference) channels.peek ();
            if (ref != null)
                return (ISOChannel) ref.get ();
        } 
        return null;
    }
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        allow = cfg.getAll ("allow");
        if (socketFactory != this && socketFactory instanceof Configurable) {
            ((Configurable)socketFactory).setConfiguration (cfg);
        }
    }
}

