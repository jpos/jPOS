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

package org.jpos.iso.channel;

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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public class ChannelPool implements ISOChannel, LogSource, Configurable, Cloneable {
    boolean usable = true;
    String name = "";
    protected Logger logger;
    protected String realm;
    Configuration cfg = null;
    List<ISOChannel> pool;
    ISOChannel current;

    public ChannelPool () {
        super ();
        pool = new CopyOnWriteArrayList<>();
    }

    @Override
    public void setPackager(ISOPackager p) {
        // nothing to do
    }

    @Override
    public synchronized void connect() throws IOException {
        current = null;
        LogEvent evt = new LogEvent (this, "connect");
        evt.addMessage ("pool-size=" + Integer.toString (pool.size()));
        for (ISOChannel c : pool) {
            try {
                evt.addMessage ("pool-" + Integer.toString(pool.indexOf(c)));
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

    @Override
    public synchronized void disconnect() {
        current = null;
        LogEvent evt = new LogEvent (this, "disconnect");
        for (ISOChannel c : pool) {
            try {
                c.disconnect();
            } catch (IOException e) {
                evt.addMessage(e);
            }
        }
        Logger.log (evt);
    }

    @Override
    public synchronized void reconnect() throws IOException {
        disconnect ();
        connect ();
    }

    @Override
    public synchronized boolean isConnected() {
        try {
            return getCurrent().isConnected ();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public ISOMsg receive() throws IOException, ISOException {
        return getCurrent().receive ();
    }

    @Override
    public void send(ISOMsg m) throws IOException, ISOException {
        getCurrent().send (m);
    }

    @Override
    public void send(byte[] b) throws IOException, ISOException {
        getCurrent().send (b);
    }

    @Override
    public void setUsable(boolean b) {
        this.usable = b;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        NameRegistrar.register ("channel."+name, this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ISOPackager getPackager() {
        return null;
    }

    @Override
    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public synchronized void setConfiguration(Configuration cfg)
        throws ConfigurationException
    {
        this.cfg = cfg;
        String channelName[] = cfg.getAll ("channel");
        for (String aChannelName : channelName) {
            try {
                addChannel(aChannelName);
            } catch (NameRegistrar.NotFoundException e) {
                throw new ConfigurationException(e);
            }
        }
    }
    public void addChannel (ISOChannel channel) {
        pool.add (channel);
    }
    public void addChannel (String name) 
        throws NameRegistrar.NotFoundException
    {
        pool.add (NameRegistrar.get ("channel."+name));
    }
    public void removeChannel (ISOChannel channel) {
        pool.remove (channel);
    }
    public void removeChannel (String name) throws NameRegistrar.NotFoundException {
        @SuppressWarnings("unchecked")
        ISOChannel ch = (ISOChannel) NameRegistrar.get("channel."+name);
        pool.remove(ch);
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

    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new InternalError();
      }
    }

}

