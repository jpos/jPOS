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

import java.util.List;
import java.util.Vector;
import java.io.IOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

public class ChannelPool implements ISOChannel, LogSource, Configurable {
    boolean usable = true;
    String name = "";
    protected Logger logger;
    protected String realm;
    Configuration cfg = null;
    List pool;
    ISOChannel current;

    public ChannelPool () {
        super ();
        pool = new Vector ();
    }
    public void setPackager(ISOPackager p) {
        // nothing to do
    }
    public synchronized void connect () throws IOException {
        IOException ioe = null;
        current = null;
        LogEvent evt = new LogEvent (this, "connect");
        evt.addMessage ("pool-size=" + Integer.toString (pool.size()));
        for (int i=0; i<pool.size(); i++) {
            try {
                evt.addMessage ("pool-" + Integer.toString (i));
                ISOChannel c = (ISOChannel) pool.get (i);
                c.connect ();
                if (c.isConnected()) {
                    current = c;
                    usable = true;
                    break;
                }
            } catch (IOException e) {
                evt.addMessage (e);
            }
        }
        if (current == null)
            evt.addMessage ("connect failed");
        Logger.log (evt);
        if (current == null) {
            throw new IOException ("unable to connect");
        }
    }
    public synchronized void disconnect () throws IOException {
        current = null;
        LogEvent evt = new LogEvent (this, "disconnect");
        for (int i=0; i<pool.size(); i++) {
            try {
                ISOChannel c = (ISOChannel) pool.get (i);
                c.disconnect ();
            } catch (IOException e) {
                evt.addMessage (e);
            }
        }
        Logger.log (evt);
    }
    public synchronized void reconnect() throws IOException {
        disconnect ();
        connect ();
    }
    public synchronized boolean isConnected() {
        try {
            return getCurrent().isConnected ();
        } catch (IOException e) {
            return false;
        }
    }
    public ISOMsg receive() throws IOException, ISOException {
        return getCurrent().receive ();
    }
    public void send (ISOMsg m) throws IOException, ISOException {
        getCurrent().send (m);
    }
    public void setUsable(boolean b) {
        this.usable = b;
    }
    public void setName (String name) {
	this.name = name;
	NameRegistrar.register ("channel."+name, this);
    }
    public String getName() {
	return this.name;
    }
    public ISOPackager getPackager () {
        return (ISOPackager) null;
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
    public synchronized void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        String channelName[] = cfg.getAll ("channel");
        for (int i=0; i<channelName.length; i++) {
            try {
                addChannel (channelName[i]);
            } catch (NameRegistrar.NotFoundException e) {
                throw new ConfigurationException (e);
            }
        }
    }
    public void addChannel (ISOChannel channel) {
        pool.add (channel);
    }
    public void addChannel (String name) 
        throws NameRegistrar.NotFoundException
    {
        pool.add ((ISOChannel) NameRegistrar.get ("channel."+name));
    }
    public void removeChannel (ISOChannel channel) {
        pool.remove (channel);
    }
    public void removeChannel (String name) throws NameRegistrar.NotFoundException {
        pool.remove ((ISOChannel) NameRegistrar.get ("channel."+name));
    }
    public int size() {
        return pool.size();
    }
    public synchronized ISOChannel getCurrent () throws IOException {
        if (current == null)
            connect();
        else if (!usable)
            reconnect();

        return current;
    }
}

