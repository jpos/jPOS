package org.jpos.iso;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;

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

public class ISOMUX implements Runnable, ISOSource, LogSource {
    private ISOChannel channel;
    private Thread rx = null, tx = null;
    private Vector txQueue;
    private Hashtable rxQueue;
    private int traceNumberField = 11;
    private volatile boolean terminate = false;
    private String name;
    private ISOMUX muxInstance;

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
	muxInstance = this;
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
    private void initMUX (ISOChannel c) {
        channel = c;
        rx = null;
        txQueue = new Vector();
        rxQueue = new Hashtable();
        cnt = new int[SIZEOF_CNT];
        requestListener = null;
        rx = new Thread (new Receiver(this));
	name = "";
    }
    /**
     * allow changes to default value 11 (used in ANSI X9.2 messages)
     * @param traceNumberField new traceNumberField
     */  
    public void setTraceNumberField(int traceNumberField) {
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
                + ISOUtil.zeropad((String) m.getValue(traceNumberField),6);
    }

    /**
     * get rid of expired requests
     */
    private void purgeRxQueue() {
        Enumeration e = rxQueue.keys();
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

    private class Receiver implements Runnable, LogSource {
        Runnable parent;
        protected Receiver(Runnable p) {
            parent = p;
        }
        public void run() {
            while (!terminate || !rxQueue.isEmpty() || !txQueue.isEmpty()) {
                if (channel.isConnected()) {
                    try {
                        ISOMsg d = channel.receive();
                        cnt[RX]++;
                        String k = getKey(d);
                        ISORequest r = (ISORequest) rxQueue.get(k);
                        if (r != null) {
                            rxQueue.remove(k);
                            if (r.isExpired()) {
                                if ((++cnt[RX_EXPIRED]) % 10 == 0)
                                    purgeRxQueue();
                            }
                            else {
                                r.setResponse(d);
                            }
                        }
                        else {
                            if (requestListener != null) {
                                requestListener.process(muxInstance, d);
                                cnt[RX_FORWARDED]++;
                            }
                            else 
                                cnt[RX_UNKNOWN]++;
                        }
                    } catch (Exception e) {
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
	    Object o = txQueue.firstElement();
	    ISOMsg m = null;

	    if (o instanceof ISORequest) {
		ISORequest r = (ISORequest) o;
		if (r.isExpired()) 
		    cnt[TX_EXPIRED]++;
		else {
		    m = r.getRequest();
		    rxQueue.put (getKey(m), r);
		    r.setTransmitted ();
		}
	    } else if (o instanceof ISOMsg) {
		m = (ISOMsg) o;
	    }
	    if (m != null) {
                channel.send(m);
                cnt[TX]++;
            }
            txQueue.removeElement(o);
            txQueue.trimToSize();
        }
    }
    public void run () {
        tx = Thread.currentThread();
	                                        // OS/400 V4R4 JVM 
	rx.setPriority (rx.getPriority()+1);    // Thread problem
					        // (Vincent.Greene@amo.com)
        rx.start();
	boolean firstTime = true;
        while (!terminate || !txQueue.isEmpty()) {
            try {
                if (channel.isConnected()) {
                    doTransmit();
                }
                else {
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
	    } catch (Exception e) {
		Logger.log (new LogEvent (this, "mux", e));
            }
        }
	// Wait for the receive queue to empty out before shutting down
	while (!rxQueue.isEmpty()) {
	    try {
		Thread.sleep(5000); // Wait for the receive queue to clear.
		purgeRxQueue();	    // get rid of expired stuff
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
	txQueue.addElement(r);
	this.notify();
    }
    /**
     * send a message over channel, usually a
     * response from an ISORequestListener
     */
    synchronized public void send(ISOMsg m) {
	txQueue.addElement(m);
	this.notify();
    }

    private void terminate(boolean hard) {
	Logger.log (new LogEvent (this, "mux", 
            "<terminate type=\"" + (hard ? "hard" : "soft") +"\"/>"));
	terminate = true;
        synchronized(this) {
            if (hard) {
                txQueue.removeAllElements();
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
}
