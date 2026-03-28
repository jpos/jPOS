/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import org.jpos.core.handlers.exception.ExceptionHandler;
import org.jpos.core.handlers.exception.ExceptionHandlerAware;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.iso.header.BaseHeader;
import org.jpos.jfr.ChannelEvent;
import org.jpos.log.evt.Connect;
import org.jpos.log.evt.Disconnect;
import org.jpos.metrics.MeterInfo;
import org.jpos.metrics.iso.ISOMsgMetrics;
import org.jpos.util.*;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * BaseChannel was ISOChannel. Now ISOChannel is an interface
 * Revision: 1.34 Date: 2000/04/08 23:54:55
 */

/**
 * ISOChannel is an abstract class that provides functionality that
 * allows the transmission and reception of ISO 8583 Messages
 * over a TCP/IP session.
 * <p>
 * This class is not thread-safe.
 * <p>
 * ISOChannel is Observable in order to support GUI components
 * such as ISOChannelPanel.
 * <br>
 * It now support the new Logger architecture so we will
 * probably setup ISOChannelPanel to be a LogListener instead
 * of being an Observer in future releases.
 *
 * @author Alejandro P. Revilla
 * @author Bharavi Gade
 * @version $Revision$ $Date$
 * @see ISOMsg
 * @see MUX
 * @see ISOException
 * @see org.jpos.iso.channel.CSChannel
 * @see Logger
 *
 */
