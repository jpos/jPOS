package org.jpos.iso;

import java.io.IOException;
import java.io.EOFException;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.net.ServerSocket;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.ThreadPool;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

/**
 * Accept ServerChannel sessions and forwards them to ISORequestListeners
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ISOServer extends SimpleLogSource implements Runnable {
    int port;
    Class clientSideChannelClass;
    ISOPackager clientPackager;
    Collection clientOutgoingFilters, clientIncomingFilters, listeners;
    ThreadPool pool;
    public static final int DEFAULT_MAX_THREADS = 100;

   /**
    * @param port port to listen
    * @param clientSide client side ISOChannel (where we accept connections)
    * @param pool ThreadPool (created if null)
    * @see ChannelConnector
    */
    public ISOServer(int port, ServerChannel clientSide, ThreadPool pool) {
	super();
	this.port = port;
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
    }

    protected class Session implements Runnable, LogSource {
        ServerChannel channel;
        protected Session(ServerChannel channel) {
            this.channel = channel;
        }
        public void run() {
	    Logger.log (new LogEvent (this, "SessionStart"));
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
            } catch (Exception e) { 
		Logger.log (new LogEvent (this, "SessionError", e));
            } 
	    Logger.log (new LogEvent (this, "SessionEnd"));
        }
	public void setLogger (Logger logger, String realm) { 
	}
	public String getRealm () {
	    return ISOServer.this.getRealm() + ".session";
	}
	public Logger getLogger() {
	    return ISOServer.this.getLogger();
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

    public void run() {
        ServerChannel  channel;
	for (;;) {
	    try {
		ServerSocket serverSocket = new ServerSocket(port);
		Logger.log (new LogEvent (this, "iso-server", 
		    "listening on port "+port));
		for (;;) {
		    try {
			channel = (ServerChannel) 
			    clientSideChannelClass.newInstance();
			channel.setPackager (clientPackager);
			setFilters (channel);
			if (channel instanceof LogSource) {
			    ((LogSource)channel) .
				setLogger (getLogger(), getRealm()+".channel");
			}
			channel.accept (serverSocket);
			pool.execute (new Session(channel));
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
		setIncomingFilters (clientOutgoingFilters);
    }

    private void relax() {
	try {
	    Thread.sleep (5000);
	} catch (InterruptedException e) { }
    }
}

