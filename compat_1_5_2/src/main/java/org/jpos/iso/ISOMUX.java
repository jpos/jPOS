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

package org.jpos.iso;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Should run in it's own thread. Starts another Receiver thread
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see ISORequest
 * @see ISOChannel
 * @see ISOException
 * @see ISORequestListener
 */

@SuppressWarnings({"unchecked", "deprecation"})
public class ISOMUX implements Runnable, LogSource, MUX,
                               Configurable, Loggeable, ISOMUXMBean
{
    private ISOChannel channel;
    private Thread rx = null, tx = null;
    private List txQueue;
    private Map rxQueue;
    private int traceNumberField = 11;
    private volatile boolean terminate = false;
    private String name;
    private ISOMUX muxInstance;
    private boolean doConnect;

    protected Logger logger = null;
    protected String realm = null;
 
    public static final int CONNECT      = 0;
    public static final int TX           = 1;
    public static final int RX           = 2;
    public static final int TX_EXPIRED   = 3;
    public static final int RX_EXPIRED   = 4;
    public static final int TX_PENDING   = 5;
    public static final int RX_PENDING   = 6;
    public static final int RX_UNKNOWN   = 7;
    public static final int RX_FORWARDED = 8;
    public static final int SIZEOF_CNT   = 9;

    private int[] cnt;

    private ISORequestListener requestListener;

    /**
     * @param c a connected or unconnected ISOChannel
     */
    public ISOMUX (ISOChannel c) {
        super();
        initMUX(c);
    }
    /**
     * @param c a connected or unconnected ISOChannel
     * @param logger a logger
     * @param realm  logger's realm
     */
    public ISOMUX (ISOChannel c, Logger logger, String realm) {
        super();
        setLogger (logger, realm);
        initMUX (c);
    }
    public void setConfiguration (Configuration cfg) {
        setTraceNumberField (cfg.getInt ("tracenofield"));
    }
    private void initMUX (ISOChannel c) {
        doConnect = true;
        channel = c;
        rx = null;
        txQueue = new ArrayList();
        rxQueue = new HashMap();
        cnt = new int[SIZEOF_CNT];
        requestListener = null;
        rx = new Thread (new Receiver(this),"ISOMUX-Receiver");
        name = "";
        muxInstance = this;
    }
    /**
     * allow changes to default value 11 (used in ANSI X9.2 messages)
     * @param traceNumberField new traceNumberField
     */  
    public void setTraceNumberField(int traceNumberField) {
        if (traceNumberField > 0) 
            this.traceNumberField = traceNumberField;
    }
    /**
     * @return the underlying ISOChannel
     */
    public ISOChannel getISOChannel() {
        return channel;
    }
   /**
    * set an ISORequestListener for unmatched messages
    * @param rl a request listener object
    * @see ISORequestListener
    */
    public void setISORequestListener(ISORequestListener rl) {
        requestListener = rl;
    }
   /**
    * remove possible ISORequestListener 
    * @see ISORequestListener
    */
    public void removeISORequestListener() {
        requestListener = null;
    }

    /**
     * construct key to match request with responses
     * @param   m   request/response
     * @return      key (default terminal(41) + tracenumber(11))
     */
    protected String getKey(ISOMsg m) throws ISOException {
        return (m.hasField(41)?ISOUtil.zeropad((String)m.getValue(41),16) : "")
           + (m.hasField (traceNumberField) ?
                ISOUtil.zeropad((String) m.getValue(traceNumberField),6) :
                Long.toString (System.currentTimeMillis()));
    }

    /**
     * get rid of expired requests
     */
    private void purgeRxQueue() {
        Enumeration e = Collections.enumeration(rxQueue.keySet());
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            ISORequest r = (ISORequest) rxQueue.get(key);
            if (r != null && r.isExpired()) {
                rxQueue.remove(key);
                cnt[RX_EXPIRED]++;
            }
        }
    }

    /**
     * show Counters
     * @param p - where to print
     */
    public void showCounters(PrintStream p) {
        int[] c = getCounters();
        p.println("           Connections: " + c[CONNECT]);
        p.println("           TX messages: " + c[TX]);
        p.println("            TX expired: " + c[TX_EXPIRED]);
        p.println("            TX pending: " + c[TX_PENDING]);
        p.println("           RX messages: " + c[RX]);
        p.println("            RX expired: " + c[RX_EXPIRED]);
        p.println("            RX pending: " + c[RX_PENDING]);
        p.println("          RX unmatched: " + c[RX_UNKNOWN]);
        p.println("          RX forwarded: " + c[RX_FORWARDED]);
    }

    /**
     * get the counters in order to pretty print them
     * or for stats purposes
     */
    public int[] getCounters() {
        cnt[TX_PENDING] = txQueue.size();
        cnt[RX_PENDING] = rxQueue.size();
        return cnt;
    }
    public void resetCounters () {
        cnt = new int[SIZEOF_CNT];
    }
    /**
     * @return number of re-connections on the underlying channel
     */
    public int getConnectionCount () {
        return cnt[CONNECT];
    }
    /**
     * @return number of transmitted messages
     */
    public int getTransmitCount () {
        return cnt[TX];
    }
    /**
     * @return number of expired messages
     */
    public int getExpiredCount () {
        return cnt[TX_EXPIRED];
    }
    /**
     * @return number of messages waiting to be transmited
     */
    public int getTransmitPendingCount () {
        return txQueue.size();
    }
    /**
     * @return number of received messages
     */
    public int getReceiveCount () {
        return cnt[RX];
    }
    /**
     * @return number of unanswered messages
     */
    public int getReceiveExpiredCount () {
        return cnt[RX_EXPIRED];
    }
    /**
     * @return number of messages waiting for response
     */
    public int getReceivePendingCount () {
        return rxQueue.size();
    }
    /**
     * @return number of unknown messages received
     */
    public int getUnknownCount () {
        return cnt[RX_UNKNOWN];
    }
    /**
     * @return number of forwarded messages received
     */
    public int getForwardedCount () {
        return cnt[RX_FORWARDED];
    }
    private class Receiver implements Runnable, LogSource {
        Runnable parent;
        protected Receiver(Runnable p) {
            parent = p;
        }
        public void run() {
            int i = 0;
            while (!terminate || !rxQueue.isEmpty() || !txQueue.isEmpty()) {
                if (i++ % 250 == 1) 
                    Logger.log (new LogEvent (this, "mux", parent));
                if (channel.isConnected()) {
                    try {
                        ISOMsg d = channel.receive();
                        cnt[RX]++;
                        String k = getKey(d);
                        ISORequest r = (ISORequest) rxQueue.get(k);
                        boolean forward = true;
                        if (r != null) {
                            rxQueue.remove(k);
                            synchronized (r) {
                                if (r.isExpired()) {
                                    if (++cnt[RX_EXPIRED] % 10 == 0)
                                        purgeRxQueue();
                                }
                                else {
                                    r.setResponse(d);
                                    forward = false;
                                }
                            }
                        }
                        if (forward) {
                            if (requestListener != null) {
                                requestListener.process(muxInstance, d);
                                cnt[RX_FORWARDED]++;
                            }
                            else 
                                cnt[RX_UNKNOWN]++;
                        }
                    } catch (Throwable e) {
                        if (!terminate) {
                            channel.setUsable(false);
                            if (!(e instanceof EOFException))
                                Logger.log (new LogEvent (this, "muxreceiver", e));
                            synchronized(parent) {
                                parent.notify();
                            }
                        }
                    }
                }
                else {
                    try {
                        synchronized(rx) {
                            rx.wait();
                        }
                    } catch (InterruptedException e) { 
                        Logger.log (new LogEvent (this, "muxreceiver", e));
                    }
                }
            }
            Logger.log (new LogEvent (this, "muxreceiver", "terminate"));
        }
        public void setLogger (Logger logger, String realm) { }
        public String getRealm () {
            return realm;
        }
        public Logger getLogger() {
            return logger;
        }
    }

    private void doTransmit() throws ISOException, IOException {
        while (txQueue.size() > 0) {
            Object o = txQueue.get(0);
            ISOMsg m = null;

            if (o instanceof ISORequest) {
                ISORequest r = (ISORequest) o;
                if (r.isExpired()) 
                    cnt[TX_EXPIRED]++;
                else {
                    m = r.getRequest();
                    rxQueue.put (getKey(m), r);
                    r.setTransmitted ();
                    synchronized(rx) {
                        rx.notify(); // required by ChannelPool
                    }
                }
            } else if (o instanceof ISOMsg) {
                m = (ISOMsg) o;
            }
            if (m != null) {
                try {
                    channel.send(m);
                    cnt[TX]++;
                } catch (ISOException e) {
                    Logger.log (new LogEvent (this, "error", e));
                }
            }
            txQueue.remove(o);
        }
    }
    public void run () {
        tx = Thread.currentThread();

        int rxPriority = rx.getPriority();      // Bug#995787
        if (rxPriority < Thread.MAX_PRIORITY) {
                                                // OS/400 V4R4 JVM 
            rx.setPriority (rxPriority+1);      // Thread problem
                                                // (Vincent.Greene@amo.com)
        }
        rx.start();
        boolean firstTime = true;
        while (!terminate || !txQueue.isEmpty()) {
            try {
                if (channel.isConnected()) {
                    doTransmit();
                }
                else if (doConnect) {
                    if (firstTime) {
                        firstTime = !firstTime;
                        channel.connect();
                    }
                    else {
                        Thread.sleep(5000);
                        channel.reconnect();
                    }
                    cnt[CONNECT]++;
                    synchronized(rx) {
                        rx.notify();
                    }
                } else {
                    // nothing to do ...
                    try {
                        Thread.sleep (5000);
                    } catch (InterruptedException ex) { }
                }
                synchronized(this) {
                    if (!terminate && 
                         channel.isConnected() && 
                         txQueue.size() == 0)
                    {
                        this.wait();
                    }
                }
            } catch (ConnectException e) {
                if (channel instanceof ClientChannel) {
                    ClientChannel cc = (ClientChannel) channel;
                    Logger.log (new LogEvent (this, "connection-refused", 
                        cc.getHost()+":"+cc.getPort())
                    );
                }
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException ex) { }
            } catch (Exception e) {
                Logger.log (new LogEvent (this, "mux", e));
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException ex) { }
            }
        }
        // Wait for the receive queue to empty out before shutting down
        while (!rxQueue.isEmpty()) {
            try {
                Thread.sleep(5000); // Wait for the receive queue to clear.
                purgeRxQueue();     // get rid of expired stuff
            } catch (InterruptedException e) {
                break;
            }
        }
        // By closing the channel, we force the receive thread to terminate
        try {
            channel.disconnect();
        } catch (IOException e) { }

        synchronized(rx) {
            rx.notify();
        }
        try {
            rx.join ();
        } catch (InterruptedException e) { }
        Logger.log (new LogEvent (this, "mux", "terminate"));
    }
    /**
     * queue an ISORequest
     */
    synchronized public void queue(ISORequest r) {
        txQueue.add(r);
        this.notify();
    }
    /**
     * send a message over channel, usually a
     * response from an ISORequestListener
     */
    synchronized public void send(ISOMsg m) {
        txQueue.add(m);
        this.notify();
    }

    private void terminate(boolean hard) {
        LogEvent evt = new LogEvent (this, "mux", 
            "<terminate type=\"" + (hard ? "hard" : "soft") +"\"/>");
        evt.addMessage (this);
        Logger.log (evt);

        terminate = true;
        synchronized(this) {
            if (hard) {
                txQueue.clear();
                rxQueue.clear();
            }
            this.notify();
        }
    }

    /**
     * terminate MUX
     * @param wait Time to wait before forcing shutdown
     */
    public void terminate (int wait) {
        terminate(false);
        tx.interrupt();
        rx.interrupt();
        try {
            tx.join(wait);
            if (tx.isAlive()) {
                terminate(true);
                tx.join();
            }
        } catch (InterruptedException e) { }
    }

    /**
     * terminate MUX (soft terminate, wait forever if necessary)
     */
    public void terminate() {
        terminate(0);
    }

    public boolean isConnected() {
        return channel.isConnected();
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
    public boolean isTerminating() {
        return terminate;
    }
    /**
     * associates this ISOMUX with a name using NameRegistrar
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName (String name) {
        this.name = name;
        NameRegistrar.register ("mux."+name, this);
    }
    /**
     * @return ISOMUX instance with given name.
     * @throws NameRegistrar.NotFoundException;
     * @see NameRegistrar
     */
    public static ISOMUX getMUX (String name)
        throws NameRegistrar.NotFoundException
    {
        return (ISOMUX) NameRegistrar.get ("mux."+name);
    }
    /**
     * @return this ISOMUX's name ("" if no name was set)
     */
    public String getName() {
        return this.name;
    }
    /**
     * ISOMUXs usually calls connect() on the underlying ISOChannel<br>
     * You can prevent this behaveour by passing a false value.
     * @param connect false to prevent connection (default true)
     */
    public void setConnect (boolean connect) {
        this.doConnect = connect;
        if (!connect && isConnected()) {
            channel.setUsable(false);
            try {
                channel.disconnect();
            } catch (IOException e) { 
                Logger.log (new LogEvent(this, "set-connect", e));
            }
            synchronized(this) {
                this.notify();
            }
        }
    }
    /**
     * @return connect flag value
     */
    public boolean getConnect() {
        return doConnect;
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + "<mux-stats connected=\"" + 
            channel.isConnected() + "\">");
        showCounters (p);
        p.println (indent + "</mux-stats>");
    }
    public ISOMsg request (ISOMsg m, long timeout) throws ISOException {
        ISORequest req = new ISORequest (m);
        queue (req);
        return req.getResponse ((int) timeout);
    }
    public void request (ISOMsg m, long timeout, ISOResponseListener r, Object handBack) 
        throws ISOException 
    {
        throw new ISOException ("Not implemented");
    }
}

