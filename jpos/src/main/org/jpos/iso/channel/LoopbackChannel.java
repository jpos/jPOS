package org.jpos.iso.channel;
/**
 * sends back requests, posibly applying filters
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @since 1.2.2
 */

import java.io.IOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.FilteredBase;
import org.jpos.util.NameRegistrar;
import org.jpos.util.BlockingQueue;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.iso.ISOFilter.VetoException;

public class LoopbackChannel extends FilteredBase implements LogSource {
    boolean usable = true;
    private int[] cnt;
    String name;
    BlockingQueue queue;
    Logger logger;
    String realm;

    public LoopbackChannel () {
	super();
        cnt = new int[SIZEOF_CNT];
	queue = new BlockingQueue();
    }

   /**
    * setPackager is optional on LoopbackChannel, it is
    * used for debugging/formating purposes only
    */
    public void setPackager(ISOPackager packager) {
	// N/A
    }

    public void connect () {
	cnt[CONNECT]++;
	usable = true;
    }

    /**
     * disconnects ISOChannel
     * @exception IOException
     */
    public void disconnect () {
	usable = false;
    }

    public void reconnect() {
	usable = true;
    }

    public boolean isConnected() {
	return usable;
    }

    public void send (ISOMsg m)
	throws IOException,ISOException, VetoException
    {
	if (!isConnected())
	    throw new ISOException ("unconnected ISOChannel");
	LogEvent evt = new LogEvent (this, "loopback-send", m);
	applyOutgoingFilters (m, evt);
	queue.enqueue (m);
	cnt[TX]++;
	Logger.log (evt);
    }

    public ISOMsg receive() throws IOException, ISOException
    {
	if (!isConnected())
	    throw new ISOException ("unconnected ISOChannel");
	try {
	    ISOMsg m = (ISOMsg) queue.dequeue();
	    LogEvent evt = new LogEvent (this, "loopback-receive", m);
	    applyIncomingFilters (m, evt);
	    cnt[RX]++;
	    Logger.log (evt);
	    return m;
	} catch (InterruptedException e) {
	    throw new IOException (e.toString());
	} catch (BlockingQueue.Closed e) {
	    throw new IOException (e.toString());
	}
    }

    public void setUsable(boolean b) {
	this.usable = usable;
    }

    public int[] getCounters() {
	return cnt;
    }

    public void setName (String name) {
	this.name = name;
	NameRegistrar.register ("channel."+name, this);
    }

    public String getName() {
	return name;
    }

    public ISOPackager getPackager() {
	return null;
    }

    public void resetCounters() {
        for (int i=0; i<SIZEOF_CNT; i++)
            cnt[i] = 0;
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
}
