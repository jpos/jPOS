/**
 * Corre en su propio Thread e implementa un MUX
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:27  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

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

	public ISOMUX (ISOChannel c) {
		channel = c;
		rx = null;
		txQueue = new Vector();
		rxQueue = new Hashtable();
		cnt = new int[SIZEOF_CNT];
		rx = new Thread (new Receiver(this));
	}

	/**
	 * Genera clave para matchear responses con sus
	 * respectivos requests.
	 * @param	m	request/response
	 * @return		key (default terminal(41) + tracenumber(11)
	 */
	protected String getKey(ISOMsg m) throws ISOException {
		return    ISOUtil.zeropad((String) m.getValue(41),8) 
				+ ISOUtil.zeropad((String) m.getValue(11),6);
	}

	/**
	 * Elimina de rxQueue requests expired
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
			catch (ConnectException e) { 
			 	System.out.println(e);
				try {
					Thread.sleep(1000);
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

	public static void main (String args[]) {
		ISOChannel channel=new BASE24Channel
			(args[0], Integer.parseInt(args[1]), new ISO87APackager());
		ISOMUX mux = new ISOMUX (channel);

		Thread t = new Thread (mux);
		t.start();

		ISOMsg m = new ISOMsg ();
		ISOPackager packager = new ISO87APackager ();
		m.setPackager (packager);
		try {
			m.set(new ISOField (0,  "0200"));
			m.set(new ISOField (2,  "1234")); // LLNUM
	
			m.set(new ISOField (3,  "000001"));
			m.set(new ISOField (4,  "0000001000"));
			// m.set(new ISOField (30, "C100"));
			m.set(new ISOField (34, "TEST"));
			m.set(new ISOField (36, "5678"));
			m.set(new ISOField (37, "123456789"));
			m.set(new ISOField (38, "123456"));
			m.set(new ISOField (41, "29110000")); 
		} catch (ISOException e) {
			e.printStackTrace();
		}
		for (int i=0; i<100; i++) {
			try {
				m = (ISOMsg) m.clone();
				m.set(new ISOField (11, Integer.toString(i)));
			} catch (ISOException e) {
				e.printStackTrace();
			}
			mux.queue (new ISORequest (m));
		}

		// ISOMsg response = r.getResponse (60*1000);
		// if (response != null) {
		//	response.dump(System.out, "");
		//}
		//else {
		//	System.out.println ("No answer");
		//}

		for (;;) {
			mux.showCounters(System.out);
			System.out.println();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) { }
		}
	}
}
