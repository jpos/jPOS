/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

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
        setChanged();
        notifyObservers();
    }

    /**
     * disconnects ISOChannel
     */
    public void disconnect () {
	usable = false;
        setChanged();
        notifyObservers();
    }

    public void reconnect() {
	usable = true;
        setChanged();
        notifyObservers();
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
        setChanged();
        notifyObservers();
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
