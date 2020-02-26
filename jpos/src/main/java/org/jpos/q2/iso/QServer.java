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
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOServerEventListener;
import org.jpos.iso.ISOServerSocketFactory;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ServerChannel;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.LocalSpace;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceListener;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ISO Server wrapper.
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 */

@SuppressWarnings("unchecked")
public class QServer
    extends QBeanSupport
    implements QServerMBean, SpaceListener, ISORequestListener
{
    private int port = 0;
    private int maxSessions = 100;
    private int minSessions = 1;
    private String channelString, packagerString, socketFactoryString;
    private ISOChannel channel = null;
    private ISOServer server;
    protected LocalSpace sp;
    private String inQueue;
    private String outQueue;
    private String sendMethod;
    AtomicInteger msgn = new AtomicInteger();
    public QServer () {
        super ();
    }

    @Override
    public void initService() throws ConfigurationException {
        Element e = getPersist ();
        sp        = grabSpace (e.getChild ("space"));
    }

    private void newChannel () throws ConfigurationException {
        Element persist = getPersist ();
        Element e = persist.getChild ("channel");
        if (e == null) {
            throw new ConfigurationException ("channel element missing");
        }

        ChannelAdaptor adaptor = new ChannelAdaptor ();
        channel = adaptor.newChannel (e, getFactory ());
    }

    private void initServer ()
        throws ConfigurationException
    {
        if (port == 0) {
            throw new ConfigurationException ("Port value not set");
        }
        newChannel();
        if (channel == null) {
            throw new ConfigurationException ("ISO Channel is null");
        }

        if (!(channel instanceof ServerChannel)) {
            throw new ConfigurationException (channelString +
                  "does not implement ServerChannel");
        }

        ThreadPool pool = null;
        pool = new ThreadPool (minSessions ,maxSessions, getName() + "-ThreadPool");
        pool.setLogger (log.getLogger(), getName() + ".pool");

        server = new ISOServer (port, (ServerChannel) channel, pool);
        server.setLogger (log.getLogger(), getName() + ".server");
        server.setName (getName ());
        if (socketFactoryString != null) {
            ISOServerSocketFactory sFac = (ISOServerSocketFactory) getFactory().newInstance(socketFactoryString);
            if (sFac != null && sFac instanceof LogSource) {
                ((LogSource) sFac).setLogger(log.getLogger(),getName() + ".socket-factory");
            }
            server.setSocketFactory(sFac);
        }
        getFactory().setConfiguration (server, getPersist());
        addServerSocketFactory();
        addListeners ();// ISORequestListener
        addISOServerConnectionListeners();
        NameRegistrar.register (getName(), this);
        new Thread (server).start();
    }
    private void initIn() {
        Element persist = getPersist();
        inQueue = persist.getChildText("in");
        if (inQueue != null) {
            /*
             * We have an 'in' queue to monitor for messages we will
             *  send out through server in our (SpaceListener)notify(Object, Object) method.
             */

            sp.addListener(inQueue, this);
        }
    }
    private void initOut() {
        Element persist = getPersist();
        outQueue = persist.getChildText("out");
        if (outQueue != null) {
            /*
             * We have an 'out' queue to send any messages to that are received
             * by the our requestListener(this).
             *
             * Note, if additional ISORequestListeners are registered with the server after
             *  this point, then they won't see anything as our process(ISOSource, ISOMsg)
             *  always return true.
             */
           server.addISORequestListener(this);
        }
    }
    @Override
    public void startService () {
        try {
            initServer ();
            initIn();
            initOut();
            initWhoToSendTo();
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
    }
    private void initWhoToSendTo() {

        Element persist = getPersist();
        sendMethod = persist.getChildText("send-request");
        if (sendMethod==null) {
            sendMethod="LAST";
        }


    }

    @Override
    public void stopService () {
        if (server != null) {
            server.shutdown ();
            sp.removeListener(inQueue, this);
        }
    }
    @Override
    public void destroyService () {
        NameRegistrar.unregister (getName());
        NameRegistrar.unregister ("server." + getName());
    }

    @Override
    public synchronized void setPort (int port) {
        this.port = port;
        setAttr (getAttrs (), "port", port);
        setModified (true);
    }

    @Override
    public int getPort () {
        return port;
    }

    @Override
    public synchronized void setPackager (String packager) {
        packagerString = packager;
        setAttr (getAttrs (), "packager", packagerString);
        setModified (true);
    }

    @Override
    public String getPackager () {
        return packagerString;
    }

    @Override
    public synchronized void setChannel (String channel) {
        channelString = channel;
        setAttr (getAttrs (), "channel", channelString);
        setModified (true);
    }

    @Override
    public String getChannel () {
        return channelString;
    }

    @Override
    public synchronized void setMaxSessions (int maxSessions) {
        this.maxSessions = maxSessions;
        setAttr (getAttrs (), "maxSessions", maxSessions);
        setModified (true);
    }

    @Override
    public int getMaxSessions () {
        return maxSessions;
    }

    @Override
    public synchronized void setMinSessions (int minSessions) {
        this.minSessions = minSessions;
        setAttr (getAttrs (), "minSessions", minSessions);
        setModified (true);
    }

    @Override
    public int getMinSessions () {
        return minSessions;
    }

    @Override
    public synchronized void setSocketFactory (String sFactory) {
        socketFactoryString = sFactory;
        setAttr (getAttrs(),"socketFactory", socketFactoryString);
        setModified (true);
    }

    @Override
    public String getSocketFactory() {
        return socketFactoryString;
    }

    @Override
    public String getISOChannelNames() {
        return server.getISOChannelNames();
    }

    public ISOServer getISOServer() {
        return server;
    }
    
    @Override
    public String getCountersAsString () {
        return server.getCountersAsString ();
    }
    @Override
    public String getCountersAsString (String isoChannelName) {
        return server.getCountersAsString (isoChannelName);
    }
    private void addServerSocketFactory () throws ConfigurationException {
        QFactory factory = getFactory ();
        Element persist = getPersist ();

        Element serverSocketFactoryElement = persist.getChild ("server-socket-factory");

        if (serverSocketFactoryElement != null) {
            ISOServerSocketFactory serverSocketFactory = (ISOServerSocketFactory) factory.newInstance (
                QFactory.getAttributeValue (serverSocketFactoryElement, "class"));
            factory.setLogger        (serverSocketFactory, serverSocketFactoryElement);
            factory.setConfiguration (serverSocketFactory, serverSocketFactoryElement);
            server.setSocketFactory(serverSocketFactory);
        }

    }

    private void addListeners ()
        throws ConfigurationException
    {
        QFactory factory = getFactory ();
        Iterator iter = getPersist().getChildren (
            "request-listener"
        ).iterator();
        while (iter.hasNext()) {
            Element l = (Element) iter.next();
            ISORequestListener listener = (ISORequestListener)
                factory.newInstance (QFactory.getAttributeValue (l, "class"));
            factory.setLogger        (listener, l);
            factory.setConfiguration (listener, l);
            server.addISORequestListener (listener);
        }
    }

    private void addISOServerConnectionListeners()
         throws ConfigurationException
    {

        QFactory factory = getFactory ();
        Iterator iter = getPersist().getChildren (
            "connection-listener"
        ).iterator();
        while (iter.hasNext()) {
            Element l = (Element) iter.next();
            ISOServerEventListener listener = (ISOServerEventListener)
                factory.newInstance (QFactory.getAttributeValue (l, "class"));
            factory.setLogger        (listener, l);
            factory.setConfiguration (listener, l);
            server.addServerEventListener(listener);
        }
    }


    private LocalSpace grabSpace (Element e) throws ConfigurationException
    {
        String uri = e != null ? e.getText() : "";
        Space sp = SpaceFactory.getSpace (uri);
        if (sp instanceof LocalSpace) {
            return (LocalSpace) sp;
        }
        throw new ConfigurationException ("Invalid space " + uri);
    }

    /*
     * This method will be invoked through the SpaceListener interface we registered once
     * we noticed we had an 'in' queue.
     */
    @Override
    public void notify(Object key, Object value) {
        Object obj = sp.inp(key);
        if (obj instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) obj;
            if ("LAST".equals(sendMethod)) {
                try {
                    ISOChannel c = server.getLastConnectedISOChannel();
                    if (c == null) {
                        throw new ISOException("Server has no active connections");
                    }
                    if (!c.isConnected()) {
                        throw new ISOException("Client disconnected");
                    }
                    c.send(m);
                }
                catch (Exception e) {
                    getLog().warn("notify", e);
                }
            }
            else if ("ALL".equals(sendMethod)) {
                String channelNames = getISOChannelNames();
                if (channelNames != null) {
                    StringTokenizer tok = new StringTokenizer(channelNames, " ");
                    while (tok.hasMoreTokens()) {
                        try {
                            ISOChannel c = server.getISOChannel(tok.nextToken());
                            if (c == null) {
                                throw new ISOException("Server has no active connections");
                            }
                            if (!c.isConnected()) {
                                throw new ISOException("Client disconnected");
                            }
                            c.send(m);
                        } catch (Exception e) {
                            getLog().warn("notify", e);
                        }
                    }
                }
            }
            else if ("RR".equals(sendMethod)) {
                String channelNames = getISOChannelNames();
                int i =0;
                String[] channelName;
                if (channelNames != null) {
                    StringTokenizer tok = new StringTokenizer(channelNames, " ");
                    channelName = new String[tok.countTokens()];
                    while (tok.hasMoreTokens()) {
                        channelName[i] = tok.nextToken();
                        i++;
                    }
                    try {
                        ISOChannel c = server.getISOChannel(channelName[msgn.incrementAndGet() % channelName.length]);
                        if (c == null) {
                            throw new ISOException("Server has no active connections");
                        }
                        if (!c.isConnected()) {
                            throw new ISOException("Client disconnected");
                        }
                        c.send(m);
                    } catch (Exception e) {
                        getLog().warn("notify", e);
                    }
                }
            }
        }
    }

    /*
     * This method will be invoke through the ISORequestListener interface, *if*
     * this QServer has an 'out' queue to handle.
     */
    @Override
    public boolean process(ISOSource source, ISOMsg m) {
        sp.out(outQueue, m);
        return true;
    }

}


