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
import org.jpos.apps.qsp.task.DailyTask;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;
import org.w3c.dom.Node;

/**
 * Configure User defined Tasks
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigDailyTask implements QSPReConfigurator {
    public static final String NAMEREGISTRAR_PREFIX = "qsp.daily.task.";

    public void config (QSP qsp, Node node) throws ConfigurationException
    {
        String className = 
            node.getAttributes().getNamedItem ("class").getNodeValue();
        LogEvent evt    = new LogEvent (qsp, "config-daily-task", className);
        String name     = getValue (node, "name");
        String poolSize = getValue (node, "poolsize");
        ThreadPool pool = (poolSize != null)  ?
            new ThreadPool (1, Integer.parseInt (poolSize)) : null;

        try {
            Class c = Class.forName(className);
            Runnable task = (Runnable) c.newInstance();
            DailyTask controller = new DailyTask (task, pool);

            controller.setLogger (
                ConfigLogger.getLogger (node),
                ConfigLogger.getRealm (node) + ".daily-task" 
            );
            configureTask (controller, node, evt);

            if (name != null) {
                NameRegistrar.register (NAMEREGISTRAR_PREFIX+name, controller);
                try {
                    qsp.registerMBean (controller, "type=dailytask,name="+name);
                } catch (NotCompliantMBeanException e) {
                    // ignoring, use may or may not implement MBean
                    evt.addMessage (e.getMessage());
                } 
            }
            Thread thread = new Thread(controller);
            thread.setName ("qsp-daily-task-"+name);
            thread.start();
        } catch (Exception e) {
            throw new ConfigurationException ("config-daily-task:"+className,e);
        } finally {
            Logger.log (evt);
        }
    }
    public void reconfig (QSP qsp, Node node) throws ConfigurationException
    {
        String name   = getValue (node, "name");
        if (name == null)
            return; // nothing to do

        LogEvent evt = new LogEvent (qsp, "re-config-task", name);
        try {
            DailyTask controller = (DailyTask) 
                NameRegistrar.get (NAMEREGISTRAR_PREFIX + name);
            configureTask (controller, node, evt);
        } catch (NameRegistrar.NotFoundException e) {
            evt.addMessage ("<task-not-found/>");
        }
        Logger.log (evt);
    }
    private void configureTask (Configurable task, Node node, LogEvent evt)
        throws ConfigurationException
    {
        Properties props = new Properties();
        String start = getValue (node, "start");
        if (start == null)
            throw new ConfigurationException ("Attribute 'start' no found");

        props.put ("start", start);
        task.setConfiguration (
            new SimpleConfiguration (
                ConfigUtil.addProperties (node, props, evt)
            )
        );
    }
    private String getValue (Node node, String tagName) {
        Node n = node.getAttributes().getNamedItem (tagName);
        return n != null ? n.getNodeValue() : null;
    }
}

