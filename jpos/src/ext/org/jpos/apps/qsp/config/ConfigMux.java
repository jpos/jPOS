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
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMUX;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.w3c.dom.Node;

/**
 * Configure mux
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigMux implements QSPReConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
        String name = node.getAttributes().getNamedItem ("name").getNodeValue();
        LogEvent evt = new LogEvent (qsp, "config-mux", name);
        Logger logger = ConfigLogger.getLogger (node);
        String realm  = ConfigLogger.getRealm (node);
        ISOChannel channel = ConfigChannel.getChildChannel (node);
        ISOMUX mux = null;
        try{
            if(node.getAttributes().getNamedItem("class")==null) 
                mux = new ISOMUX (channel, logger, realm);
            else{ 
                Class muxClass = Class.forName(node.getAttributes().getNamedItem("class").getNodeValue());
                Class[] argTypes = {ISOChannel.class, Logger.class, String.class};
                Object[] args = {channel, logger, realm};
                mux = (ISOMUX)muxClass.getConstructor(argTypes).newInstance(args);
            }
        }catch(Exception e){
            throw new ConfigurationException("error trying to construct ISOMUX", e);
        }
        boolean connect = node.getAttributes()
                        .getNamedItem("connect").getNodeValue().equals("yes");
        evt.addMessage ("MUX "+name+"/"+channel.getName());
        mux.setName (name);
        mux.setConnect (connect);
        if (mux instanceof Configurable) {
            Configuration cfg = new SimpleConfiguration (
                ConfigUtil.addProperties (node, new Properties(), evt)
            );
            ((Configurable)mux).setConfiguration (cfg);
        }
        new Thread (mux).start();
        try {
            qsp.registerMBean (mux, "type=mux,name="+name);
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
        LogEvent evt = new LogEvent (qsp, "re-config-mux", name);
        boolean connect = node.getAttributes()
                        .getNamedItem("connect").getNodeValue().equals("yes");
        try {
            ISOMUX mux = ISOMUX.getMUX (name);
            mux.setLogger (ConfigLogger.getLogger (node),
                           ConfigLogger.getRealm (node));
            mux.setConnect (connect);

            if (mux instanceof ReConfigurable) {
                Configuration cfg = new SimpleConfiguration (
                    ConfigUtil.addProperties (node, new Properties(), evt)
                );
                ((Configurable)mux).setConfiguration (cfg);
            }
        } catch (NotFoundException e) {
            evt.addMessage (e);
            throw new ConfigurationException (e);
        } finally {
            Logger.log (evt);
        }
    }

    public static ISOMUX getMUX (Node node) {
        Node n = node.getAttributes().getNamedItem ("name");
        if (n != null)
            try {
                return ISOMUX.getMUX (n.getNodeValue());
            } catch (NotFoundException e) { }
        return null;
    }
}
