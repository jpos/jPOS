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

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.core.NodeConfigurable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOServer;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Configure ISORequestListener
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.iso.ISORequestListener
 */
public class ConfigRequestListener implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
        LogEvent evt = new LogEvent (qsp, "config-request-listener");
        try {
            Node parent;
            // Find parent MUX
            if ( (parent  = node.getParentNode()) == null)
                throw new ConfigurationException ("orphan request listener");

            Object obj = null;
            if (parent.getNodeName().equals ("mux")) 
                obj = ConfigMux.getMUX (parent);
            else if (parent.getNodeName().equals ("server"))
                obj = ConfigServer.getServer (parent);

            if (obj == null) {
                ConfigurationException e = 
                    new ConfigurationException ("null parent");
                evt.addMessage (e);
                throw e;
            }
            addRequestListener (obj, node, evt);
        } finally {
            Logger.log (evt);
        }
    }

    private void addRequestListener (Object obj, Node node, LogEvent evt) 
        throws ConfigurationException
    {
        NamedNodeMap attr = node.getAttributes();
        String className = attr.getNamedItem ("class").getNodeValue();
        ISORequestListener listener = 
            (ISORequestListener) ConfigUtil.newInstance (className);
        evt.addMessage ("<request-listener class=\""+className+"\"/>");
        try {
            if (listener instanceof LogSource) {
                ((LogSource)listener).setLogger (
                    ConfigLogger.getLogger (node),
                    ConfigLogger.getRealm (node)
                );
                evt.addMessage ("<log-source/>");
            }
            
            if (listener instanceof NodeConfigurable) {
                evt.addMessage ("<NodeConfigurable>");
                ((NodeConfigurable)listener).setConfiguration (
                    node
                );
                evt.addMessage ("</NodeConfigurable>");
            }
            
            if (listener instanceof Configurable) {
                evt.addMessage ("<configurable>");
                Properties props = ConfigUtil.addProperties (node, null, evt);
                ((Configurable)listener).setConfiguration (
                    new SimpleConfiguration (props)
                );
                evt.addMessage ("</configurable>");
            }
            if (obj instanceof ISOMUX) {
                ((ISOMUX)obj).setISORequestListener (listener);
                evt.addMessage ("<parent type=\"mux\" name=\""+
                    ((ISOMUX)obj).getName() + "\"/>");
            }
            else if (obj instanceof ISOServer) {
                ((ISOServer)obj).addISORequestListener (listener);
                evt.addMessage ("<parent type=\"server\" name=\""+
                    ((ISOServer)obj).getName() + "\"/>");
            }
        } catch (ISOException e) {
            throw new ConfigurationException (e);
        }
    }
}
