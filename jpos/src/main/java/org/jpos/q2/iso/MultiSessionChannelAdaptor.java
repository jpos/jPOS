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

package org.jpos.q2.iso;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.space.SpaceUtil;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.NameRegistrar;

import java.io.IOException;
import java.util.Date;

/**
 * @author apr
 * @since 1.8.5
 */
@SuppressWarnings({"unused", "unchecked"})
public class MultiSessionChannelAdaptor 
    extends ChannelAdaptor
    implements MultiSessionChannelAdaptorMBean, Channel, Loggeable
{
    int sessions = 1;
    ISOChannel[] channels;
    int roundRobinCounter = 0;

    public MultiSessionChannelAdaptor () {
        super ();
        resetCounters();
    }
    public void initService() throws ConfigurationException {
        initSpaceAndQueues();
        NameRegistrar.register (getName(), this);
    }
    public void startService () {
        try {
            channels = new ISOChannel[sessions];
            for (int i=0; i<sessions; i++) {
                ISOChannel c = initChannel();
                if (c instanceof LogSource) {
                    LogSource ls = (LogSource) c;
                    ls.setLogger(ls.getLogger(), ls.getRealm()+"-"+i);

                }
                channels[i] = c;
                if (!writeOnly)
                    new Thread (new Receiver (i), "channel-receiver-" + in + "-" + i).start ();
            }
            new Thread (new Sender (), "channel-sender-" + in).start ();
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
    }

    public int getSessions() {
        return sessions;
    }

    public void setSessions(int sessions) {
        this.sessions = sessions;
    }
    @SuppressWarnings("unchecked")
    public class Sender implements Runnable {
        public Sender () {
            super ();
        }
        public void run () {
            while (running ()){
                ISOChannel channel = null;
                try {
                    if (!running())
                        break;
                    if (sp.rd(ready, delay) == null)
                        continue;
                    Object o = sp.in (in, delay);
                    channel = getNextChannel(); // we want to call getNextChannel even if o is null so that
                                                // it can pull the 'ready' indicator.
                    if (o instanceof ISOMsg && channel != null) {
                        channel.send ((ISOMsg) o);
                        tx++;
                    }
                } catch (ISOFilter.VetoException e) { 
                    getLog().warn ("channel-sender-"+in, e.getMessage ());
                } catch (ISOException e) {
                    getLog().warn ("channel-sender-"+in, e.getMessage ());
                    if (!ignoreISOExceptions) {
                        disconnect (channel);
                    }
                    ISOUtil.sleep (1000); // slow down on errors
                } catch (Exception e) { 
                    getLog().warn ("channel-sender-"+in, e.getMessage ());
                    disconnect (channel);
                    ISOUtil.sleep (1000);
                }
            }
            disconnectAll();
        }
    }
    @SuppressWarnings("unchecked")
    public class Receiver implements Runnable {
        int slot;
        ISOChannel channel;
        public Receiver (int slot) {
            super ();
            this.channel = channels[slot];
            this.slot = slot;
        }
        public void run () {
            ISOUtil.sleep(slot*10); // we don't want to blast a server at startup
            while (running()) {
                try {
                    if (!channel.isConnected()) {
                        connect(slot);
                        if (!channel.isConnected()) {
                            ISOUtil.sleep(delay);
                            continue;
                        }
                    }
                    ISOMsg m = channel.receive ();
                    rx++;
                    lastTxn = System.currentTimeMillis();
                    if (timeout > 0)
                        sp.out (out, m, timeout);
                    else
                        sp.out (out, m);
                } catch (ISOException e) {
                    if (running()) {
                        getLog().warn ("channel-receiver-"+out, e);
                        if (!ignoreISOExceptions) {
                            disconnect (channel);
                        }
                        ISOUtil.sleep(1000);
                    }
                } catch (Exception e) { 
                    if (running()) {
                        getLog().warn("channel-receiver-" + out, e);
                        disconnect (channel);
                        ISOUtil.sleep(1000);
                    }
                }
            }
        }
    }
    @Override
    protected void initSpaceAndQueues () throws ConfigurationException {
        super.initSpaceAndQueues();
        Element persist = getPersist ();
        String s = persist.getChildTextTrim("sessions");
        setSessions(s != null && s.length() > 0 ? Integer.parseInt(s) : 1);
    }

    private void connect (int slot) {
        ISOChannel c = channels[slot];
        if (c != null && !c.isConnected()) {
            try {
                c.connect ();
                sp.put (ready, new Date());
            } catch (IOException e) {
                getLog().warn ("check-connection(" + slot + ") " + c.toString(), e.getMessage ());
            }
        }
    }
    private void disconnect (ISOChannel channel) {
        try {
            if (getConnectedCount() <= 1)
                SpaceUtil.wipe(sp, ready);
            channel.disconnect ();
        } catch (IOException e) {
            getLog().warn ("disconnect", e);
        }
    }
    private void disconnectAll() {
        for (ISOChannel channel : channels) disconnect(channel);
    }
    private ISOChannel getNextChannel() {
        ISOChannel c = null;
        for (int size = channels.length; size > 0; size--) {
            c = channels[roundRobinCounter++ % channels.length];
            if (c != null && c.isConnected())
                break;
        }
        if (c == null)
            SpaceUtil.wipe(sp, ready);
        return c;
    }
    private int getConnectedCount() {
        int connected = 0;
        for (ISOChannel c : channels) {
            if (c != null && c.isConnected()) {
                connected++;
            }
        }
        return connected;
    }
}
