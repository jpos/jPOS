package uy.com.cs.jpos.iso;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

/**
 * Should run in it's own thread. Starts another Receiver thread
 *
 * See the
 * <a href="API_users_guide.html#ISOMUX">API User's Guide</a>
 * for details.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISORequest
 * @see ISOChannel
 * @see ISOException
 */
public class ISOMUX implements Runnable {
	private ISOChannel channel;
	private Thread rx;
	private Vector txQueue;
	private Hashtable rxQueue;

	public static final int CONNECT    = 0;
	public static final int TX         = 1;
	public static final int RX         = 2;
	public static final int TX_EXPIRED = 3;
	public static final int RX_EXPIRED = 4;
	public static final int TX_PENDING = 5;
	public static final int RX_PENDING = 6;
	public static final int RX_UNKNOWN = 7;
	public static final int SIZEOF_CNT = 8;

	private int[] cnt;

	/**
	 * @param c a connected or unconnected ISOChannel
	 */
	public ISOMUX (ISOChannel c) {
		channel = c;
		rx = null;
		txQueue = new Vector();
		rxQueue = new Hashtable();
		cnt = new int[SIZEOF_CNT];
		rx = new Thread (new Receiver(this));
	}

	/**
	 * construct key to match request with responses
	 * @param	m	request/response
	 * @return		key (default terminal(41) + tracenumber(11))
	 */
	protected String getKey(ISOMsg m) throws ISOException {
		return    ISOUtil.zeropad((String) m.getValue(41),8) 
				+ ISOUtil.zeropad((String) m.getValue(11),6);
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
		p.println("           Conexiones: " + c[CONNECT]);
		p.println("Mensajes transmitidos: " + c[TX]);
		p.println("         tx expirados: " + c[TX_EXPIRED]);
		p.println("        tx pendientes: " + c[TX_PENDING]);
		p.println("   Mensajes recibidos: " + c[RX]);
		p.println("         rx expirados: " + c[RX_EXPIRED]);
		p.println("        rx pendientes: " + c[RX_PENDING]);
		p.println("    rx no reconocidos: " + c[RX_UNKNOWN]);
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

	private class Receiver implements Runnable {
		Runnable parent;
		protected Receiver(Runnable p) {
			parent = p;
		}
		public void run() {
			for (;;) {
				if (channel.isConnected()) {
					try {
						ISOMsg d = channel.receive();
						cnt[RX]++;
						// d.dump (System.out, "<--- ");
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
						else 
							cnt[RX_UNKNOWN]++;
					} catch (ISOException e) {
						channel.setUsable(false);
						System.out.println("Receiver ISOException");
						e.printStackTrace();
						synchronized(parent) {
							parent.notify();
						}
					} catch (IOException e) {
						channel.setUsable(false);
						System.out.println("Receiver IOException");
						e.printStackTrace();
						synchronized(parent) {
							parent.notify();
						}
					}
				}
				else {
					try {
						synchronized(rx) {
							rx.wait();
						}
					} catch (InterruptedException e) { 
						System.out.println ("Receiver was interrupted");
					}
				}
			}
		}
	}

	private void doTransmit() throws ISOException, IOException {
		while (txQueue.size() > 0) {
			ISORequest r = (ISORequest) txQueue.firstElement();
			if (r.isExpired()) 
				cnt[TX_EXPIRED]++;
			else {
				ISOMsg m = r.getRequest();
				channel.send(m);
				m.dump (System.out, "---> ");
				cnt[TX]++;
				rxQueue.put (getKey(m), r);
			}
			txQueue.removeElement(r);
			txQueue.trimToSize();
		}
	}
	public void run () {
		rx.start();
		for (;;) {
			try {
				if (channel.isConnected()) {
					doTransmit();
				}
				else {
					channel.reconnect();
					cnt[CONNECT]++;
					synchronized(rx) {
						rx.notify();
					}
				}
				synchronized(this) {
					if (channel.isConnected() && txQueue.size() == 0)
						this.wait();
				}
			}
			catch (UnknownHostException e) { 
			 	// System.out.println(e);
				try {
					Thread.sleep(50000);
				} catch (InterruptedException ie) { }
			}
			catch (ConnectException e) { 
			 	// System.out.println(e);
				try {
					Thread.sleep(50000);
				} catch (InterruptedException ie) { }
			}
			catch (java.net.SocketException e) { 
			 	// System.out.println(e);
				try {
					Thread.sleep(50000);
				} catch (InterruptedException ie) { }
			}
			catch (IOException e) {
				e.printStackTrace();
				// delay after IO exception
				try { Thread.sleep(1000); } catch (InterruptedException ie) { }
			}
			catch (InterruptedException e) {
				System.out.println("ISOMUX interrupted");
			}
			catch (ISOException e) {
				e.printStackTrace();
			}
		}
	}
	public void queue(ISORequest r) {
		synchronized(this) {
			txQueue.addElement(r);
			this.notify();
		}
	}
}
