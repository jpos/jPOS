package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ISOChannel is an abstract class that provides functionality that
 * allows the transmision and reception of ISO 8583 Messages
 * over a TCP/IP session.<br>
 *
 * It is not thread-safe, ISOMUX takes care of the
 * synchronization details
 *
 * @see ISOMUX
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOMUX
 * @see ISOException
 * @see CSChannel
 * @see Internetworking_with_TCP/IP_ISBN_0-13-474321-0
 *
 */
public abstract class ISOChannel extends Observable {
    private Socket socket;
    private String host;
    private int port;
    private boolean usable;
    protected DataInputStream serverIn;
    protected DataOutputStream serverOut;
    protected ISOPackager packager;
    protected ServerSocket serverSocket = null;

    public static final int CONNECT      = 0;
    public static final int TX           = 1;
    public static final int RX           = 2;
    public static final int SIZEOF_CNT   = 3;

    private int[] cnt;

    /**
     * constructor shared by server and client
     * ISOChannels (which have different signatures)
     */
    public ISOChannel () {
        cnt = new int[SIZEOF_CNT];
    }

    /**
     * constructs a client ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     * @param p     an ISOPackager
     * @see ISOPackager
     */
    public ISOChannel (String host, int port, ISOPackager p) {
        this();
        setHost(host, port);
        setPackager(p);
    }
    /**
     * initialize an ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     */
    public void setHost(String host, int port) {
        this.host = host;
        this.port = port;
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
     * constructs a server ISOChannel
     * @param p     an ISOPackager
     * @exception IOException
     * @see ISOPackager
     */
    public ISOChannel (ISOPackager p) throws IOException {
        this();
        this.host = null;
        this.port = 0;
        this.packager = p;
    }
    /**
     * constructs a server ISOChannel associated with a Server Socket
     * @param p     an ISOPackager
     * @param serverSocket where to accept a connection
     * @exception IOException
     * @see ISOPackager
     */
    public ISOChannel (ISOPackager p, ServerSocket serverSocket) 
        throws IOException 
    {
        this();
        this.host = null;
        this.port = 0;
        this.packager = p;
        this.serverSocket = serverSocket;
    }
    /**
     * reset stat info
     */
    public void resetCounters() {
        for (int i=0; i<SIZEOF_CNT; i++)
            cnt[i] = 0;
    }
    /**
     * get the counters in order to pretty print them
     * or for stats purposes
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
    /**
     * setup I/O Streams from socket
     * @param socket a Socket (client or server)
     * @exception IOException
     */
    protected void connect (Socket socket) throws IOException {
        this.socket = socket;

        serverIn = new DataInputStream (
            new BufferedInputStream (socket.getInputStream ())
        );
        serverOut = new DataOutputStream(
            new BufferedOutputStream(socket.getOutputStream())
        );
        usable = true;
        cnt[CONNECT]++;
        setChanged();
        notifyObservers();
    }
    /**
     * Connects client ISOChannel to server
     * @exception IOException
     */
    public void connect () throws IOException {
        if (serverSocket != null)
            accept(serverSocket);
        else
            connect(new Socket (host, port));
    }
    /**
     * Accepts connection 
     * @exception IOException
     */
    public void accept(ServerSocket s) throws IOException {
        connect(s.accept());
    }

    /**
     * @param b - new Usable state (used by ISOMUX internals to
     * flag as unusable in order to force a reconnection)
     */
    public void setUsable(boolean b) {
        usable = b;
    }
    protected void sendMessageLength(int len) throws IOException { }
    protected void sendMessageHeader(ISOMsg m, int len) throws IOException { }
    protected void sendMessageTrailler(ISOMsg m, int len) throws IOException { }
    protected int getMessageLength() throws IOException, ISOException {
        return -1;
    }
    protected int getHeaderLength()         { return 0; }
    protected int getHeaderLength(byte[] b) { return 0; }
    protected byte[] streamReceive() throws IOException {
        return new byte[0];
    }
    /**
     * sends an ISOMsg over the TCP/IP session
     * @param m the Message to be sent
     * @exception IOException
     * @exception ISOException
     */
    public void send (ISOMsg m) throws IOException, ISOException {
        m.setPackager (packager);
        byte[] b = m.pack();
        // System.out.println (
        //  "--[pack]--\n"+ ISOUtil.hexString(b) + "\n--[end]--");
        sendMessageLength(b.length + getHeaderLength());
        sendMessageHeader(m, b.length);
        serverOut.write(b, 0, b.length);
        sendMessageTrailler(m, b.length);
        serverOut.flush ();

        m.setDirection(ISOMsg.OUTGOING);
        cnt[TX]++;
        setChanged();
        notifyObservers(m);
    }
    protected boolean isRejected(byte[] b) {
        // VAP Header support - see VAPChannel
        return false;
    }
    /**
     * Waits and receive an ISOMsg over the TCP/IP session
     * @return the Message received
     * @exception IOException
     * @exception ISOException
     */
    public ISOMsg receive() throws IOException, ISOException {
        byte[] b, header=null;
        int len  = getMessageLength();
        int hLen = getHeaderLength();

        if (len == -1) 
            b = streamReceive();
        else if (len > 10 && len <= 4096) {
            int l;
            if (hLen > 0) {
                // ignore message header (TPDU)
                header = new byte [hLen];
                serverIn.readFully(header,0,hLen);
                if (isRejected(header))
                    throw new ISOException ("Unhandled Rejected Message");
                len -= hLen;
            }
            b = new byte[len];
            serverIn.readFully(b,0,len);
            // System.out.println (
            //  "--[unpack]--\n"+ ISOUtil.hexString(b) + "\n--[end]--");
        }
        else
            throw new ISOException("receive length " +len + " seems extrange");

        ISOMsg m = new ISOMsg();
        m.setPackager (packager);
        if (b.length > 0)   // Ignore NULL messages (i.e. VAP/X.25 sync, etc.)
            m.unpack (b);

        m.setHeader(header);
        m.setDirection(ISOMsg.INCOMING);
        cnt[RX]++;
        setChanged();
        notifyObservers(m);
        return m;
    }
    /**
     * Low level receive
     * @param b byte array
     * @exception IOException
     */
    public int getBytes (byte[] b) throws IOException {
        return serverIn.read (b);
    }
    /**
     * disconnects the TCP/IP session. The instance is ready for
     * a reconnection. There is no need to create a new ISOChannel<br>
     * @exception IOException
     */
    public void disconnect () throws IOException {
        usable = false;
        setChanged();
        notifyObservers();
        serverIn  = null;
        serverOut = null;
        if (socket != null)
            socket.close ();
        socket = null;
    }   
    /**
     * Issues a disconnect followed by a connect
     * @exception IOException
     */
    public void reconnect() throws IOException {
        disconnect();
        connect();
    }
}
