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

import javax.management.NotCompliantMBeanException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Configure ThreadPool
 * @author <A href=mailto:alcarraz@fing.edu.uy>Andr&eacute;s Alcarraz</A>
 * @version $Revision$ $Date$
 */
public class ConfigThreadPool implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
        LogEvent evt = new LogEvent (qsp, "config-thread-pool");
        try{
            NamedNodeMap atts = node.getAttributes();
            int initialSize = Integer.parseInt(atts.getNamedItem("initial-size").getNodeValue());
            int maxSize = Integer.parseInt(atts.getNamedItem("max-size").getNodeValue());
            ThreadPool threadPool = new ThreadPool (initialSize, maxSize);
            threadPool.setLogger (
                ConfigLogger.getLogger (node),
                ConfigLogger.getRealm (node)
            );
            evt.addMessage (threadPool);
            String name = atts.getNamedItem ("name").getNodeValue();
            NameRegistrar.register ("thread.pool."+name,threadPool);
            try{
                qsp.registerMBean(threadPool, "type=thread-pool,name=" + name);
            }catch (NotCompliantMBeanException e){
                evt.addMessage(e.getMessage());
            }
        } catch (Exception e){
            evt.addMessage(e);
        } finally{
            Logger.log (evt);
        }
    }
    public static ThreadPool getThreadPool (Node node) {
        Node n = node.getAttributes().getNamedItem ("thread-pool");
        if (n != null) {
            try {
                return ThreadPool.getThreadPool (n.getNodeValue ());
            } catch (NotFoundException e) { }
        }
        return null;
    }
}

