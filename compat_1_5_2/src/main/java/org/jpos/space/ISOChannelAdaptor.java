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

package org.jpos.space;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogSource;

import java.io.IOException;


/**
 * Integrates legacy (pre 2.0) ISOChannels to new Space based architecture
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
public class ISOChannelAdaptor
        extends SimpleLogSource 
        implements Configurable
{
    Space sp;
    Configuration cfg;
    ISOChannel channel;
    String to, from, ready;
    public ISOChannelAdaptor () {
        super ();
        sp = TransientSpace.getSpace ();
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        this.cfg = cfg;
        try {
            to   = get ("to");
            from = get ("from");
            channel = (ISOChannel) NameRegistrar.get (
                "channel." + get ("channel")
            );
            ready  = channel.toString() + ".ready";
            new Thread (new Sender ()).start ();
            new Thread (new Receiver ()).start ();
        } catch (NameRegistrar.NotFoundException e) {
            throw new ConfigurationException (e);
        }
    }
    @SuppressWarnings("unchecked")
    public class Sender implements Runnable {
        public Sender () {
            super ();
        }
        public void run () {
            Thread.currentThread().setName ("channel-sender-" + to);
            for (;;) {
                try {
                    checkConnection ();
                    Object o = sp.in (to, 10000);
                    if (o instanceof ISOMsg)
                        channel.send ((ISOMsg) o);
                } catch (Exception e) { 
                    Logger.log (
                        new LogEvent (
                            ISOChannelAdaptor.this, "channel-sender"+to, e
                        )
                    );
                    ISOUtil.sleep (1000);
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    public class Receiver implements Runnable {
        public Receiver () {
            super ();
        }
        public void run () {
            Thread.currentThread().setName ("channel-receiver-"+from);
            for (;;) {
                try {
                    sp.rd (ready);
                    ISOMsg m = channel.receive ();
                    sp.out (from, m);
                } catch (Exception e) { 
                    Logger.log (
                        new LogEvent (
                            ISOChannelAdaptor.this, "channel-receiver-"+from, e
                        )
                    );
                    disconnect ();
                }
            }
        }
    }
    protected void checkConnection () {
        try {
            while (!channel.isConnected ()) {
                while (sp.inp (ready) != null)
                    ;
                channel.connect ();
                if (!channel.isConnected ())
                    ISOUtil.sleep (10000);
            }
            sp.out (ready, new Object ());
        } catch (IOException e) {
            Logger.log (
                new LogEvent (
                    ISOChannelAdaptor.this, "check-connection", e
                )
            );
            ISOUtil.sleep (10000);
        }
    }
    protected void disconnect () {
        try {
            while (sp.inp (ready) != null)
                ;
            channel.disconnect ();
        } catch (IOException e) {
            Logger.log (
                new LogEvent (
                    ISOChannelAdaptor.this, "disconnect", e
                )
            );
        }
    }
    private String get (String prop) throws ConfigurationException
    {
        String value = cfg.get (prop);
        if (value == null)
            throw new ConfigurationException ("null property "+prop);
        return value;
    }
}

