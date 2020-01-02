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
import org.jpos.core.Environment;
import org.jpos.iso.*;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;

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
 *
 */
public class OneShotChannelAdaptor 
    extends QBeanSupport
    implements OneShotChannelAdaptorMBean, Channel
{
    Space<String,Object> sp;
    String in, out;
    long delay;
    int maxConnections;
    int maxConnectAttempts;
    public OneShotChannelAdaptor () {
        super ();
    }

    @SuppressWarnings("unchecked")
    private Space<String,Object> grabSpace (Element e) {
        return (Space<String,Object>) SpaceFactory.getSpace (e != null ? e.getText() : "");
    }

    public void initAdaptor() {
        Element persist = getPersist ();
        sp = grabSpace (persist.getChild ("space"));
        in = Environment.get(persist.getChildTextTrim ("in"));
        out = Environment.get(persist.getChildTextTrim ("out"));
        delay = 5000;

        String s = Environment.get(persist.getChildTextTrim ("max-connections"));
        maxConnections = s!=null ? Integer.parseInt(s) : 1;  // reasonable default
        s = Environment.get(persist.getChildTextTrim ("max-connect-attempts"));
        maxConnectAttempts = s!=null ? Integer.parseInt(s) : 15;  // reasonable default
    }
    public void startService () {
        try {
            initAdaptor();
            for (int i=0; i<maxConnections; i++) {
                Worker w = new Worker(i);
                w.initChannel();
                new Thread(w).start();
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
     * @param timeout in millis
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
            int[] handbackFields = cfg.getInts ("handback-field");
            while (running ()){
                try {
                    Object o = sp.in (in, delay);
                    if (o instanceof ISOMsg) {
                        ISOMsg m = (ISOMsg) o;
                        ISOMsg handBack = null;
                        if (handbackFields.length > 0)
                            handBack = (ISOMsg) m.clone (handbackFields);
                        for (int i=0; !channel.isConnected() 
                                && i<maxConnectAttempts; i++) 
                        {
                            channel.reconnect();
                            if (!channel.isConnected())
                                ISOUtil.sleep (1000L);
                        }
                        if (channel.isConnected()) {
                            channel.send (m);
                            m = channel.receive();
                            channel.disconnect();
                            if (handBack != null)
                                m.merge (handBack);
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
            String channelName  = QFactory.getAttributeValue (e, "class");
            if (channelName == null)
                throw new ConfigurationException ("class attribute missing from channel element.");
            
            String packagerName = QFactory.getAttributeValue (e, "packager");

            ISOChannel channel   = (ISOChannel) f.newInstance (channelName);
            ISOPackager packager;
            if (packagerName != null) {
                packager = (ISOPackager) f.newInstance (packagerName);
                channel.setPackager (packager);
                f.setConfiguration (packager, e);
            }
            QFactory.invoke (channel, "setHeader", QFactory.getAttributeValue (e, "header"));
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
            for (Object o : e.getChildren("filter")) {
                Element f = (Element) o;
                String clazz = QFactory.getAttributeValue(f, "class");
                ISOFilter filter = (ISOFilter) fact.newInstance(clazz);
                fact.setLogger(filter, f);
                fact.setConfiguration(filter, f);
                String direction = QFactory.getAttributeValue(f, "direction");
                if (direction == null)
                    channel.addFilter(filter);
                else if ("incoming".equalsIgnoreCase(direction))
                    channel.addIncomingFilter(filter);
                else if ("outgoing".equalsIgnoreCase(direction))
                    channel.addOutgoingFilter(filter);
                else if ("both".equalsIgnoreCase(direction)) {
                    channel.addIncomingFilter(filter);
                    channel.addOutgoingFilter(filter);
                }
            }
        }
    
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

    public String getOutQueue () {
        return out;
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
        } catch (NumberFormatException e) {
            getLog().error (e);
        }
        return port;
    }
    public synchronized void setSocketFactory (String sFac) {
        setProperty(getProperties("channel"), "socketFactory", sFac);
        setModified(true);
    }
    public String getSocketFactory() {
        return getProperty(getProperties ("channel"), "socketFactory");
    }
}

