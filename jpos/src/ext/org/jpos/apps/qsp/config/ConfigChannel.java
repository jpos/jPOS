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

package org.jpos.apps.qsp.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.management.NotCompliantMBeanException;
import javax.swing.JPanel;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPReConfigurator;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOClientSocketFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.gui.ISOChannelPanel;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configure channel
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigChannel implements QSPReConfigurator {
    private String [] attributeNames = {  
                    "class",
                    "packager",
                    "packager-config",
                    "packager-logger",
                    "packager-realm",
                    "header",
                    "host",
                    "port",
                    "timeout",
                    "socket-factory",
                    "scroll",
                    "refresh",
                    "name"  // used here to update digest
                };

    public void config (QSP qsp, Node node) throws ConfigurationException
    {
        ISOChannel channel;
        LogEvent evt = new LogEvent (qsp, "config-channel");
        String name = node.getAttributes().getNamedItem ("name").getNodeValue();
        try {
            channel = getChannel (name);
        } catch (NameRegistrar.NotFoundException e) {
            channel = createChannel (name, node, evt);
            channel.setName (name);
        }
        try {
            qsp.registerMBean (channel, "type=channel,name="+name);
        } catch (NotCompliantMBeanException e) {
            evt.addMessage (e.getMessage());
        } catch (Exception e) {
            evt.addMessage (e);
            throw new ConfigurationException (e);
        } finally {
            Logger.log (evt);
        }
    }
    public void reconfig (QSP qsp, Node node) throws ConfigurationException
    {
        String name = node.getAttributes().getNamedItem ("name").getNodeValue();
        LogEvent evt = new LogEvent (qsp, "re-config-channel", name);
        try {
            ISOChannel channel = getChannel (name);

            byte[] digest = (byte[])NameRegistrar.get (name + ".digest");
            byte[] previousDigest = new byte [digest.length];
            System.arraycopy (digest, 0, previousDigest, 0, digest.length);
            Properties props = ConfigUtil.addAttributes (
                node, attributeNames, null, evt
            );
            digest = (byte[]) props.get (ConfigUtil.DIGEST_PROPERTY);
            NameRegistrar.register (name + ".digest", digest);

            if (channel instanceof LogSource)
                ((LogSource)channel).setLogger (
                    ConfigLogger.getLogger (node),
                    ConfigLogger.getRealm  (node)
                );
            Configuration cfg = new SimpleConfiguration (
                ConfigUtil.addProperties (node, props, evt)
            );
            if (channel instanceof ReConfigurable) {
                ((Configurable)channel).setConfiguration (cfg);
            }
            ISOPackager p = channel.getPackager();
            if (p instanceof ReConfigurable) {
                ((Configurable)p).setConfiguration (cfg);
            }
            if (Arrays.equals (previousDigest, digest)) {
                evt.addMessage ("<unchanged/>");
                return;
            }
            evt.addMessage ("<modified/>");

            if (cfg.get ("timeout", null) != null)
                ConfigUtil.invoke (
                    channel, "setTimeout", new Integer(cfg.getInt ("timeout"))
                );

            ConfigUtil.invoke (channel, "setHeader", cfg.get ("header", null));
        } catch (NameRegistrar.NotFoundException e) {
            evt.addMessage (e);
        } finally {
            Logger.log (evt);
        }
    }

    private ISOChannel createChannel (String name, Node node, LogEvent evt)
        throws ConfigurationException
    {
        Properties props = ConfigUtil.addAttributes
            (node, attributeNames, null, evt);

        NameRegistrar.register 
            (name + ".digest", props.get (ConfigUtil. DIGEST_PROPERTY));

        Logger logger = ConfigLogger.getLogger (node);
        String realm  = ConfigLogger.getRealm  (node);

        try {
            ISOChannel channel = newChannel (
                props.getProperty ("class"),
                props.getProperty ("packager"), 
                logger, realm
            );
            Configuration cfg = new SimpleConfiguration (
                ConfigUtil.addProperties (node, props, evt)
            );
            if (channel instanceof Configurable) {
                ((Configurable)channel).setConfiguration (cfg);
            }
            ISOPackager p = channel.getPackager();

            if (p instanceof LogSource) {
                String pl = props.getProperty ("packager-logger", null);
                if (pl != null) 
                    ((LogSource)p).setLogger (
                        Logger.getLogger (pl), 
                        props.getProperty ("packager-realm", realm+".packager")
                    );
            }

            if (p instanceof Configurable) {
                ((Configurable)p).setConfiguration (cfg);
            }
            String socketFactoryName = cfg.get ("socket-factory", null);
            if (socketFactoryName != null) {
                ISOClientSocketFactory factory = 
                    getSocketFactory (socketFactoryName);
                if (factory != null) {
                    if (factory instanceof LogSource)
                        ((LogSource)factory).setLogger (logger, realm);
                    if (factory instanceof Configurable)
                        ((Configurable)factory).setConfiguration (cfg);
                    ConfigUtil.invoke (
                        channel, "setSocketFactory", 
                        factory, ISOClientSocketFactory.class
                    );
                }
            }

            if (cfg.get ("timeout", null) != null)
                ConfigUtil.invoke (
                    channel, "setTimeout", new Integer(cfg.getInt ("timeout"))
                );

            ConfigUtil.invoke (channel, "setHeader", cfg.get ("header", null));

            if (cfg.get ("connect").equals ("yes")) {
                try {
                    channel.connect();
                } catch (IOException e) {
                    evt.addMessage (e);
                }
            }

            JPanel panel = ConfigControlPanel.getPanel (node);
            if (panel != null) {
                ISOChannelPanel icp = new ISOChannelPanel (channel, name);
                if (props.getProperty ("scroll","yes").equals("no"))
                    icp.getISOMeter().setScroll(false);
                if (props.getProperty ("refresh",null) != null) {
                    int refresh = cfg.getInt ("refresh");
                    icp.getISOMeter().setRefresh (refresh);
                }
                panel.add (icp);
            }
            return channel;
        } catch (ConfigurationException e) {
            throw e;
        } catch (ISOException e) {
            throw new ConfigurationException ("error creating channel", e);
        } catch (NullPointerException e) {
            throw new ConfigurationException ("error creating channel", e);
        }
    }

    private ISOClientSocketFactory getSocketFactory (String name) 
        throws ConfigurationException
    {
        ISOClientSocketFactory factory = null;
        try {
            Class c = Class.forName (name);
            if (c != null) {
                factory = (ISOClientSocketFactory) c.newInstance();
                // if (logger != null && (factory instanceof LogSource))
                //    ((LogSource) factory).setLogger (logger, realm);
            }
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException (name, e);
        } catch (InstantiationException e) {
            throw new ConfigurationException (name, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException (name, e);
        }
        return factory;
    }

    private ISOChannel newChannel
        (String channelName, String packagerName, Logger logger, String realm)
        throws ConfigurationException
    {
        ISOChannel channel  = null;
        ISOPackager packager= null;
        try {
            Class c = Class.forName(channelName);
            if (c != null) {
                channel = (ISOChannel) c.newInstance();
                if (packagerName != null) {
                    Class p = Class.forName(packagerName);
                    packager = (ISOPackager) p.newInstance();
                    channel.setPackager(packager);
                }
                if (logger != null && (channel instanceof LogSource))
                    ((LogSource) channel) .
                        setLogger (logger, realm);
            }
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException ("newChannel:"+channelName, e);
        } catch (InstantiationException e) {
            throw new ConfigurationException ("newChannel:"+channelName, e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException ("newChannel:"+channelName, e);
        }
        return channel;
    }

    public static ISOChannel getChannel (String name)
        throws NameRegistrar.NotFoundException
    {
        return (ISOChannel) NameRegistrar.get ("channel."+name);
    }
    public static ISOChannel getChannel (Node node) {
        Node n = node.getAttributes().getNamedItem ("name");
        if (n != null)
            try {
                return ConfigChannel.getChannel (n.getNodeValue());
            } catch (NameRegistrar.NotFoundException e) { }
        return null;
    }
    public static ISOChannel getChildChannel (Node node) 
        throws ConfigurationException
    {
        ISOChannel channel = null;
        NodeList childs = node.getChildNodes();
        for (int i=0; i<childs.getLength() && channel == null; i++) {
            Node n = childs.item(i);
            if (n.getNodeName().equals ("channel"))
                channel = ConfigChannel.getChannel (n);
        }

        if (channel == null)
            throw new ConfigurationException ("could not find channel");

        return channel;
    }
}
