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

package org.jpos.iso;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.*;

/**
 * Accept ServerChannel sessions and forwards them to ISORequestListeners
 * @author Alejandro P. Revilla
 * @author Bharavi Gade
 * @version $Revision$ $Date$
 */
public class ISOServer extends Observable 
    implements LogSource, Runnable, Observer, ISOServerMBean, Configurable,
    Loggeable, ISOServerSocketFactory
{
    int port;
    protected ISOChannel clientSideChannel;
    ISOPackager clientPackager;
    protected Collection clientOutgoingFilters, clientIncomingFilters, listeners;
    ThreadPool pool;
    public static final int DEFAULT_MAX_THREADS = 100;
    public static final String LAST = ":last";
    String name;
    protected long lastTxn = 0l;
    protected Logger logger;
    protected String realm;
    protected String realmChannel;
    protected ISOServerSocketFactory socketFactory = null; 
    public static final int CONNECT      = 0;
    public static final int SIZEOF_CNT   = 1;
    private int[] cnt;
    private String[] allow;
    private InetAddress bindAddr;
    private int backlog;
    protected Configuration cfg;
    private boolean shutdown = false;
    private ServerSocket serverSocket;
    private Map channels;
    protected boolean ignoreISOExceptions;

   /**
    * @param port port to listen
    * @param clientSide client side ISOChannel (where we accept connections)
    * @param pool ThreadPool (created if null)
    */
    public ISOServer(int port, ServerChannel clientSide, ThreadPool pool) {
        super();
        this.port = port;
        this.clientSideChannel = clientSide;
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
        channels = new HashMap();
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
            setChanged ();
            notifyObservers ();
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
                    try {
                        ISOMsg m = channel.receive();
                        lastTxn = System.currentTimeMillis();
                        Iterator iter = listeners.iterator();
                        while (iter.hasNext())
                            if (((ISORequestListener)iter.next()).process
                                (channel, m)) 
                                break;
                    } 
                    catch (ISOFilter.VetoException e) {
                        Logger.log (new LogEvent (this, "VetoException", e.getMessage()));
                    }
                    catch (ISOException e) {
                        if (ignoreISOExceptions) {
                            Logger.log (new LogEvent (this, "ISOException", e.getMessage()));
                        } else
                            throw e;
                    }
                }
            } catch (EOFException e) {
                // Logger.log (new LogEvent (this, "session-warning", "<eof/>"));
            } catch (SocketException e) {
                // if (!shutdown) 
                //     Logger.log (new LogEvent (this, "session-warning", e));
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
        new Thread ("ISOServer-shutdown") {
            public void run () {
                shutdownServer ();
                if (!cfg.getBoolean ("keep-channels"))
                    shutdownChannels ();
            }
        }.start();
    }
    private void shutdownServer () {
        try {
            if (serverSocket != null)
                serverSocket.close ();
            if (pool != null)
                pool.close();
        } catch (IOException e) {
            Logger.log (new LogEvent (this, "shutdown", e));
        }
    }
    private void shutdownChannels () {
        Iterator iter = channels.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            WeakReference ref = (WeakReference) entry.getValue();
            ISOChannel c = (ISOChannel) ref.get ();
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
        Iterator iter = channels.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            WeakReference ref = (WeakReference) entry.getValue();
            ISOChannel c = (ISOChannel) ref.get ();
            if (c == null || (!c.isConnected()))
                iter.remove ();
        }
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket ss = new ServerSocket();
        try {
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(bindAddr, port), backlog);
        } catch(SecurityException e) {
            ss.close();
            throw e;
        } catch(IOException e) {
            ss.close();
            throw e;
        }
        return ss;
    }

    public void run() {
        ServerChannel  channel;
        if (socketFactory == null)
            socketFactory = this;
        serverLoop : while  (!shutdown) {
            try {
                serverSocket = socketFactory.createServerSocket(port);
                
                Logger.log (new LogEvent (this, "iso-server", 
                    "listening on " + (bindAddr != null ? bindAddr + ":" : "port ") + port
                    + (backlog > 0 ? " backlog="+backlog : "")
                ));
                while (!shutdown) {
                    try {
                        if (pool.getAvailableCount() <= 0) {
                            try {
                                serverSocket.close();
                            } catch (IOException e){
                                Logger.log (new LogEvent (this, "iso-server", e));
                                relax();
                            }
                            
                            for (int i=0; pool.getAvailableCount() <= 0; i++) {
                                ISOUtil.sleep (250);
                                if (shutdown) break serverLoop;
                                if (i % 240 == 0 && cfg.getBoolean("pool-exhaustion-warning", true)) {
                                    LogEvent evt = new LogEvent (this, "warn");
                                    evt.addMessage (
                                        "pool exhausted " + serverSocket.toString()
                                    );
                                    evt.addMessage (pool);
                                    Logger.log (evt);
                                }
                            }

                            serverSocket = socketFactory.createServerSocket(port);
                        }
                        channel = (ServerChannel) clientSideChannel.clone();
                        channel.accept (serverSocket);
                        if ((cnt[CONNECT]++) % 100 == 0)
                            purgeChannels ();
                        WeakReference wr = new WeakReference (channel);
                        channels.put (channel.getName(), wr);
                        channels.put (LAST, wr);
                        pool.execute (createSession(channel));
                        setChanged ();
                        notifyObservers (this);
                        if (channel instanceof Observable)  
                            ((Observable)channel).addObserver (this);
                    } catch (SocketException e) {
                        if (!shutdown)
                            Logger.log (new LogEvent (this, "iso-server", e));
                    } catch (IOException e) {
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
     * @return ISOServer instance with given name.
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
        lastTxn = 0l;
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
    public int getActiveConnections () {
        return pool.getActiveCount();
    }
    /**
     * @return most recently connected ISOChannel or null
     */
    public ISOChannel getLastConnectedISOChannel () {
        return getISOChannel (LAST);
    }
    /**
     * @return ISOChannel under the given name
     */
    public ISOChannel getISOChannel (String name) {
        WeakReference ref = (WeakReference) channels.get (name);
        if (ref != null)
            return (ISOChannel) ref.get ();
        return null;
    }
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        allow = cfg.getAll ("allow");
        backlog = cfg.getInt ("backlog", 0);
        ignoreISOExceptions = cfg.getBoolean("ignore-iso-exceptions");
        String ip = cfg.get ("bind-address", null);
        if (ip != null) {
            try {
                bindAddr = InetAddress.getByName (ip);
            } catch (UnknownHostException e) {
                throw new ConfigurationException ("Invalid bind-address " + ip, e);
            }
        }
        if (socketFactory == null)
            socketFactory = this;
        if (socketFactory != this && socketFactory instanceof Configurable) {
            ((Configurable)socketFactory).setConfiguration (cfg);
        }
    }
    public String getISOChannelNames () {
        StringBuffer sb = new StringBuffer ();
        Iterator iter = channels.entrySet().iterator();
        for (int i=0; iter.hasNext(); i++) {
            Map.Entry entry = (Map.Entry) iter.next();
            WeakReference ref = (WeakReference) entry.getValue();
            ISOChannel c = (ISOChannel) ref.get ();
            if (c != null && !LAST.equals (entry.getKey()) && c.isConnected()) {
                if (i > 0)
                    sb.append (' ');
                sb.append (entry.getKey());
            }
        }
        return sb.toString();
    }
    public String getCountersAsString () {
        StringBuffer sb = new StringBuffer ();
        int cnt[] = getCounters();
        sb.append ("connected=");
        sb.append (Integer.toString(cnt[0]));
        sb.append (", rx=");
        sb.append (Integer.toString(cnt[0]));
        sb.append (", tx=");
        sb.append (Integer.toString(cnt[1]));
        sb.append (", last=");
        sb.append (lastTxn);
        if (lastTxn > 0) {
            sb.append (", idle=");
            sb.append(System.currentTimeMillis() - lastTxn);
            sb.append ("ms");
        }
        return sb.toString();
    }
    
    public int[] getCounters()
    {
        Iterator iter = channels.entrySet().iterator();
        int[] cnt = new int[3];
        cnt[2] = 0;
        for (int i=0; iter.hasNext(); i++) {
            Map.Entry entry = (Map.Entry) iter.next();
            WeakReference ref = (WeakReference) entry.getValue();
            ISOChannel c = (ISOChannel) ref.get ();
            if (c != null && !LAST.equals (entry.getKey()) && c.isConnected()) {
                cnt[2]++;
                if (c instanceof BaseChannel) {
                    int[] cc = ((BaseChannel)c).getCounters();
                    cnt[0] += cc[ISOChannel.RX];
                    cnt[1] += cc[ISOChannel.TX];
                }
            }
        }
        return cnt;
    }
    public int getTXCounter() {
        int cnt[] = getCounters();
        return cnt[1];
    }
    public int getRXCounter() {
        int cnt[] = getCounters();
        return cnt[0];
    }
    public int getConnections () {
        int cnt[] = getCounters();
        return cnt[2];
    }
    public long getLastTxnTimestampInMillis() {
        return lastTxn;
    }
    public long getIdleTimeInMillis() {
        return lastTxn > 0L ? System.currentTimeMillis() - lastTxn : -1L;
    }

        
    public String getCountersAsString (String isoChannelName) {
        ISOChannel channel = getISOChannel(isoChannelName);
        StringBuffer sb = new StringBuffer();
        if (channel instanceof BaseChannel) {
            int[] counters = ((BaseChannel)channel).getCounters();
            append (sb, "rx=", counters[ISOChannel.RX]);
            append (sb, ", tx=", counters[ISOChannel.TX]);
            append (sb, ", connects=", counters[ISOChannel.CONNECT]);
        }
        return sb.toString();
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + getCountersAsString());
        Iterator iter = channels.entrySet().iterator();
        String inner = indent + "  ";
        for (int i=0; iter.hasNext(); i++) {
            Map.Entry entry = (Map.Entry) iter.next();
            WeakReference ref = (WeakReference) entry.getValue();
            ISOChannel c = (ISOChannel) ref.get ();
            if (c != null && !LAST.equals (entry.getKey()) && c.isConnected()) {
                if (c instanceof BaseChannel) {
                    StringBuffer sb = new StringBuffer ();
                    int[] cc = ((BaseChannel)c).getCounters();
                    sb.append (inner);
                    sb.append (entry.getKey());
                    sb.append (": rx=");
                    sb.append (Integer.toString (cc[ISOChannel.RX]));
                    sb.append (", tx=");
                    sb.append (Integer.toString (cc[ISOChannel.TX]));
                    sb.append (", last=");
                    sb.append (Long.toString(lastTxn));
                    p.println (sb.toString());
                }
            }
        }
    }
    private void append (StringBuffer sb, String name, int value) {
        sb.append (name);
        sb.append (value);
    }
}

