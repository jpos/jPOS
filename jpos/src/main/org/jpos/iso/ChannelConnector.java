package org.jpos.iso;

/**
 * Connects two channels
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @since 1.2.2
 */
public class ChannelConnector {
    ISOChannel client, server;
    Thread rx, tx;
    public ChannelConnector (ISOChannel client, ISOChannel server)
    {
	super();
	this.client      = client;
	this.server      = server;
	rx = new Thread (new Receiver());
	rx.setName ("ChannelConnector.Receiver");
	tx = new Thread (new Sender());
	tx.setName ("ChannelConnector.Sender");
	rx.start();
	tx.start();
    }
    protected class Receiver implements Runnable {
	public void run() {
	    try {
		while (client.isConnected() && server.isConnected())
		    server.send (client.receive());
	    } catch (Throwable e) { }
	    tx.interrupt();
	}
    }
    protected class Sender implements Runnable {
	public void run() {
	    try {
		while (client.isConnected() && server.isConnected())
		    client.send (server.receive());
	    } catch (Throwable e) { }
	    rx.interrupt();
	}
    }
}
