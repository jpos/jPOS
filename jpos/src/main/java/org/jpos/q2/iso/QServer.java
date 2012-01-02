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

import java.util.Iterator;

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOServer;
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
/**
 * ISO Server wrapper.
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 * @jmx:mbean description="ISOServer wrapper"
 *                  extends="org.jpos.q2.QBeanSupportMBean"
 */

public class QServer
    extends QBeanSupport
    implements QServerMBean, SpaceListener, ISORequestListener
{
    private int port = 0;
    private int maxSessions = 100;
    private int minSessions = 1;
    private String channelString, packagerString, socketFactoryString;
    private ISOChannel channel = null;
    private ISOPackager packager = null;
    private ISOServer server;
    protected LocalSpace sp;
    private String inQueue;
    private String outQueue;

    public QServer () {
        super ();
    }
    
    public void initService() throws ConfigurationException {
        Element e = getPersist ();
        sp        = grabSpace (e.getChild ("space")); 
    }

    private void newChannel () throws ConfigurationException {
        Element persist = getPersist ();
        Element e = persist.getChild ("channel");
        if (e == null)
            throw new ConfigurationException ("channel element missing");

        ChannelAdaptor adaptor = new ChannelAdaptor ();
        channel = adaptor.newChannel (e, getFactory ());
    }

    private void initServer () 
        throws ConfigurationException
    {
        if (port == 0)
            throw new ConfigurationException ("Port value not set");
        newChannel();
        if (channel == null)
            throw new ConfigurationException ("ISO Channel is null");

        if (!(channel instanceof ServerChannel)) {
            throw new ConfigurationException (channelString + 
                  "does not implement ServerChannel");
        }

        ThreadPool pool = null;
        pool = new ThreadPool (minSessions ,maxSessions);
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
        addListeners ();
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
             * by the our requestListner(this).
             * 
             * Note, if additional ISORequestListeners are registered with the server after
             *  this point, then they won't see anything as our process(ISOSource, ISOMsg)
             *  always return true.
             */
           server.addISORequestListener(this);

        }
    }
    public void startService () {
        try {
            initServer ();
            initIn();
            initOut();
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
    }
    public void stopService () {
        if (server != null)
            server.shutdown ();
    }
    public void destroyService () {
        NameRegistrar.unregister (getName());
        NameRegistrar.unregister ("server." + getName());
    }

    /**
     * @jmx:managed-attribute description="Server port"
     */
    public synchronized void setPort (int port) {
        this.port = port;
        setAttr (getAttrs (), "port", new Integer (port));
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Server port"
     */
    public int getPort () {
        return port;
    }

    /**
     * @jmx:managed-attribute description="Packager"
     */
    public synchronized void setPackager (String packager) {
        packagerString = packager;
        setAttr (getAttrs (), "packager", packagerString);
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Packager"
     */
    public String getPackager () {
        return packagerString;
    }

    /**
     * @jmx:managed-attribute description="Channel"
     */
    public synchronized void setChannel (String channel) {
        channelString = channel;
        setAttr (getAttrs (), "channel", channelString);
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Channel"
     */
    public String getChannel () {
        return channelString;
    }

    /**
     * @jmx:managed-attribute description="Maximum Nr. of Sessions"
     */
    public synchronized void setMaxSessions (int maxSessions) {
        this.maxSessions = maxSessions;
        setAttr (getAttrs (), "maxSessions", new Integer (maxSessions));
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="Maximum Nr. of Sessions"
     */
    public int getMaxSessions () {
        return maxSessions;
    }
    /**
     * @jmx:managed-attribute description="Minimum Nr. of Sessions"
     */
    public synchronized void setMinSessions (int minSessions) {
        this.minSessions = minSessions;
        setAttr (getAttrs (), "minSessions", new Integer (minSessions));
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="Minimum Nr. of Sessions"
     */
    public int getMinSessions () {
        return minSessions;
    }
    /**
     * @jmx:managed-attribute description="Socket Factory" 
     */
    public synchronized void setSocketFactory (String sFactory) {
        socketFactoryString = sFactory;
        setAttr (getAttrs(),"socketFactory", socketFactoryString);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="Socket Factory" 
     */
    public String getSocketFactory() {
        return socketFactoryString;
    }    

    public String getISOChannelNames() {
        return server.getISOChannelNames();
    }
    public String getCountersAsString () {
        return server.getCountersAsString ();
    }
    public String getCountersAsString (String isoChannelName) {
        return server.getCountersAsString (isoChannelName);
    }
    private void addServerSocketFactory () throws ConfigurationException {
        QFactory factory = getFactory ();
        Element persist = getPersist ();
        
        Element serverSocketFactoryElement = persist.getChild ("server-socket-factory");
        
        if (serverSocketFactoryElement != null) {
            ISOServerSocketFactory serverSocketFactory = (ISOServerSocketFactory) factory.newInstance (serverSocketFactoryElement.getAttributeValue ("class"));
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
                factory.newInstance (l.getAttributeValue ("class"));
            factory.setLogger        (listener, l);
            factory.setConfiguration (listener, l);
            server.addISORequestListener (listener);
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
    public void notify(Object key, Object value) {
        Object obj = sp.inp (key);
        if (obj instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) obj;
            try {
                ISOChannel c = server.getLastConnectedISOChannel();
                if (c == null)
                    throw new ISOException ("Server has no active connections");
                if (!c.isConnected())
                    throw new ISOException ("Client disconnected");
                c.send(m);
            } catch (Exception e) { 
                getLog().warn ("notify", e);
            }
        }
    }

    /*
     * This method will be invoke through the ISORequestListener interface, *if*
     * this QServer has an 'out' queue to handle.
     */
    public boolean process(ISOSource source, ISOMsg m) {
        sp.out(outQueue, m);
        return true;
    }

}

   
