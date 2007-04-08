/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.q2.iso;

import java.util.Iterator;

import org.jpos.util.LogSource;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOServerSocketFactory;
import org.jpos.iso.ServerChannel;

import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.core.ConfigurationException;

import org.jdom.Element;
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
    implements QServerMBean 
{
    private int port = 0;
    private int maxSessions = 100;
    private int minSessions = 1;
    private String channelString, packagerString, socketFactoryString;
    private ISOChannel channel = null;
    private ISOPackager packager = null;
    private ISOServer server;

    public QServer () {
        super ();
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
        addListeners ();
        new Thread (server).start();
    }
    public void startService () {
        try {
            initServer ();
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
    }
    public void stopService () {
        NameRegistrar.unregister ("server." + getName());
        server.shutdown ();
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

}

   
