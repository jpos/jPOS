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

import java.util.Properties;

import javax.management.NotCompliantMBeanException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPReConfigurator;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Stopable;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.w3c.dom.Node;

/**
 * Configure User defined Tasks
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigObject implements QSPReConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
        String className = 
            node.getAttributes().getNamedItem ("class").getNodeValue();
        String name   = getValue (node, "name");
        LogEvent evt  = new LogEvent (qsp, "config-object", className);
        try {
            Class c = Class.forName(className);
            Object task = c.newInstance();
            if (task instanceof LogSource) {
                ((LogSource)task).setLogger (
                    ConfigLogger.getLogger (node),
                    ConfigLogger.getRealm (node)
                );
            }
            if (task instanceof Configurable) 
                configureTask ((Configurable) task, node, evt);

            if (name != null) {
                NameRegistrar.register (name, task);
                try {
                    qsp.registerMBean (task, "type=object,name="+name);
                } catch (NotCompliantMBeanException e) {
                    // ignoring, use may or may not implement MBean
                    evt.addMessage (e.getMessage());
                } 
            }

            if (task instanceof Runnable) {
                Thread thread = new Thread ((Runnable) task);
                thread.setName ("qsp-object-"+name);
                thread.start();
            }

            if (task instanceof Stopable) {
                try {
                    Runtime.getRuntime().addShutdownHook (
                        new Shutdown((Stopable) task, name)
                    );
                } catch (Exception e) {
                    evt.addMessage (e.toString ());
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException ("config-task:"+className, e);
        } finally {
            Logger.log (evt);
        }
    }
    public void reconfig (QSP qsp, Node node) throws ConfigurationException
    {
        String name   = getValue (node, "name");
        if (name == null)
            return; // nothing to do

        LogEvent evt = new LogEvent (qsp, "re-config-object", name);
        try {
            Object task = NameRegistrar.get (name);
            if (task instanceof ReConfigurable) 
                configureTask ((Configurable) task, node, evt);
        } catch (NameRegistrar.NotFoundException e) {
            evt.addMessage ("<object-not-found/>");
        }
        Logger.log (evt);
    }
    private void configureTask (Configurable task, Node node, LogEvent evt)
        throws ConfigurationException
    {
        String [] attributeNames = { "name", "connection-pool", "thread-pool" };
        Properties props = ConfigUtil.addAttributes (
            node, attributeNames, null, evt
        );
        task.setConfiguration (new SimpleConfiguration (
            ConfigUtil.addProperties (node, props, evt)
            )
        );
    }
    private String getValue (Node node, String tagName) {
        Node n = node.getAttributes().getNamedItem (tagName);
        return n != null ? n.getNodeValue() : null;
    }
}

