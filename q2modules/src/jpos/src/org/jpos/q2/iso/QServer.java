/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */
package org.jpos.q2.iso;

import java.util.Set;
import java.util.List;
import java.util.Iterator;

import org.jpos.util.ThreadPool;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ServerChannel;

import org.jpos.q2.QFactory;
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

public class QServer
    extends QBeanSupport
    implements QServerMBean 
{
    private int port = 0;
    private int maxSessions = 100;
    private int minSessions = 1;
    private String channelString, packagerString;
    private ISOChannel channel = null;
    private ISOPackager packager = null;
    private ISOServer server;

    public QServer () {
        super ();
    }

    private void newChannel () throws Q2ConfigurationException {
        Element persist = getPersist ();
        Element e = persist.getChild ("channel");
        if (e == null)
            throw new Q2ConfigurationException ("channel element missing");

        ChannelAdaptor adaptor = new ChannelAdaptor ();
        channel = adaptor.newChannel (e, getFactory ());
    }

    private void initServer () 
        throws Q2ConfigurationException
    {
        if (port == 0)
            throw new Q2ConfigurationException ("Port value not set");
        newChannel();
        if (channel == null)
            throw new Q2ConfigurationException ("ISO Channel is null");

        if (!(channel instanceof ServerChannel)) {
            throw new Q2ConfigurationException (channelString + 
                  "does not implement ServerChannel");
        }

        ThreadPool pool = null;
        pool = new ThreadPool (minSessions ,maxSessions);
        pool.setLogger (log.getLogger(), getName() + ".pool");

        server = new ISOServer (port, (ServerChannel) channel, pool);
        server.setLogger (log.getLogger(), getName() + ".server");
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
    private void addListeners () 
	throws Q2ConfigurationException
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

   
