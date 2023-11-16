/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.jfr.ChannelEvent;
import org.jpos.util.*;

import static org.jpos.jfr.ChannelEvent.*;

/**
 * Accept ServerChannel sessions and forwards them to ISORequestListeners
 * @author Alejandro P. Revilla
 * @author Bharavi Gade
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
public class ISOServer extends Observable
    implements LogSource, Runnable, Observer, ISOServerMBean, Configurable,
    Loggeable, ISOServerSocketFactory
{
    private enum PermLogPolicy {
        ALLOW_NOLOG, DENY_LOG, ALLOW_LOG, DENY_LOGWARNING
    }

    int port;
    private InetAddress bindAddr;

    private Map<String,Boolean> specificIPPerms= new HashMap<>();   // TRUE means allow; FALSE means deny
    private List<String> wildcardAllow;
    private List<String> wildcardDeny;
    private PermLogPolicy ipPermLogPolicy= PermLogPolicy.ALLOW_NOLOG;

    protected ISOChannel clientSideChannel;
    ISOPackager clientPackager;
    protected Collection clientOutgoingFilters, clientIncomingFilters;
    protected List<ISORequestListener> listeners;
    public static final int DEFAULT_MAX_SESSIONS = 100;
    public static final String LAST = ":last";
    String name;
    protected long lastTxn = 0l;
    protected Logger logger;
    protected String realm;
    protected String realmChannel;
    protected ISOServerSocketFactory socketFactory = null;

    private AtomicInteger connectionCount = new AtomicInteger();

    private int backlog;
    protected Configuration cfg;
    private volatile boolean shutdown = false;
    private ServerSocket serverSocket;
    private Map<String,WeakReference<ISOChannel>> channels;
    protected boolean ignoreISOExceptions;
    protected List<ISOServerEventListener> serverListeners = null;
    private ExecutorService executor;
    private Semaphore permits;
    private int permitsCount = DEFAULT_MAX_SESSIONS;
    private static final long SMALL_RELAX = 250;
    private static final long LONG_RELAX = 5000;

   /**
    * @param port port to listen
    * @param clientSide client side ISOChannel (where we accept connections)
    */
    public ISOServer(int port, ServerChannel clientSide, int maxSessions) {
        super();
        this.port = port;
        this.clientSideChannel = clientSide;
        this.clientPackager = clientSide.getPackager();
        if (clientSide instanceof FilteredChannel) {
            FilteredChannel fc = (FilteredChannel) clientSide;
            this.clientOutgoingFilters = fc.getOutgoingFilters();
            this.clientIncomingFilters = fc.getIncomingFilters();
        }
        listeners = new ArrayList<>();
        name = "";
        channels = Collections.synchronizedMap(new HashMap<>());
        serverListeners = Collections.synchronizedList(new ArrayList<>());

        executor = Executors.newVirtualThreadPerTaskExecutor();
        if (maxSessions > 0)
            permitsCount = maxSessions;
        permits = new Semaphore(permitsCount);
    }

    @Override
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        configureConnectionPerms();
        backlog = cfg.getInt ("backlog", 5);
        ignoreISOExceptions = cfg.getBoolean("ignore-iso-exceptions");
        String ip = cfg.get ("bind-address", null);
        if (ip != null) {
            try {
                bindAddr = InetAddress.getByName (ip);
            } catch (UnknownHostException e) {
                throw new ConfigurationException ("Invalid bind-address " + ip, e);
            }
        }
        if (socketFactory == null) {
            socketFactory = this;
        }
        if (socketFactory != this && socketFactory instanceof Configurable) {
            ((Configurable)socketFactory).setConfiguration (cfg);
        }
    }

    // Helper method to setConfiguration. Handles "allow" and "deny" params
    private void configureConnectionPerms() throws ConfigurationException
    {
        boolean hasAllows= false, hasDenies= false;

        String[] allows= cfg.getAll ("allow");
        if (allows != null && allows.length > 0) {
            hasAllows= true;

            for (String allowIP : allows) {
                allowIP= allowIP.trim();

                if (allowIP.indexOf('*') == -1) {                   // specific IP with no wildcards
                    specificIPPerms.put(allowIP, true);
                } else {                                            // there's a wildcard
                    wildcardAllow= (wildcardAllow == null) ? new ArrayList<>() : wildcardAllow;
                    String[] parts= allowIP.split("[*]");
                    wildcardAllow.add(parts[0]);                    // keep only the first part
                }
            }
        }

        String[] denies= cfg.getAll ("deny");
        if (denies != null && denies.length > 0) {
            hasDenies= true;

            for (String denyIP : denies) {
                boolean conflict= false;                            // used for a little sanity check

                denyIP= denyIP.trim();
                if (denyIP.indexOf('*') == -1) {                    // specific IP with no wildcards
                    Boolean oldVal= specificIPPerms.put(denyIP, false);
                    conflict= (oldVal == Boolean.TRUE);
                } else {                                            // there's a wildcard
                    wildcardDeny= (wildcardDeny == null) ? new ArrayList<>() : wildcardDeny;
                    String[] parts= denyIP.split("[*]");
                    if (wildcardAllow != null && wildcardAllow.contains(parts[0]))
                        conflict= true;
                    else
                        wildcardDeny.add(parts[0]);                 // keep only the first part
                }

                if (conflict) {
                    throw new ConfigurationException(
                            "Conflicting IP permission in '"+getName()+"' configuration: 'deny' "
                                    +denyIP+" while having an identical previous 'allow'.");
                }
            }
        }

        // sum up permission policy and logging type
        ipPermLogPolicy= (!hasAllows && !hasDenies) ? PermLogPolicy.ALLOW_NOLOG :           // default when no permissions specified
                         ( hasAllows && !hasDenies) ? PermLogPolicy.DENY_LOG :
                         (!hasAllows && hasDenies)  ? PermLogPolicy.ALLOW_LOG :
                                                      PermLogPolicy.DENY_LOGWARNING;        // mixed allows & denies, if nothing matches we'll DENY and log a warning
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
        executor.submit(() -> {
            Thread.currentThread().setName("ISOServer-shutdown");
            shutdownServer();
            if (!cfg.getBoolean("keep-channels")) {
                shutdownChannels();
            }
        });
    }
    private void shutdownServer () {
        try {
            if (serverSocket != null) {
                serverSocket.close ();
                fireEvent(new ISOServerShutdownEvent(this));
            }
            executor.awaitTermination(LONG_RELAX, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            fireEvent(new ISOServerShutdownEvent(this));
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
                    fireEvent(new ISOServerClientDisconnectEvent(this, c));
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
            if (c == null || !c.isConnected()) {
                iter.remove ();
            }
        }
    }


    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket ss = new ServerSocket();
        try {
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(bindAddr, port), backlog);
        } catch(SecurityException e) {
            ss.close();
            fireEvent(new ISOServerShutdownEvent(this));
            throw e;
        } catch(IOException e) {
            ss.close();
            fireEvent(new ISOServerShutdownEvent(this));
            throw e;
        }
        return ss;
    }

    //-----------------------------------------------------------------------------
    // -- Helper Session inner class. It's a Runnable, running in its own
    // -- thread and handling a connection to this ISOServer
    // --
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
        @Override
        public void run() {
            setChanged ();
            notifyObservers ();
            if (channel instanceof BaseChannel baseChannel) {
                LogEvent ev = new LogEvent (this, "session-start", connectionCount.get() + "/" + permitsCount);
                Socket socket = baseChannel.getSocket ();
                if (!checkPermission (socket, ev))
                    return;
                realm = realm + "/" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                if (clientSideChannel instanceof BaseChannel bc)
                    baseChannel.setCounters(bc.getMsgInCounter(), bc.getMsgOutCounter());
            }
            try {
                while (true) try {
                    ISOMsg m = channel.receive();
                    lastTxn = System.currentTimeMillis();
                    for (Object listener : listeners) {
                        if (((ISORequestListener) listener).process(channel, m)) {
                            break;
                        }
                    }
                } catch (ISOFilter.VetoException e) {
                    Logger.log(new LogEvent(this, "VetoException", e.getMessage()));
                } catch (ISOException e) {
                    if (ignoreISOExceptions) {
                        Logger.log(new LogEvent(this, "ISOException", e.getMessage()));
                    } else {
                        throw e;
                    }
                }
            } catch (EOFException e) {
                 // Logger.log (new LogEvent (this, "session-warning", "<eof/>"));
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
                fireEvent(new ISOServerClientDisconnectEvent(ISOServer.this, channel));
            } catch (IOException ex) {
                Logger.log (new LogEvent (this, "session-error", ex));
                fireEvent(new ISOServerClientDisconnectEvent(ISOServer.this, channel));
            }
            Logger.log (new LogEvent (this, "session-end ", connectionCount.decrementAndGet() + "/" + permitsCount));
        }
        @Override
        public void setLogger (Logger logger, String realm) {
        }
        @Override
        public String getRealm () {
            return realm;
        }
        @Override
        public Logger getLogger() {
            return ISOServer.this.getLogger();
        }

        private boolean checkPermission (Socket socket, LogEvent ev) {
            try {
                checkPermission0 (socket, ev);
                return true;
            } catch (ISOException e) {
                try {
                    int delay = 1000 + new Random().nextInt(4000);
                    ev.addMessage(e.getMessage());
                    ev.addMessage("delay=" + delay);
                    ISOUtil.sleep(delay);
                    socket.close();
                    fireEvent(new ISOServerShutdownEvent(ISOServer.this));
                } catch (Throwable t) {
                    ev.addMessage (t);
                }
            } finally {
                Logger.log (ev);
            }
            return false;
        }

        private void checkPermission0 (Socket socket, LogEvent evt) throws ISOException {
            // if there are no allow/deny params, just return without doing any checks
            // (i.e.: "silent allow policy", keeping backward compatibility)
            if (specificIPPerms.isEmpty() && wildcardAllow == null && wildcardDeny == null)
                return;

            String ip= socket.getInetAddress().getHostAddress ();           // The remote IP

            // first, check allows or denies for specific/whole IPs (no wildcards)
            boolean specificAllow = specificIPPerms.get(ip);
            if (specificAllow == Boolean.TRUE) {                            // specific IP allow
                evt.addMessage("access granted, ip=" + ip);
                return;
            } else if (specificAllow == Boolean.FALSE) {                    // specific IP deny
                throw new ISOException("access denied, ip=" + ip);
            } else {                                                        // no specific match under the specificIPPerms Map
                // We check the wildcard lists, deny first
                if (wildcardDeny != null) {
                    for (String wdeny : wildcardDeny) {
                        if (ip.startsWith(wdeny)) {
                            throw new ISOException ("access denied, ip=" + ip);
                        }
                    }
                }
                if (wildcardAllow != null) {
                    for (String wallow : wildcardAllow) {
                        if (ip.startsWith(wallow)) {
                            evt.addMessage("access granted, ip=" + ip);
                            return;
                        }
                    }
                }

                // Reaching this point means that nothing matched our specific or wildcard rules, so we fall
                // back on the default permission policies and log type
                switch (ipPermLogPolicy) {
                    case DENY_LOG:        // only allows were specified, default policy is to deny non-matches and log the issue
                        throw new ISOException ("access denied, ip=" + ip);
                        // break;

                    case ALLOW_LOG:       // only denies were specified, default policy is to allow non-matches and log the issue
                        evt.addMessage("access granted, ip=" + ip);
                        break;

                    case DENY_LOGWARNING: // mix of allows and denies were specified, but the IP matched no rules!
                                          // so we adopt a deny policy but give a special warning
                        throw new ISOException ("access denied, ip=" + ip + " (WARNING: the IP did not match any rules!)");
                        // break;

                    case ALLOW_NOLOG:   // this is the default case when no allow/deny are specified
                                        // the method will abort early on the first "if", so this is here just for completion
                        break;
                }

            }
            // we should never reach this point!! :-)
        }
    } // inner class Session

    //-------------------------------------------------------------------------------
    //-- This is the main run for this ISOServer's Thread
    @Override
    public void run() {
        // ServerChannel  channel;
        if (socketFactory == null) {
            socketFactory = this;
        }
        int round = 0;
        serverLoop : while  (!shutdown) {
            round++;
            try {
                if (permits.availablePermits() <= 0) {
                    LockSupport.parkNanos(Duration.ofMillis(SMALL_RELAX).toNanos());
                    if (round % 240 == 0 && cfg.getBoolean("permits-exhaustion-warning", true)) {
                        log ("warn", "permits exhausted " + serverSocket.toString());
                    }
                    continue;
                }
                serverSocket = socketFactory.createServerSocket(port);
                log ("iso-server",
                    "listening on " + (bindAddr != null ? bindAddr + ":" : "port ") + port
                    + ", permits=" + permits.availablePermits()
                    + (backlog > 0 ? ", backlog="+backlog : "")
                );
                while (!shutdown) {
                    try {
                        if (permits.availablePermits() <= 0) {
                            ChannelEvent jfr = new ChannelEvent.AcceptException(
                              "Available permits too low (%d)".formatted(permits.availablePermits())
                            );
                            jfr.begin();
                            try {
                                serverSocket.close();
                                fireEvent(new ISOServerShutdownEvent(this));
                            } catch (IOException e){
                                log ("iso-server", Caller.info() + ":" + e.getMessage());
                            } finally {
                                jfr.commit();
                            }
                            continue serverLoop;
                        }
                        final ServerChannel channel = (ServerChannel) clientSideChannel.clone();
                        channel.accept (serverSocket);

                        if (connectionCount.getAndIncrement() % 100 == 0) {
                            purgeChannels ();
                        }
                        WeakReference<ISOChannel> wr = new WeakReference<> (channel);
                        channels.put (channel.getName(), wr);
                        channels.put (LAST, wr);
                        executor.submit (() -> {
                            try {
                                permits.acquireUninterruptibly();
                                createSession(channel).run();
                            } finally {
                                permits.release();
                            }
                          });
                        setChanged ();
                        notifyObservers (this);
                        fireEvent(new ISOServerAcceptEvent(this, channel));
                        if (channel instanceof Observable) {
                            ((Observable)channel).addObserver (this);
                        }
                    } catch (SocketException e) {
                        if (!shutdown) {
                            Logger.log (new LogEvent (this, "iso-server", e));
                            relax();
                            continue serverLoop;
                        }
                    } catch (IOException e) {
                        Logger.log (new LogEvent (this, "iso-server", e));
                        relax();
                    }
                } // while !shutdown
            } catch (Throwable e) {
                Logger.log (new LogEvent (this, "iso-server", e));
                relax();
            }
        }
    } // ISOServer's run()
    //-------------------------------------------------------------------------------

    private void relax() {
        LockSupport.parkNanos(Duration.ofMillis(LONG_RELAX).toNanos());
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
    @Override
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
        this.realmChannel = realm + ".channel";
    }
    @Override
    public String getRealm () {
        return realm;
    }
    @Override
    public Logger getLogger() {
        return logger;
    }
    @Override
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
    @Override
    public int getPort () {
        return port;
    }
    @Override
    public void resetCounters () {
        connectionCount.set(0);
        lastTxn = 0l;
    }

    /**
     * @return number of connections accepted by this server
     */
    @Override
    public int getConnectionCount () {
        return connectionCount.get();
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
        if (ref != null) {
            return (ISOChannel) ref.get ();
        }
        return null;
    }


    @Override
    public String getISOChannelNames () {
        StringBuilder sb = new StringBuilder ();
        Iterator iter = channels.entrySet().iterator();
        for (int i=0; iter.hasNext(); i++) {
            Map.Entry entry = (Map.Entry) iter.next();
            WeakReference ref = (WeakReference) entry.getValue();
            ISOChannel c = (ISOChannel) ref.get ();
            if (c != null && !LAST.equals (entry.getKey()) && c.isConnected()) {
                if (i > 0) {
                    sb.append (' ');
                }
                sb.append (entry.getKey());
            }
        }
        return sb.toString();
    }
    public String getCountersAsString () {
        StringBuilder sb = new StringBuilder ();
        int cnt[] = getCounters();
        sb.append ("connected=");
        sb.append (Integer.toString(cnt[2]));
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

    @Override
    public int getTXCounter() {
        int cnt[] = getCounters();
        return cnt[1];
    }
    @Override
    public int getRXCounter() {
        int cnt[] = getCounters();
        return cnt[0];
    }
    public int getConnections () {
        return connectionCount.get();
    }
    @Override
    public long getLastTxnTimestampInMillis() {
        return lastTxn;
    }
    @Override
    public long getIdleTimeInMillis() {
        return lastTxn > 0L ? System.currentTimeMillis() - lastTxn : -1L;
    }


    @Override
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
    @Override
    public void dump (PrintStream p, String indent) {
        p.println (indent + getCountersAsString());
        Iterator iter = channels.entrySet().iterator();
        String inner = indent + "  ";
        for (int i=0; iter.hasNext(); i++) {
            Map.Entry entry = (Map.Entry) iter.next();
            WeakReference ref = (WeakReference) entry.getValue();
            ISOChannel c = (ISOChannel) ref.get ();
            if (c != null && !LAST.equals (entry.getKey()) && c.isConnected() && c instanceof BaseChannel) {
                StringBuilder sb = new StringBuilder ();
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
    private void append (StringBuffer sb, String name, int value) {
        sb.append (name);
        sb.append (value);
    }

    public void addServerEventListener(ISOServerEventListener listener) {
        serverListeners.add(listener);
    }
    public void removeServerEventListener(ISOServerEventListener listener) {
        serverListeners.remove(listener);
    }

    public void fireEvent(EventObject event) {
        for (ISOServerEventListener l : serverListeners) {
            try {
                l.handleISOServerEvent(event);
            }
            catch (Exception ignore) {
                /*
                 * Don't want an exception from a handler to exit the loop or
                 * let it bubble up.
                 * If it bubbles up it can cause side effects like getting caught
                 * in the throwable catch leading to server trying to listen on
                 * the same port.
                 * We don't want a side effect in jpos caused by custom user
                 * handler code.
                 */
            }

        }
    }

    private void log (String level, String message) {
        LogEvent evt = new LogEvent (this, level);
        evt.addMessage (message);
        Logger.log (evt);
    }

    public int getActiveConnections () {
        return permitsCount - permits.availablePermits();
    }
}

