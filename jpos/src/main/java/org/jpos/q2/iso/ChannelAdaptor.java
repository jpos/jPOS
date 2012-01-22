/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.NameRegistrar;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Alejandro Revilla
 */
public class ChannelAdaptor 
    extends QBeanSupport
    implements ChannelAdaptorMBean, Channel, Loggeable
{
    Space sp;
    private ISOChannel channel;
    String in, out, ready, reconnect;
    long delay;
    boolean keepAlive = false;
    boolean ignoreISOExceptions = false;
    boolean writeOnly = false;
    int rx, tx, connects;
    long lastTxn = 0l;
    long timeout = 0l;

    public ChannelAdaptor () {
        super ();
        resetCounters();
    }
    
    public void initService() throws ConfigurationException {
        initSpaceAndQueues();
        NameRegistrar.register (getName(), this);
    }
    public void startService () {
        try {
            channel = initChannel ();
            new Thread (new Sender ()).start ();
            if (!writeOnly) // fixes #426 && jPOS-20
                new Thread (new Receiver ()).start ();
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
    }
    public void stopService () {
        try {
            sp.out (in, new Object());
            if (channel != null && channel.isConnected())
                disconnect();
        } catch (Exception e) {
            getLog().warn ("error disconnecting from remote host", e);
        }
    }
    public void destroyService () {
        NameRegistrar.unregister (getName ());
        NameRegistrar.unregister ("channel." + getName ());
    }

    public synchronized void setReconnectDelay (long delay) {
        getPersist().getChild ("reconnect-delay") 
            .setText (Long.toString (delay));
        this.delay = delay;
        setModified (true);
    }
    public long getReconnectDelay () {
        return delay;
    }
    public synchronized void setInQueue (String in) {
        String old = this.in;
        this.in = in;
        if (old != null)
            sp.out (old, new Object());

        getPersist().getChild("in").setText (in);
        setModified (true);
    }
    public String getInQueue () {
        return in;
    }
    public synchronized void setOutQueue (String out) {
        this.out = out;
        getPersist().getChild("out").setText (out);
        setModified (true);
    }

    /**
     * Queue a message to be transmitted by this adaptor
     * @param m message to send
     */
    public void send (ISOMsg m) {
        sp.out (in, m);
    }
    /**
     * Queue a message to be transmitted by this adaptor
     * @param m message to send
     * @param timeout 
     */
    public void send (ISOMsg m, long timeout) {
        sp.out (in, m, timeout);
    }

    /**
     * Receive message
     */
    public ISOMsg receive () {
        return (ISOMsg) sp.in (out);
    }

    /**
     * Receive message
     * @param timeout time to wait for an incoming message
     */
    public ISOMsg receive (long timeout) {
        return (ISOMsg) sp.in (out, timeout);
    }
    /**
     * @return true if channel is connected
     */
    public boolean isConnected () {
        return sp != null && sp.rdp (ready) != null;
    }

    public String getOutQueue () {
        return out;
    }

    public ISOChannel newChannel (Element e, QFactory f) 
        throws ConfigurationException
    {
        String channelName  = e.getAttributeValue ("class");
        String packagerName = e.getAttributeValue ("packager");

        ISOChannel channel   = (ISOChannel) f.newInstance (channelName);
        ISOPackager packager = null;
        if (packagerName != null) {
            packager = (ISOPackager) f.newInstance (packagerName);
            channel.setPackager (packager);
            f.setConfiguration (packager, e);
        }
        QFactory.invoke (channel, "setHeader", e.getAttributeValue ("header"));
        f.setLogger        (channel, e);
        f.setConfiguration (channel, e);

        if (channel instanceof FilteredChannel) {
            addFilters ((FilteredChannel) channel, e, f);
        }
        if (getName () != null)
            channel.setName (getName ());
        return channel;
    }

    protected void addFilters (FilteredChannel channel, Element e, QFactory fact)
        throws ConfigurationException
    {
        Iterator iter = e.getChildren ("filter").iterator();
        while (iter.hasNext()) {
            Element f = (Element) iter.next();
            String clazz = f.getAttributeValue ("class");
            ISOFilter filter = (ISOFilter) fact.newInstance (clazz);
            fact.setLogger        (filter, f);
            fact.setConfiguration (filter, f);
            String direction = f.getAttributeValue ("direction");
            if (direction == null)
                channel.addFilter (filter);
            else if ("incoming".equalsIgnoreCase (direction))
                channel.addIncomingFilter (filter);
            else if ("outgoing".equalsIgnoreCase (direction))
                channel.addOutgoingFilter (filter);
            else if ("both".equalsIgnoreCase (direction)) {
                channel.addIncomingFilter (filter);
                channel.addOutgoingFilter (filter);
            }
        }
    }
    protected ISOChannel initChannel () throws ConfigurationException {
        Element persist = getPersist ();
        Element e = persist.getChild ("channel");
        if (e == null)
            throw new ConfigurationException ("channel element missing");

        ISOChannel c = newChannel (e, getFactory());
        String socketFactoryString = getSocketFactory();
        if (socketFactoryString != null && c instanceof FactoryChannel) {
            ISOClientSocketFactory sFac = (ISOClientSocketFactory) getFactory().newInstance(socketFactoryString);
            if (sFac != null && sFac instanceof LogSource) {
                ((LogSource) sFac).setLogger(log.getLogger(),getName() + ".socket-factory");
            }
            getFactory().setConfiguration (sFac, e);
            ((FactoryChannel)c).setSocketFactory(sFac);
        }
        return c;
    }
    protected void initSpaceAndQueues () throws ConfigurationException {
        Element persist = getPersist ();
        sp = grabSpace (persist.getChild ("space"));
        in      = persist.getChildTextTrim ("in");
        out     = persist.getChildTextTrim ("out");
        String s = persist.getChildTextTrim ("reconnect-delay");
        delay    = s != null ? Long.parseLong (s) : 10000; // reasonable default
        keepAlive = "yes".equalsIgnoreCase (persist.getChildTextTrim ("keep-alive"));
        ignoreISOExceptions = "yes".equalsIgnoreCase (persist.getChildTextTrim ("ignore-iso-exceptions"));
        writeOnly = "yes".equalsIgnoreCase (getPersist().getChildTextTrim ("write-only"));
        String t = persist.getChildTextTrim("timeout");
        timeout = t != null && t.length() > 0 ? Long.parseLong(t) : 0l;
        ready   = getName() + ".ready";
        reconnect = getName() + ".reconnect";
    }

    public class Sender implements Runnable {
        public Sender () {
            super ();
        }
        public void run () {
            Thread.currentThread().setName ("channel-sender-" + in);
            while (running ()){
                try {
                    checkConnection ();
                    if (!running())
                        break;
                    Object o = sp.in (in, delay);
                    if (o instanceof ISOMsg) {
                        channel.send ((ISOMsg) o);
                        tx++;
                    }
                    else if (keepAlive && channel.isConnected()) {
                        if (channel instanceof BaseChannel) {
                            ((BaseChannel)channel).sendKeepAlive();
                        }
                    }
                } catch (ISOFilter.VetoException e) { 
                    getLog().warn ("channel-sender-"+in, e.getMessage ());
                } catch (ISOException e) {
                    getLog().warn ("channel-sender-"+in, e.getMessage ());
                    if (!ignoreISOExceptions) {
                        disconnect ();
                    }
                    ISOUtil.sleep (1000); // slow down on errors
                } catch (Exception e) { 
                    getLog().warn ("channel-sender-"+in, e.getMessage ());
                    disconnect ();
                    ISOUtil.sleep (1000);
                }
            }
        }
    }
    public class Receiver implements Runnable {
        public Receiver () {
            super ();
        }
        public void run () {
            Thread.currentThread().setName ("channel-receiver-"+out);
            while (running()) {
                try {
                    sp.rd (ready);
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
                            sp.out (reconnect, new Object(), delay);
                            disconnect ();
                            sp.out (in, new Object()); // wake-up Sender
                        }
                        ISOUtil.sleep(1000);
                    }
                } catch (Exception e) { 
                    if (running()) {
                        getLog().warn ("channel-receiver-"+out, e);
                        sp.out (reconnect, new Object(), delay);
                        disconnect ();
                        sp.out (in, new Object()); // wake-up Sender
                        ISOUtil.sleep(1000);
                    }
                }
            }
        }
    }
    protected void checkConnection () {
        while (running() && 
                sp.rdp (reconnect) != null)
        {
            ISOUtil.sleep(1000);
        }
        while (running() && !channel.isConnected ()) {
            while (sp.inp (ready) != null)
                ;
            try {
                channel.connect ();
            } catch (IOException e) {
                getLog().warn ("check-connection", e.getMessage ());
            }
            if (!channel.isConnected ())
                ISOUtil.sleep (delay);
            else
                connects++;
        }
        if (running() && (sp.rdp (ready) == null))
            sp.out (ready, new Date());
    }
    protected synchronized void disconnect () {
        try {
            while (sp.inp (ready) != null)
                ;
            channel.disconnect ();
        } catch (IOException e) {
            getLog().warn ("disconnect", e);
        }
    }
    public synchronized void setHost (String host) {
        setProperty (getProperties ("channel"), "host", host);
        setModified (true);
    }
    public String getHost () {
        return getProperty (getProperties ("channel"), "host");
    }
    public synchronized void setPort (int port) {
        setProperty (
            getProperties ("channel"), "port", Integer.toString (port)
        );
        setModified (true);
    }
    public int getPort () {
        int port = 0;
        try {
            port = Integer.parseInt (
                getProperty (getProperties ("channel"), "port")
            );
        } catch (NumberFormatException e) { }
        return port;
    }
    public synchronized void setSocketFactory (String sFac) {
        setProperty(getProperties("channel"), "socketFactory", sFac);
        setModified(true);
    }

    public void resetCounters () {
        rx = tx = connects = 0;
        lastTxn = 0l;
    }
    public String getCountersAsString () {
        StringBuffer sb = new StringBuffer();
        append (sb, "tx=", tx);
        append (sb, ", rx=", rx);
        append (sb, ", connects=", connects);
        sb.append (", last=");
        sb.append(lastTxn);
        if (lastTxn > 0) {
            sb.append (", idle=");
            sb.append(System.currentTimeMillis() - lastTxn);
            sb.append ("ms");
        }
        return sb.toString();
    }
    public int getTXCounter() {
        return tx;
    }
    public int getRXCounter() {
        return rx;
    }
    public int getConnectsCounter () {
        return connects;
    }
    public long getLastTxnTimestampInMillis() {
        return lastTxn;
    }
    public long getIdleTimeInMillis() {
        return lastTxn > 0L ? System.currentTimeMillis() - lastTxn : -1L;
    }
    public String getSocketFactory() {
        return getProperty(getProperties ("channel"), "socketFactory");
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + getCountersAsString());
    }
    protected Space grabSpace (Element e) {
        return SpaceFactory.getSpace (e != null ? e.getText() : "");
    }
    protected void append (StringBuffer sb, String name, int value) {
        sb.append (name);
        sb.append (value);
    }
}
