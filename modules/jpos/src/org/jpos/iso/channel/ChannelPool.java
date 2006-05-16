/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso.channel;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

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

