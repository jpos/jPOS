package org.jpos.q2.qbean;

import java.util.List;
import java.util.Iterator;

import org.jpos.util.ThreadPool;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ServerChannel;

import org.jpos.q2.QBeanSupport;
import org.jpos.q2.Q2ConfigurationException;

import org.jdom.Element;
/**
 * ISO Server wrapper.
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 * @jmx:mbean description="ISOServer wrapper"
 *                  extends="org.jpos.q2.QBeanSupportMBean"
 */

public class ServerAdaptor
    extends QBeanSupport
    implements ServerAdaptorMBean 
{
    private int port = 0;
    private int maxSessions = 100;
    private String channelString, packagerString;
    private ISOChannel channel = null;
    private ISOPackager packager = null;
    private ISOServer server;

    public ServerAdaptor () {
        super ();
    }

    private void newChannel () throws Q2ConfigurationException {
        try {
            Class c = Class.forName (channelString);
            if (c != null) {
                channel = (ISOChannel) c.newInstance ();
                if (packagerString != null) {
                    Class p = Class.forName (packagerString);
                    packager = (ISOPackager) p.newInstance ();
                    channel.setPackager (packager);
                }
            } 
        } catch (ClassNotFoundException e) {
            throw new Q2ConfigurationException (e);
        } catch (InstantiationException e) {
            throw new Q2ConfigurationException (e);
        } catch (IllegalAccessException e) {
            throw new Q2ConfigurationException (e);
        }
    }

    private void initServer () 
        throws Q2ConfigurationException
    {
        if (port == 0)
            throw new Q2ConfigurationException ("Port value not set");
        if (channelString == null)
            throw new Q2ConfigurationException ("Channel name not set");

        newChannel();

        if (channel == null)
            throw new Q2ConfigurationException ("ISO Channel is null");

        if (!(channel instanceof ServerChannel)) {
            throw new Q2ConfigurationException (channelString + "does not implement ServerChannel");
        }

        ThreadPool pool = null;
        pool = new ThreadPool (1,maxSessions);
        pool.setLogger (log.getLogger(), getName() + ".pool");

        ISOServer server = new ISOServer (port, (ServerChannel) channel, pool);
        server.setLogger (log.getLogger(), getName() + ".server");
        
        new Thread (server).start();
    }

    public void startService () {
        try {
            initServer ();
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
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
}

   
