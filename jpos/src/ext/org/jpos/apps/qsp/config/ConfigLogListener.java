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

import java.util.Properties;
import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogListener;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;

/**
 * Configure log listener
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigLogListener implements QSPConfigurator {
    public int DEFAULT_WINDOW = 86400;
    public int DEFAULT_COPIES = 10;

    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	LogEvent evt = new LogEvent (qsp, "config-log-listener");
	LogListener listener = getLogListener (node, evt);
	Node parent;

	// Find parent logger name
	if ( (parent  = node.getParentNode()) == null)
	    throw new ConfigurationException ("orphan log-listener");
	String loggerName = 
	    parent.getAttributes().getNamedItem ("name").getNodeValue();

	Logger l = Logger.getLogger(loggerName);
	l.addListener(listener);
	evt.addMessage ("parent logger=" + loggerName);
	Logger.log (evt);
    }

    private LogListener getLogListener (Node node, LogEvent evt) 
	throws ConfigurationException
    {
	LogListener listener = null;

	Node n = node.getAttributes().getNamedItem ("name");
	String name = null;
	if (n != null) {
	    name = n.getNodeValue();
	    try {
		listener = (LogListener) 
		    NameRegistrar.get ("log-listener." + name);
		evt.addMessage ("log-listener '" + name + "' reused");
	    } catch (NameRegistrar.NotFoundException e) { }
	}
	if (listener == null) {
	    listener = createLogListener (node, evt);
	    if (name != null)
		NameRegistrar.register ("log-listener." + name, listener);
	}
	return listener;
    }

    private LogListener createLogListener (Node node, LogEvent evt) 
	throws ConfigurationException 
    {
	String className =
	    node.getAttributes().getNamedItem ("class").getNodeValue();
	LogListener listener =
	    (LogListener) ConfigUtil.newInstance (className);

	evt.addMessage ("<log-listener class=\""+className+"\"/>");
	if (listener instanceof Configurable) {
	    Properties props = ConfigUtil.addProperties (node, null, evt);
	    ((Configurable)listener).setConfiguration (
		new SimpleConfiguration (props)
	    );
	    evt.addMessage ("<configurable/>");
	}
	return listener;
    }
}