@SuppressWarnings("unchecked")
public abstract class BaseChannel extends Observable
    implements FilteredChannel, ClientChannel, ServerChannel, FactoryChannel, ISOMsgMetrics.Source,
               LogSource, Configurable, BaseChannelMBean, Cloneable, ExceptionHandlerAware
{
    private Socket socket;
    private String host, localIface;
    private String[] hosts;
    private int[] ports;
    private int port, timeout, connectTimeout, localPort;
    private int maxPacketLength = 100000;
    private boolean keepAlive;
    private boolean expectKeepAlive;
    private boolean soLingerOn = true;
    private int soLingerSeconds = 5;
    private Configuration cfg;
    /** Whether this channel is in a usable state. */
    protected boolean usable;
    /** When true, this channel's own header overrides the message's header. */
    protected boolean overrideHeader;
    private String name;
    private long sendTimeout = 15000L;
    // private int serverPort = -1;
    /** Input stream for reading from the connected socket. */
    protected DataInputStream serverIn;
    /** Output stream for writing to the connected socket. */
    protected DataOutputStream serverOut;
    // The lock objects should be final, and never changed, but due to the clone() method, they must be set there.
    /** Lock guarding {@link #serverIn} and read operations. */
    protected Lock serverInLock = new ReentrantLock();
    /** Lock guarding {@link #serverOut} and write operations. */
    protected Lock serverOutLock = new ReentrantLock();
    /** The packager used to pack/unpack ISO messages. */
    protected ISOPackager packager;
    /** Server socket used when this channel acts as a server. */
    protected ServerSocket serverSocket = null;
    /** Incoming and outgoing filter chains. */
    protected List<ISOFilter> incomingFilters, outgoingFilters;
    /** Optional factory for creating client sockets. */
    protected ISOClientSocketFactory socketFactory = null;

    /** Message counters array. */
    protected int[] cnt;

    /** The logger for this channel. */
    protected Logger logger = null;
    /** The realm string for this channel. */
    protected String realm = null;
    /** The original realm before any override. */
    protected String originalRealm = null;
    /** Message header bytes. */
    protected byte[] header = null;
    private static final int DEFAULT_TIMEOUT = 300000;
    private int nextHostPort = 0;
    private boolean roundRobin = false;
    private boolean debugIsoError = true;

    private ISOMsgMetrics isoMsgMetrics;

    private final UUID uuid;

    private final Map<Class<? extends Exception>, List<ExceptionHandler>> exceptionHandlers = new HashMap<>();

    /**
     * constructor shared by server and client
     * ISOChannels (which have different signatures)
     */
    public BaseChannel () {
        super();
        uuid = UUID.randomUUID();
        cnt = new int[SIZEOF_CNT];
        name = "";
        incomingFilters = new ArrayList<>();
        outgoingFilters = new ArrayList<>();
        setHost(null, 0);
    }

    /**
     * constructs a client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public BaseChannel (String host, int port, ISOPackager p) {
        this();
        setHost(host, port);
        setPackager(p);
    }
    /**
     * constructs a server ISOChannel
     * @param p     an ISOPackager
     * @exception IOException on error
     * @see ISOPackager
     */
    public BaseChannel (ISOPackager p) throws IOException {
        this();
        setPackager (p);
    }

    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException on error
     * @see ISOPackager
     */
    public BaseChannel (ISOPackager p, ServerSocket serverSocket) throws IOException {
        this();
        setPackager (p);
        setServerSocket (serverSocket);
    }

    /**
     * initialize an ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     */
    public void setHost(String host, int port) {
        this.host = host;
        this.port = port;
        this.hosts = new String[] { host };
        this.ports = new int[] { port };
    }

    /**
     * initialize an ISOChannel
     * @param iface server TCP Address
     * @param port  server port number
     */
    public void setLocalAddress (String iface, int port) {
        this.localIface = iface;
        this.localPort = port;
    }


    /**
     * @param host to connect (client ISOChannel)
     */
    public void setHost (String host) {
        this.host = host;
        this.hosts = new String[] { host };
    }
    /**
     * @param port to connect (client ISOChannel)
     */
    public void setPort (int port) {
        this.port = port;
        this.ports = new int[] { port };
    }
    /**
     * @return hostname (may be null)
     */
    public String getHost() {
        return host;
    }
    /**
     * @return port number
     */
    public int getPort() {
        return port;
    }
    /**
     * set Packager for channel
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public void setPackager(ISOPackager p) {
        this.packager = p;
    }

    /**
     * @return current packager
     */
    public ISOPackager getPackager() {
        return packager;
    }

    /**
     * Associates this ISOChannel with a server socket
     * @param sock where to accept a connection
     */
    public void setServerSocket (ServerSocket sock) {
        setHost (null, 0);
        this.serverSocket = sock;
        name = "";
    }

    @Override
    public Map<Class<? extends Exception>, List<ExceptionHandler>> getExceptionHandlers() {
        return exceptionHandlers;
    }

    /**
     * reset stat info
     */
    public void resetCounters() {
        for (int i=0; i<SIZEOF_CNT; i++)
            cnt[i] = 0;
    }
   /**
    * Returns the message counters array.
     * @return counters
    */
    public int[] getCounters() {
        return cnt;
    }
    /**
     * @return the connection state
     */
    public boolean isConnected() {
        return socket != null && usable;
    }

    @Override
    public void setISOMsgMetrics(ISOMsgMetrics metrics) {
        isoMsgMetrics = metrics;
    }
    @Override
    public ISOMsgMetrics getISOMsgMetrics() {
        return isoMsgMetrics;
    }

    /**
     * setup I/O Streams from socket
     * @param socket a Socket (client or server)
     * @exception IOException on error
     */
    protected void connect (Socket socket) throws IOException {
        this.socket = socket;
        applyTimeout();
        serverInLock.lock();
        try {
            serverIn = new DataInputStream (
              new BufferedInputStream (socket.getInputStream ())
            );
        } finally {
            serverInLock.unlock();
        }
        serverOutLock.lock();
        try {
            serverOut = new DataOutputStream(
              new BufferedOutputStream(socket.getOutputStream(), 2048)
            );
        } finally {
            serverOutLock.unlock();
        }
        postConnectHook();
        usable = true;
        cnt[CONNECT]++;
        setChanged();
        notifyObservers();
    }
    /** Hook called after a successful connection. May be overridden by subclasses.
     * @throws IOException on I/O error
     */
    protected void postConnectHook() throws IOException {
        // do nothing
    }
    /**
     * factory method pattern (as suggested by Vincent.Greene@amo.com)
     * @param host remote host
     * @param port remote port
     * @throws IOException on error
     *
     * Use Socket factory if exists. If it is missing create a normal socket
     * @see ISOClientSocketFactory
     * @return newly created socket
     */
    protected Socket newSocket(String host, int port) throws IOException {
        try {
            if (socketFactory != null)
                return socketFactory.createSocket (host, port);
            else {
                Socket s = new Socket();
                if (localIface != null || localPort != 0) {
                    InetAddress addr = localIface == null ?
                      InetAddress.getLocalHost() :
                      InetAddress.getByName(localIface);
                    s.bind(new InetSocketAddress(addr, localPort));
                }
                s.connect (
                    new InetSocketAddress (host, port),
                    connectTimeout
                );
                return s;
            }
        } catch (ISOException e) {
            throw new IOException (e.getMessage());
        }
    }
    /** Creates a new socket connected to one of the given hosts/ports.
     * @param hosts array of host names to try
     * @param ports corresponding array of port numbers
     * @param evt  log event for connection diagnostics
     * @return the connected socket
     * @throws IOException on connection failure
     */
    protected Socket newSocket (String[] hosts, int[] ports, LogEvent evt)
        throws IOException
    {
        Socket s = null;
        if (!roundRobin)
            nextHostPort = 0;

        IOException lastIOException = null;
        String h=null;
        int p=0;
        for (int i=0; i<hosts.length; i++) {
            try {
                int ii = nextHostPort++ % hosts.length;
                h = hosts[ii];
                p = ports[ii];
                s = newSocket (h, p);
                evt.addMessage(new Connect(
                  s.getInetAddress().getHostAddress(),
                  s.getPort(),
                  s.getLocalPort(), null)
                );
                break;
            } catch (IOException e) {
                lastIOException = e;
                evt.addMessage(
                  new Connect(h, p, 0, "%s:%d %s (%s)".formatted(
                      h, p, Caller.shortClassName(lastIOException.getClass().getName()), Caller.info(1)
                    )
                  )
                );
            }
        }
        if (s == null)
            throw new IOException ("%s:%d %s (%s)".formatted(h, p, Caller.shortClassName(lastIOException.getClass().getName()), Caller.info(1)));
        return s;
    }
    /**
     * Returns the current connected socket.
     * @return current socket
     */
    public Socket getSocket() {
        return socket;
    }
    /**
     * Returns the current server socket.
     * @return current serverSocket
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * sets socket timeout (as suggested by
     * Leonard Thomas {@literal <leonard@rhinosystemsinc.com>})
     * @param timeout in milliseconds
     * @throws SocketException on error
     */
    public void setTimeout (int timeout) throws SocketException {
        this.timeout = timeout;
        applyTimeout();
    }
    /** Returns the socket read timeout in milliseconds.
     * @return socket timeout in milliseconds
     */
    public int getTimeout () {
        return timeout;
    }

    /**
     * sets timeout, and also keep alive
     * @throws SocketException on error setting the timeout
     */
    protected void applyTimeout () throws SocketException {
        if (socket != null && socket.isConnected()) {
            if (keepAlive)
                socket.setKeepAlive(keepAlive);
            if (timeout >= 0)
                socket.setSoTimeout(timeout);
        }
    }
    /**
     * Socket SO_LINGER option to use when closing the socket.
     * @see java.net.Socket#setSoLinger(boolean, int)
     * @param on     if true, enable SO_LINGER
     * @param linger SO_LINGER timeout in seconds
     */
    public void setSoLinger(boolean on, int linger) {
        this.soLingerOn = on;
        this.soLingerSeconds = linger;
    }
    /** Returns whether SO_LINGER is enabled on the socket.
     * @return true if SO_LINGER is enabled
     */
    public boolean isSoLingerOn() {
        return soLingerOn;
    }
    /** Returns the SO_LINGER timeout in seconds.
     * @return SO_LINGER timeout in seconds
     */
    public int getSoLingerSeconds() {
        return soLingerSeconds;
    }
    /**
     * Connects client ISOChannel to server
     * @exception IOException on I/O error
     */
    public void connect () throws IOException {
        ChannelEvent jfr = new ChannelEvent.Connect();
        jfr.begin();
        LogEvent evt = new LogEvent (this, "connect").withTraceId(uuid);
        try {
            socket = newSocket (hosts, ports, evt);
            if (getHost() != null)
                jfr.setDetail("%s:%d".formatted(getHost(), getPort()));
            connect(socket);
            jfr.append("%d".formatted(socket.getLocalPort()));
            evt.withTraceId(getSocketUUID());
            evt.withTag("endpoint", toEndpoint(socket));
            applyTimeout();
        } catch (IOException e) {
            jfr = new ChannelEvent.ConnectionException(jfr.getDetail());
            jfr.begin();
            jfr.append (e.getMessage());
            throw e;
        } finally {
            Logger.log (evt);
            jfr.commit();
        }
    }

    /**
     * Accepts connection
     * @exception IOException on I/O error
     */
    public void accept(ServerSocket s) throws IOException {
        ChannelEvent jfr = new ChannelEvent.Accept();
        jfr.begin();
        try {
            Socket ss = s.accept();
            this.name = "%d %s:%d".formatted(
              ss.getLocalPort(),
              ss.getInetAddress().getHostAddress(),
              ss.getPort()
            );
            jfr.setDetail(name);
            connect(ss);
        } catch (IOException e) {
            jfr = new ChannelEvent.AcceptException(e.getMessage());
            jfr.begin();
            throw e;
        } finally {
            jfr.commit();
        }

        // Warning - closing here breaks ISOServer, we need an
        // accept that keep ServerSocket open.
        // s.close();
    }

    /**
     * @param b - new Usable state (used by ISOMUX internals to
     * flag as unusable in order to force a reconnection)
     */
    public void setUsable(boolean b) {
        Logger.log (new LogEvent (this, "usable", b));
        usable = b;
    }
   /**
    * allow subclasses to override default packager
    * on outgoing messages
    * @param m outgoing ISOMsg
    * @return ISOPackager
    */
    protected ISOPackager getDynamicPackager (ISOMsg m) {
        return packager;
    }

   /**
    * allow subclasses to override default packager
    * on incoming messages
    * @param image incoming message image
    * @return ISOPackager
    */
    protected ISOPackager getDynamicPackager (byte[] image) {
        return packager;
    }
    /**
     * allow subclasses to override default packager
     * on incoming messages
     * @param header message header
     * @param image incoming message image
     * @return ISOPackager
     */
     protected ISOPackager getDynamicPackager (byte[] header, byte[] image) {
         return getDynamicPackager(image);
     }


    /**
     * Allow subclasses to override the Default header on
     * incoming messages.
     * @param image message image
     * @return ISOHeader instance
     */
    protected ISOHeader getDynamicHeader (byte[] image) {
        return image != null ?
            new BaseHeader (image) : null;
    }
    /** Sends the message length prefix; default implementation is a no-op.
     * @param len the message length to send
     * @throws IOException on I/O error
     */
    protected void sendMessageLength(int len) throws IOException { }
    /** Sends the message header bytes.
     * @param m   the ISO message being sent
     * @param len the message length
     * @throws IOException on I/O error
     */
    protected void sendMessageHeader(ISOMsg m, int len) throws IOException {
        if (!isOverrideHeader() && m.getHeader() != null)
            serverOut.write(m.getHeader());
        else if (header != null)
            serverOut.write(header);
    }
    /**
     * @deprecated use sendMessageTrailer(ISOMsg m, byte[] b) instead.
     * @param m a reference to the ISOMsg
     * @param len the packed image length
     * @throws IOException on error
     */
    @Deprecated
    protected void sendMessageTrailler(ISOMsg m, int len) throws IOException
    {
    }

    /**
     * @deprecated use sendMessageTrailer(ISOMsg m, byte[] b instead.
     */
    @SuppressWarnings ("deprecation")
    @Deprecated
    /**
     * Sends the message trailer bytes.
     * @param m the ISO message being sent
     * @param b the trailer bytes to send
     * @throws IOException on I/O error
     */
    /**
     * Sends the deprecated message trailer bytes.
     * @param m the ISO message
     * @param b the trailer bytes
     * @throws IOException on I/O error
     */
    protected void sendMessageTrailler(ISOMsg m, byte[] b) throws IOException  {
        sendMessageTrailler (m, b.length);
    }

    /**
     * Send a Message trailer.
     *
     * @param m The unpacked ISOMsg.
     * @param b The packed ISOMsg image.
     */
    @SuppressWarnings("deprecation")
    /**
     * sendMessageTrailer implementation.
     * @throws IOException on I/O error
     */
    /**
     * Sends the message trailer bytes.
     * @param m the ISO message
     * @param b the trailer bytes
     * @throws IOException on I/O error
     */
    protected void sendMessageTrailer(ISOMsg m, byte[] b) throws IOException {
        sendMessageTrailler(m, b);
    }


    /**
     * @deprecated use getMessageTrailer(ISOMsg m) instead.
     */
    @Deprecated
    /**
     * getMessageTrailler implementation.
     * @throws IOException on I/O error
     */
    /**
     * Reads the deprecated message trailer from the stream.
     * @throws IOException on I/O error
     */
    protected void getMessageTrailler() throws IOException {
    }

    /**
     * Read some trailer data from this channel and optionally store it in the incoming ISOMsg.
     *
     * @param m The ISOMessage to store the trailer data.
     * @see ISOMsg#setTrailer(byte[])
     */
    @SuppressWarnings("deprecation")
    /**
     * getMessageTrailer implementation.
     * @throws IOException on I/O error
     */
    /**
     * Reads the message trailer from the stream and stores it in the given message.
     * @param m the ISO message to store the trailer in
     * @throws IOException on I/O error
     */
    protected void getMessageTrailer(ISOMsg m) throws IOException {
        getMessageTrailler();
    }

    /** Reads raw message bytes from the stream into the given buffer.
     * @param b      the destination buffer
     * @param offset starting offset in the buffer
     * @param len    number of bytes to read
     * @throws IOException  on I/O error
     * @throws ISOException on protocol error
     */
    protected void getMessage (byte[] b, int offset, int len) throws IOException, ISOException {
        serverIn.readFully(b, offset, len);
    }
    /** Reads and returns the message length prefix from the stream.
     * @return the message length
     * @throws IOException  on I/O error
     * @throws ISOException on protocol error
     */
    protected int getMessageLength() throws IOException, ISOException {
        return -1;
    }
    /** Returns the fixed header length for this channel.
     * @return header length in bytes
     */
    protected int getHeaderLength() {
        return header != null ? header.length : 0;
    }
    /** Returns the header length for the given raw message bytes.
     * @param b the raw message bytes
     * @return header length in bytes
     */
    protected int getHeaderLength(byte[] b) { return 0; }

    /** Returns the header length for the given ISO message.
     * @param m the ISO message
     * @return header length in bytes
     */
    protected int getHeaderLength(ISOMsg m) {
        return !overrideHeader && m.getHeader() != null ?
            m.getHeader().length : getHeaderLength();
    }

    /** Reads raw bytes from the stream for protocols without explicit length framing.
     * @return the received bytes
     * @throws IOException on I/O error
     */
    protected byte[] streamReceive() throws IOException {
        return new byte[0];
    }
    /** Writes the given bytes to the output stream.
     * @param b      the byte array to send
     * @param offset starting offset
     * @param len    number of bytes to send
     * @throws IOException on I/O error
     */
    protected void sendMessage (byte[] b, int offset, int len)
        throws IOException
    {
        serverOut.write(b, offset, len);
    }
    /**
     * Sends the specified {@link ISOMsg} over this ISOChannel.
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Verifies the channel is connected.</li>
     *   <li>Sets the message direction to {@link ISOMsg#OUTGOING}.</li>
     *   <li>Retrieves and sets a dynamic packager for the message.</li>
     *   <li>Applies all registered outgoing filters, allowing them to modify or veto the message.</li>
     *   <li>Packs the message and writes its length, header, body, and trailer to the underlying stream,
     *       protected by a locking mechanism to ensure thread safety.</li>
     *   <li>Flushes the output stream and increments message counters.</li>
     *   <li>Notifies observers of the sent message.</li>
     *   <li>Logs both the message and the send operation through {@link ChannelEvent} and {@link LogEvent}.</li>
     * </ul>
     *
     * If a {@link VetoException} is thrown by a filter, the message is not sent, and the exception is logged.
     *
     * @param m the ISO message to be sent. The message will be modified in-place: its direction and packager
     *          will be updated, and filters may alter its content.
     *
     * @throws IOException if the channel is not connected, if the output stream fails, if locking times out,
     *                     or if an unexpected I/O error occurs.
     * @throws ISOException if packing the message fails or other ISO-specific issues occur.
     * @throws VetoException if an outgoing filter vetoes the message.
     *
     * @see ISOMsg
     * @see ISOPackager
     * @see ISOFilter
     * @see ChannelEvent
     * @see LogEvent
     */
    public void send (ISOMsg m)
        throws IOException, ISOException
    {
        ChannelEvent jfr = new ChannelEvent.Send();
        jfr.begin();
        LogEvent evt = new LogEvent (this, "send").withTraceId(getSocketUUID());
        try {
            if (!isConnected())
                throw new IOException ("unconnected ISOChannel");
            m.setDirection(ISOMsg.OUTGOING);
            ISOPackager p = getDynamicPackager(m);
            m.setPackager (p);
            m = applyOutgoingFilters (m, evt);
            evt.addMessage (m);
            applyTags (evt, m);
            m.setDirection(ISOMsg.OUTGOING); // filter may have dropped this info
            m.setPackager (p); // and could have dropped packager as well
            byte[] b = pack(m);

            if (serverOutLock.tryLock(sendTimeout, TimeUnit.MILLISECONDS)) {
                try  {
                    sendMessageLength(b.length + getHeaderLength(m));
                    sendMessageHeader(m, b.length);
                    sendMessage (b, 0, b.length);
                    sendMessageTrailer(m, b);
                    serverOut.flush ();
                    cnt[TX]++;
                } finally {
                    serverOutLock.unlock();
                }
               incrementMsgOutCounter(m);
            } else {
                disconnect();
            }
            setChanged();
            notifyObservers(m);
            jfr.setDetail(m.toString());
        } catch (VetoException e) {
            //if a filter vets the message it was not added to the event
            evt.addMessage (m);
            evt.addMessage (e);
            jfr.append (e.getMessage());
            throw e;
        } catch (ISOException | IOException e) {
            evt.addMessage (e);
            jfr = new ChannelEvent.SendException(e.getMessage());
            throw e;
        } catch (Exception e) {
            evt.addMessage (e);
            jfr = new ChannelEvent.SendException(e.getMessage());
            throw new IOException ("unexpected exception", e);
        } finally {
            Logger.log (evt);
            jfr.commit();
        }
    }
    /**
     * sends a byte[] over the TCP/IP session
     * @param b the byte array to be sent
     * @exception IOException on I/O error
     * @exception ISOException on ISO processing error
     * @exception ISOFilter.VetoException if a filter vetoes the message
     */
    public void send (byte[] b) throws IOException, ISOException {
        var jfr = new ChannelEvent.Send();
        jfr.begin();
        LogEvent evt = new LogEvent (this, "send");
        try {
            if (!isConnected())
                throw new ISOException ("unconnected ISOChannel");
            serverOutLock.lock();
            try {
                serverOut.write(b);
                serverOut.flush();
            } finally {
                serverOutLock.unlock();
            }
            cnt[TX]++;
            setChanged();
        } catch (Exception e) {
            evt.addMessage (e);
            throw new ISOException ("unexpected exception", e);
        } finally {
            Logger.log (evt);
            jfr.commit();
        }
    }
    /**
     * Sends a high-level keep-alive message (zero length)
     * @throws IOException on exception
     */
    public void sendKeepAlive () throws IOException {
        serverOutLock.lock();
        try {
            sendMessageLength(0);
            serverOut.flush ();
        } finally {
            serverOutLock.unlock();
        }
    }

    /** Returns whether this channel expects keep-alive messages.
     * @return true if keep-alive is expected
     */
    public boolean isExpectKeepAlive() {
        return expectKeepAlive;
    }

    /** Returns whether the given raw message should be rejected.
     * @param b the raw message bytes
     * @return true if the message should be rejected
     */
    protected boolean isRejected(byte[] b) {
        // VAP Header support - see VAPChannel
        return false;
    }
    /** Returns whether the given raw message should be silently ignored.
     * @param b the raw message bytes
     * @return true if the message should be ignored
     */
    protected boolean shouldIgnore (byte[] b) {
        // VAP Header support - see VAPChannel
        return false;
    }
    /**
     * support old factory method name for backward compatibility
     * @return newly created ISOMsg
     */
    protected ISOMsg createMsg () {
        return createISOMsg();
    }
    /** Creates a new ISOMsg instance; may be overridden to return a subclass.
     * @return a new ISOMsg
     */
    protected ISOMsg createISOMsg () {
        return packager.createISOMsg ();
    }

    /**
     * Reads in a message header.
     *
     * @param hLen The Length og the reader to read
     * @return The header bytes that were read in
     * @throws IOException on error
     */
    protected byte[] readHeader(int hLen) throws IOException {
        byte[] header = new byte[hLen];
        serverIn.readFully(header, 0, hLen);
        return header;
    }
    /**
     * Waits and receive an ISOMsg over the TCP/IP session
     * @return the Message received
     * @throws IOException on I/O error
     * @throws ISOException on ISO processing error
     */
    public ISOMsg receive() throws IOException, ISOException {
        var jfr = new ChannelEvent.Receive();
        jfr.begin();

        byte[] b=null;
        byte[] header=null;
        LogEvent evt = new LogEvent (this, "receive").withTraceId(getSocketUUID());
        ISOMsg m = createMsg ();  // call createMsg instead of createISOMsg for backward compatibility

        m.setSource (this);
        try {
            if (!isConnected())
                throw new IOException ("unconnected ISOChannel");

            serverInLock.lock();
            try {
                int len  = getMessageLength();
                if (expectKeepAlive) {
                    while (len == 0) {
                        //If zero length, this is a keep alive msg
                        len  = getMessageLength();
                    }
                }
                int hLen = getHeaderLength();

                if (len == -1) {
                    if (hLen > 0) {
                        header = readHeader(hLen);
                    }
                    b = streamReceive();
                }
                else if (len > 0 && len <= getMaxPacketLength()) {
                    if (hLen > 0) {
                        // ignore message header (TPDU)
                        // Note header length is not necessarily equal to hLen (see VAPChannel)
                        header = readHeader(hLen);
                        len -= header.length;
                    }
                    b = new byte[len];
                    getMessage (b, 0, len);
                    getMessageTrailer(m);
                }
                else
                    throw new ISOException(
                        "receive length " +len + " seems strange - maxPacketLength = " + getMaxPacketLength());
            } finally {
                serverInLock.unlock();
            }
            m.setPackager (getDynamicPackager(header, b));
            m.setHeader (getDynamicHeader(header));
            if (b.length > 0 && !shouldIgnore (header))  // Ignore NULL messages
                unpack (m, b);
            m.setDirection(ISOMsg.INCOMING);
            evt.addMessage (m);
            applyTags (evt, m);
            m = applyIncomingFilters (m, header, b, evt);
            m.setDirection(ISOMsg.INCOMING);
            cnt[RX]++;
            incrementMsgInCounter(m);
            setChanged();
            notifyObservers(m);
        } catch (ISOException e) {
            evt.addMessage (e);
            if (header != null) {
                evt.addMessage ("--- header ---");
                evt.addMessage (ISOUtil.hexdump (header));
            }
            if (b != null && debugIsoError) {
                evt.addMessage ("--- data ---");
                evt.addMessage (ISOUtil.hexdump (b));
            }
            throw e;
        } catch (IOException e) {
            evt.addMessage (
              new Disconnect(socket.getInetAddress().getHostAddress(), socket.getPort(), socket.getLocalPort(),
                "%s (%s)".formatted(Caller.shortClassName(e.getClass().getName()), Caller.info()), e.getMessage())
            );
            closeSocket();
            throw e;
        } catch (Exception e) {
            closeSocket();
            evt.addMessage (m);
            evt.addMessage (e);
            throw new IOException ("unexpected exception", e);
        } finally {
            Logger.log (evt);
        }
        jfr.setDetail(m.toString());
        jfr.commit();
        return m;
    }
    /**
     * Low level receive
     * @param b byte array
     * @throws IOException on error
     * @return the total number of bytes read into the buffer,
     * or -1 if there is no more data because the end of the stream has been reached.
     */
    public int getBytes (byte[] b) throws IOException {
        return serverIn.read (b);
    }
    /**
     * disconnects the TCP/IP session. The instance is ready for
     * a reconnection. There is no need to create a new ISOChannel<br>
     * @exception IOException on I/O error
     */
    public void disconnect () throws IOException {
        var jfr = new ChannelEvent.Disconnect();
        jfr.begin();
        LogEvent evt = new LogEvent (this, "disconnect");
        if (socket != null) {
            String detail = socket.getRemoteSocketAddress().toString();
            jfr.setDetail(detail);
            evt.withTag("endpoint", toEndpoint(socket));
            evt.addMessage(detail);
        }

        try {
            usable = false;
            setChanged();
            notifyObservers();
            closeSocket();
            if (serverIn != null) {
                try {
                    serverIn.close();
                } catch (IOException ex) { evt.addMessage (ex); }
                serverIn  = null;
            }
            if (serverOut != null) {
                try {
                    serverOut.close();
                } catch (IOException ex) { evt.addMessage (ex); }
                serverOut = null;
            }
        } catch (IOException e) {
            evt.addMessage (e);
            jfr.append (e.getMessage());
            Logger.log (evt);
            throw e;
        } finally {
            jfr.commit();
        }
        socket = null;
    }
    /**
     * Issues a disconnect followed by a connect
     * @exception IOException on I/O error
     */
    public void reconnect() throws IOException {
        disconnect();
        connect();
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
        if (originalRealm == null)
            originalRealm = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
    /** Returns a human-readable endpoint string for the given socket.
     * @param socket the connected socket
     * @return endpoint string in the form host:port
     */
    protected String toEndpoint(Socket socket) {
        if (socket == null || socket.getInetAddress() == null)
            return null;
        return "%s:%d".formatted(socket.getInetAddress().getHostAddress(), socket.getPort());
    }
    /** Returns the original realm before any override was applied.
     * @return the original realm string
     */
    public String getOriginalRealm() {
        return originalRealm == null ?
            this.getClass().getName() : originalRealm;
    }
    /**
     * associates this ISOChannel with a name using NameRegistrar
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName (String name) {
        this.name = name;
        NameRegistrar.register("channel." + name, this);
    }
    /**
     * @return this ISOChannel's name ("" if no name was set)
     */
    public String getName() {
        return this.name;
    }
    /**
     * Adds a filter to this channel.
     * @param filter filter to add
     * @param direction ISOMsg.INCOMING, ISOMsg.OUTGOING, 0 for both
     */
    @SuppressWarnings ("unchecked")
    public void addFilter (ISOFilter filter, int direction) {
        switch (direction) {
            case ISOMsg.INCOMING :
                incomingFilters.add (filter);
                break;
            case ISOMsg.OUTGOING :
                outgoingFilters.add (filter);
                break;
            case 0 :
                incomingFilters.add (filter);
                outgoingFilters.add (filter);
                break;
        }
    }
    /**
     * @param filter incoming filter to add
     */
    public void addIncomingFilter (ISOFilter filter) {
        addFilter(filter, ISOMsg.INCOMING);
    }
    /**
     * @param filter outgoing filter to add
     */
    public void addOutgoingFilter (ISOFilter filter) {
        addFilter(filter, ISOMsg.OUTGOING);
    }

    /**
     * Adds a filter to this channel.
     * @param filter filter to add (both directions, incoming/outgoing)
     */
    public void addFilter (ISOFilter filter) {
        addFilter (filter, 0);
    }
    /**
     * Removes a filter from this channel.
     * @param filter filter to remove
     * @param direction ISOMsg.INCOMING, ISOMsg.OUTGOING, 0 for both
     */
    public void removeFilter (ISOFilter filter, int direction) {
        switch (direction) {
            case ISOMsg.INCOMING :
                incomingFilters.remove (filter);
                break;
            case ISOMsg.OUTGOING :
                outgoingFilters.remove (filter);
                break;
            case 0 :
                incomingFilters.remove (filter);
                outgoingFilters.remove (filter);
                break;
        }
    }
    /**
     * Removes a filter from this channel.
     * @param filter filter to remove (both directions)
     */
    public void removeFilter (ISOFilter filter) {
        removeFilter (filter, 0);
    }
    /**
     * @param filter incoming filter to remove
     */
    public void removeIncomingFilter (ISOFilter filter) {
        removeFilter(filter, ISOMsg.INCOMING);
    }
    /**
     * @param filter outgoing filter to remove
     */
    public void removeOutgoingFilter (ISOFilter filter) {
        removeFilter (filter, ISOMsg.OUTGOING);
    }
    /** Applies all registered outgoing filters to the given message.
     * @param m   the message to filter
     * @param evt the log event for diagnostics
     * @return the filtered message
     * @throws VetoException if a filter vetoes the message
     */
    protected ISOMsg applyOutgoingFilters (ISOMsg m, LogEvent evt)
        throws VetoException
    {
        for (ISOFilter f :outgoingFilters)
            m = f.filter (this, m, evt);
        return m;
    }
    /** Applies all registered incoming filters to the given message.
     * @param m   the message to filter
     * @param evt the log event for diagnostics
     * @return the filtered message
     * @throws VetoException if a filter vetoes the message
     */
    protected ISOMsg applyIncomingFilters (ISOMsg m, LogEvent evt)
        throws VetoException
    {
        return applyIncomingFilters (m, null, null, evt);
    }
    /**
     * Applies all registered incoming filters with the raw message header and image.
     * @param m      the decoded message
     * @param header the message header bytes
     * @param image  the raw message bytes
     * @param evt    the log event for diagnostics
     * @return the filtered message
     * @throws VetoException if a filter vetoes the message
     */
    protected ISOMsg applyIncomingFilters (ISOMsg m, byte[] header, byte[] image, LogEvent evt)
        throws VetoException
    {
        for (ISOFilter f :incomingFilters) {
            if (image != null && f instanceof RawIncomingFilter)
                m = ((RawIncomingFilter)f).filter (this, m, header, image, evt);
            else
                m = f.filter (this, m, evt);
        }
        return m;
    }
    /** Unpacks raw bytes into the given ISOMsg using this channel's packager.
     * @param m the message to populate
     * @param b the raw bytes to unpack
     * @throws ISOException on unpack error
     */
    protected void unpack (ISOMsg m, byte[] b) throws ISOException {
        m.unpack (b);
    }
    /** Packs the given ISOMsg into raw bytes using this channel's packager.
     * @param m the message to pack
     * @return the packed byte array
     * @throws ISOException on pack error
     */
    protected byte[] pack (ISOMsg m) throws ISOException {
        return m.pack();
    }
   /**
    * Implements Configurable<br>
    * Properties:<br>
    * <ul>
    * <li>host - destination host (if ClientChannel)
    * <li>port - port number      (if ClientChannel)
    * <li>local-iface - local interfase to use (if ClientChannel)
    * <li>local-port - local port to bind (if ClientChannel)
    * </ul>
    * (host not present indicates a ServerChannel)
    *
    * @param cfg Configuration
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        this.cfg = cfg;
        String h    = cfg.get    ("host");
        int port    = cfg.getInt ("port");
        maxPacketLength = cfg.getInt ("max-packet-length", 100000);
        sendTimeout = cfg.getLong ("send-timeout", sendTimeout);
        if (h != null && h.length() > 0) {
            if (port == 0)
                throw new ConfigurationException
                    ("invalid port for host '"+h+"'");
            setHost (h, port);
            setLocalAddress (cfg.get("local-iface", null),cfg.getInt("local-port"));
            String[] altHosts = cfg.getAll  ("alternate-host");
            int[] altPorts = cfg.getInts ("alternate-port");
            hosts = new String[altHosts.length + 1];
            ports = new int[altPorts.length + 1];
            if (hosts.length != ports.length) {
                throw new ConfigurationException (
                    "alternate host/port misconfiguration"
                );
            }
            hosts[0] = host;
            ports[0] = port;
            System.arraycopy (altHosts, 0, hosts, 1, altHosts.length);
            System.arraycopy (altPorts, 0, ports, 1, altPorts.length);
        }
        setOverrideHeader(cfg.getBoolean ("override-header", false));
        keepAlive = cfg.getBoolean ("keep-alive", false);
        expectKeepAlive = cfg.getBoolean ("expect-keep-alive", false);
        roundRobin = cfg.getBoolean ("round-robin", false);
        debugIsoError = cfg.getBoolean ("debug-iso-error", true);
        if (socketFactory != this && socketFactory instanceof Configurable)
            ((Configurable)socketFactory).setConfiguration (cfg);
        try {
            setTimeout (cfg.getInt ("timeout", DEFAULT_TIMEOUT));
            connectTimeout = cfg.getInt ("connect-timeout", timeout);
        } catch (SocketException e) {
            throw new ConfigurationException (e);
        }
    }
    /** Returns the current configuration for this channel.
     * @return the current {@link Configuration}
     */
    public Configuration getConfiguration() {
        return cfg;
    }
    public Collection<ISOFilter> getIncomingFilters() {
        return incomingFilters;
    }
    public Collection<ISOFilter> getOutgoingFilters() {
        return outgoingFilters;
    }
    public void setIncomingFilters (Collection filters) {
        incomingFilters = new ArrayList (filters);
    }
    public void setOutgoingFilters (Collection filters) {
        outgoingFilters = new ArrayList (filters);
    }
    /** Sets the message header bytes for this channel.
     * @param header the header bytes
     */
    public void setHeader (byte[] header) {
        this.header = header;
    }
    /** Sets the message header from a hex string.
     * @param header hex-encoded header string
     */
    public void setHeader (String header) {
        setHeader (header.getBytes());
    }
    /** Returns the message header bytes.
     * @return the header bytes, or null if not set
     */
    public byte[] getHeader () {
        return header;
    }
    /** Controls whether this channel's own header overrides the message header.
     * @param overrideHeader if true, the channel header takes precedence
     */
    public void setOverrideHeader (boolean overrideHeader) {
        this.overrideHeader = overrideHeader;
    }
    /** Returns whether the channel header overrides the message header.
     * @return true if override is active
     */
    public boolean isOverrideHeader () {
        return overrideHeader;
    }
    /**
     * Retrieves a channel instance by name from the NameRegistrar.
     * @param name the Channel's name (without the "channel." prefix)
     * @return ISOChannel instance with given name.
     * @throws NameRegistrar.NotFoundException
     * @see NameRegistrar
     */
    public static ISOChannel getChannel (String name)
        throws NameRegistrar.NotFoundException
    {
        return (ISOChannel) NameRegistrar.get ("channel."+name);
    }
   /**
    * Gets the ISOClientSocketFactory (may be null)
    * @see     ISOClientSocketFactory
    * @since 1.3.3    \
    * @return ISOClientSocketFactory
    */
    public ISOClientSocketFactory getSocketFactory() {
        return socketFactory;
    }
   /**
    * Sets the specified Socket Factory to create sockets
    * @param         socketFactory the ISOClientSocketFactory
    * @see           ISOClientSocketFactory
    * @since 1.3.3
    */
    public void setSocketFactory(ISOClientSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }
    /** Returns the maximum packet length accepted by this channel.
     * @return maximum packet length in bytes
     */
    public int getMaxPacketLength() {
        return maxPacketLength;
    }
    /** Sets the maximum packet length accepted by this channel.
     * @param maxPacketLength maximum packet length in bytes
     */
    public void setMaxPacketLength(int maxPacketLength) {
        this.maxPacketLength = maxPacketLength;
    }
    /** Closes the underlying socket.
     * @throws IOException on I/O error
     */
    protected void closeSocket() throws IOException {
        Socket s = null;
        synchronized (this) {
            if (socket != null) {
                s = socket; // we don't want more than one thread
                socket = null;     // attempting to close the socket
            }
        }
        if (s != null) {
            try {
                s.setSoLinger (soLingerOn, soLingerSeconds);
                if (shutdownSupportedBySocket(s) && !isSoLingerForcingImmediateTcpReset())
                    s.shutdownOutput();  // This will force a TCP FIN to be sent on regular sockets,
            } catch (SocketException e) {
                // NOPMD
                // safe to ignore - can be closed already
                // e.printStackTrace();
            }
            s.close ();
        }
    }
    private boolean shutdownSupportedBySocket(Socket s) {
        return !(s instanceof SSLSocket); // we can't close output on SSL connections, see [jPOS-85]
    }
    private boolean isSoLingerForcingImmediateTcpReset() {
        return soLingerOn && soLingerSeconds == 0;
    }
    public Object clone(){
        try {
            BaseChannel channel = (BaseChannel) super.clone();
            channel.cnt = cnt.clone();
            // The lock objects must also be cloned, and the DataStreams nullified, as it makes no sense
            // to use the new lock objects to protect the old DataStreams.
            // This should be safe as the only code that calls BaseChannel.clone() is ISOServer.run(),
            // and it immediately calls accept(ServerSocket) which does a connect(), and that sets the stream objects.
            channel.serverInLock = new ReentrantLock();
            channel.serverOutLock = new ReentrantLock();
            channel.serverIn = null;
            channel.serverOut = null;
            channel.usable = false;
            channel.socket = null;
            return channel;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private UUID getSocketUUID() {
        return socket != null ?
          new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ^ socket.hashCode()) :
          uuid;
    }

    /** Increments the incoming message counter for the given message.
     * @param m the received message
     * @throws ISOException on error
     */
    protected void incrementMsgInCounter(ISOMsg m) throws ISOException {
        if (isoMsgMetrics != null) {
            isoMsgMetrics.recordMessage(m, MeterInfo.ISOMSG_IN);
        }
    }
    /** Increments the outgoing message counter for the given message.
     * @param m the sent message
     * @throws ISOException on error
     */
    protected void incrementMsgOutCounter(ISOMsg m) throws ISOException {
        if (isoMsgMetrics != null) {
            isoMsgMetrics.recordMessage(m, MeterInfo.ISOMSG_OUT);
        }
    }
    private void applyTags (LogEvent evt, ISOMsg m) {
        if (m.hasField(3)) {
            String f3 = m.getString(3);
            if (f3 != null && f3.length() >= 2)
                evt.withTag("pcode", f3.substring(0, 2));
        }
        if (m.hasField(41))
            evt.withTag("tid", m.getString(41));
        if (m.hasField(42))
            evt.withTag("mid", m.getString(42));
        try {
            String mti = m.getMTI();
            if (mti != null)
                evt.withTag("mti", mti);
        } catch (ISOException ignored) { }
    }
}
