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

package org.jpos.q2.qbean;

import java.io.IOException;
import java.util.Iterator;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.space.Space;
import org.jpos.space.TransientSpace;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Configurable;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.FilteredChannel;
import org.jpos.util.LogSource;

import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.q2.Q2ConfigurationException;

import org.jdom.Element;

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @jmx:mbean description="ISOChannel wrapper" 
 *                extends="org.jpos.q2.QBeanSupportMBean"
 */
public class ChannelAdaptor 
    extends QBeanSupport
    implements ChannelAdaptorMBean
{
    Space sp;
    Configuration cfg;
    ISOChannel channel;
    String in, out, ready;
    long delay;
    public ChannelAdaptor () {
        super ();
    }
    public void initChannel () 
        throws Q2ConfigurationException, ConfigurationException 
    {
        sp = TransientSpace.getSpace ();
        Element persist = getPersist ();
        Element e = persist.getChild ("channel");
        if (e == null)
            throw new Q2ConfigurationException ("channel element missing");

        in      = persist.getChildTextTrim ("in");
        out     = persist.getChildTextTrim ("out");

        String s = persist.getChildTextTrim ("reconnect-delay");
        delay    = s != null ? Long.parseLong (s) : 10000; // reasonable default

        channel = newChannel (e, getFactory());
        ready   = channel.toString() + ".ready";
    }
    public void startService () {
        try {
            initChannel ();
            new Thread (new Sender ()).start ();
            new Thread (new Receiver ()).start ();
        } catch (Exception e) {
            getLog().warn ("error starting service", e);
        }
    }
    public void stopService () {
        try {
            disconnect();
            sp.out (in, new Object());
        } catch (Exception e) {
            getLog().warn ("error disconnecting from remote host", e);
        }
    }


    /**
     * @jmx:managed-attribute description="set reconnect delay"
     */
    public synchronized void setReconnectDelay (long delay) {
        getPersist().getChild ("reconnect-delay") 
            .setText (Long.toString (delay));
        this.delay = delay;
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="get reconnect delay"
     */
    public long getReconnectDelay () {
        return delay;
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

    public ISOChannel newChannel (Element e, QFactory f) 
	throws Q2ConfigurationException
    {
        String channelName  = e.getAttributeValue ("class");
        String packagerName = e.getAttributeValue ("packager");

        ISOChannel channel   = (ISOChannel) f.newInstance (channelName);
	ISOPackager packager = null;
        if (packagerName != null) {
            packager = (ISOPackager) f.newInstance (packagerName);
            channel.setPackager (packager);
        }
        f.invoke (channel, "setHeader", e.getAttributeValue ("header"));
        f.setLogger        (channel, e);
        f.setConfiguration (channel, e);

        if (channel instanceof FilteredChannel) {
            addFilters ((FilteredChannel) channel, e, f);
        }
        return channel;
    }

    private void addFilters (FilteredChannel channel, Element e, QFactory fact) 
	throws Q2ConfigurationException
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
        }
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
                    Object o = sp.in (in);
                    if (o instanceof ISOMsg)
                        channel.send ((ISOMsg) o);
                } catch (Exception e) { 
                    getLog().warn ("channel-sender-"+in, e);
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
                    sp.out (out, m);
                } catch (Exception e) { 
                    if (running()) {
                        getLog().warn ("channel-receiver-"+out, e);
                        disconnect ();
                    }
                }
            }
        }
    }
    protected void checkConnection () {
        try {
            while (running() && !channel.isConnected ()) {
                while (sp.inp (ready) != null)
                    ;
                channel.connect ();
                if (!channel.isConnected ())
                    ISOUtil.sleep (delay);
            }
            if (running())
                sp.out (ready, new Object ());
        } catch (IOException e) {
            getLog().warn ("check-connection", e);
            ISOUtil.sleep (delay);
        }
    }
    protected void disconnect () {
        try {
            while (sp.inp (ready) != null)
                ;
            channel.disconnect ();
        } catch (IOException e) {
            getLog().warn ("disconnect", e);
        }
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
}

