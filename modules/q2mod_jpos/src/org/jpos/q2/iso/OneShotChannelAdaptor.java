/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.FactoryChannel;
import org.jpos.iso.Channel;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOClientSocketFactory;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.FilteredChannel;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;

import org.jdom.Element;

/**
 * OneShotChannelAdaptor connects and disconnects a channel for every message
 * exchange.
 * 
 * <p>Example qbean:</p>
 * &lt;client class="org.jpos.q2.iso.OneShotChannelAdaptor" logger="Q2" name="channel-adaptor"&gt;<br>
 *   &lt;channel ...<br>
 *     ...<br>
 *     ...<br>
 *   &lt;/channel&gt;<br>
 *   &lt;max-connections&gt;5&lt;/max-connections&gt;<br>
 *   &lt;max-connect-attempts&gt;15&lt;/max-connect-attempts&gt;<br>
 *   &lt;in&gt;send&lt;/in&gt;<br>
 *   &lt;out&gt;receive&lt;/out&gt;<br>
 * &lt;/client&gt;<br>
 *
 * @author Alejandro Revilla
 * @author Thomas L. Kjeldsen
 * @version $Revision$ $Date$
 * @jmx:mbean description="ISOChannel wrapper" 
 *                extends="org.jpos.q2.QBeanSupportMBean"
 */
public class OneShotChannelAdaptor 
    extends QBeanSupport
    implements OneShotChannelAdaptorMBean, Channel
{
    Space sp;
    Configuration cfg;
    String in, out;
    long delay;
    int maxConnections;
    int maxConnectAttempts;
    public OneShotChannelAdaptor () {
        super ();
    }

    private Space grabSpace (Element e) {
        return SpaceFactory.getSpace (e != null ? e.getText() : "");
    }

    public void initAdaptor() {
        Element persist = getPersist ();
        sp = grabSpace (persist.getChild ("space")); 
        in = persist.getChildTextTrim ("in");
        out = persist.getChildTextTrim ("out");
        delay = 5000;

        String s = persist.getChildTextTrim ("max-connections");
        maxConnections = (s!=null) ? Integer.parseInt(s) : 1;  // reasonable default
        s = persist.getChildTextTrim ("max-connect-attempts");
        maxConnectAttempts = (s!=null) ? Integer.parseInt(s) : 15;  // reasonable default
    }
    public void startService () {
        try {
            initAdaptor();
            for (int i=0; i<maxConnections; i++) {
                Worker w = new Worker(i);
                w.initChannel();
                (new Thread(w)).start();
            }
            NameRegistrar.register (getName(), this);
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
    }
    public void stopService () {
        try {
            for (int i=0; i<maxConnections; i++) {
                sp.out(in, new Object());
            }
        } catch (Exception e) {
            getLog().warn ("error stopping service", e);
        }
    }
    public void destroyService () {
        NameRegistrar.unregister (getName ());
        NameRegistrar.unregister ("channel." + getName ());
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

    public class Worker implements Runnable {
        ISOChannel channel;
        int id;
        public Worker (int i) {
            super ();
            id = i;
        }
        public void run () {
            Thread.currentThread().setName ("channel-worker-" + id);
            while (running ()){
                try {
                    Object o = sp.in (in, delay);
                    if (o instanceof ISOMsg) {
                        for (int i=0; !channel.isConnected() 
                                && i<maxConnectAttempts; i++) 
                        {
                            channel.reconnect();
                            if (!channel.isConnected())
                                ISOUtil.sleep (1000L);
                        }
                        if (channel.isConnected()) {
                            channel.send ((ISOMsg) o);
                            ISOMsg m = channel.receive();
                            channel.disconnect();
                            sp.out (out, m);
                        }
                    }
                } catch (Exception e) { 
                    getLog().warn ("channel-worker-"+id, e.getMessage ());
                    ISOUtil.sleep (1000);
                } finally {
                    try {
                        channel.disconnect();
                    } catch (Exception e) {
                        getLog().warn ("channel-worker-"+id, e.getMessage ());
                    }
                }
            }
        }

        public void initChannel () throws ConfigurationException {
            Element persist = getPersist ();
            Element e = persist.getChild ("channel");
            if (e == null)
                throw new ConfigurationException ("channel element missing");

            channel = newChannel (e, getFactory());
            
            String socketFactoryString = getSocketFactory();
            if (socketFactoryString != null && channel instanceof FactoryChannel) {
                ISOClientSocketFactory sFac = (ISOClientSocketFactory) getFactory().newInstance(socketFactoryString);
                if (sFac != null && sFac instanceof LogSource) {
                    ((LogSource) sFac).setLogger(log.getLogger(),getName() + ".socket-factory");
                }
                getFactory().setConfiguration (sFac, e);
                ((FactoryChannel)channel).setSocketFactory(sFac);
            }

        }
        private ISOChannel newChannel (Element e, QFactory f) 
            throws ConfigurationException
        {
            String channelName  = e.getAttributeValue ("class");
            if (channelName == null)
                throw new ConfigurationException ("class attribute missing from channel element.");
            
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
                channel.setName (getName ()+id);
            return channel;
        }

        private void addFilters (FilteredChannel channel, Element e, QFactory fact) 
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
    
    }

    /**
     * @jmx:managed-attribute description="input queue"
     */
    public synchronized void setInQueue (String in) {
        String old = this.in;
        this.in = in;
        if (old != null)
            sp.out (old, new Object());

        getPersist().getChild("in").setText (in);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="input queue"
     */
    public String getInQueue () {
        return in;
    }
    /**
     * @jmx:managed-attribute description="output queue"
     */
    public synchronized void setOutQueue (String out) {
        this.out = out;
        getPersist().getChild("out").setText (out);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="output queue"
     */
    public String getOutQueue () {
        return out;
    }
    /**
     * @jmx:managed-attribute description="remote host address"
     */
    public synchronized void setHost (String host) {
        setProperty (getProperties ("channel"), "host", host);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="remote host address"
     */
    public String getHost () {
        return getProperty (getProperties ("channel"), "host");
    }
    /**
     * @jmx:managed-attribute description="remote port"
     */
    public synchronized void setPort (int port) {
        setProperty (
            getProperties ("channel"), "port", Integer.toString (port)
        );
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="remote port"
     */
    public int getPort () {
        int port = 0;
        try {
            port = Integer.parseInt (
                getProperty (getProperties ("channel"), "port")
            );
        } catch (NumberFormatException e) { }
        return port;
    }
    /**
     * @jmx:managed-attribute description="socket factory" 
     */
    public synchronized void setSocketFactory (String sFac) {
        setProperty(getProperties("channel"), "socketFactory", sFac);
        setModified(true);
    }
    /**
     * @jmx:managed-attribute description="socket factory" 
     */
    public String getSocketFactory() {
        return getProperty(getProperties ("channel"), "socketFactory");
    }
}

