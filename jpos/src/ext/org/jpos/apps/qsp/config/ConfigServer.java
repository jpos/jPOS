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
import javax.swing.JPanel;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.gui.ISOChannelPanel;
import org.jpos.core.Configurable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOServer;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * Configure server 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigServer implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	ThreadPool pool = null;
	LogEvent evt = new LogEvent (qsp, "config-server");
	String name = node.getAttributes().getNamedItem ("name").getNodeValue();
	int port = Integer.parseInt (
	    node.getAttributes().getNamedItem ("port").getNodeValue()
	);
	Node maxSessions = node.getAttributes().getNamedItem ("maxSessions");
	if (maxSessions != null) 
	    pool = new ThreadPool (
		1, Integer.parseInt (maxSessions.getNodeValue())
	    );
	Logger logger = ConfigLogger.getLogger (node);
	String realm  = ConfigLogger.getRealm (node);
	ISOChannel channel = ConfigChannel.getChildChannel (node);

	if (!(channel instanceof ServerChannel))
	    throw new ConfigurationException (
		channel.getName() + " does not implement ServerChannel"
	    );

	ISOServer server = new ISOServer (port, (ServerChannel) channel, pool);

	evt.addMessage ("Server "+name+"/"+channel.getName()+"/"+port);
	server.setName (name);
	server.setLogger (logger, realm);
	JPanel panel = ConfigControlPanel.getPanel (node);
	if (panel != null) {
	    ISOChannelPanel icp = new ISOChannelPanel (name);
	    panel.add (icp);
	    server.addObserver (icp);
	}
	new Thread (server).start();
	Logger.log (evt);
    }
    public static ISOServer getServer (Node node) {
	Node n = node.getAttributes().getNamedItem ("name");
	if (n != null)
	    try {
		return ISOServer.getServer (n.getNodeValue());
	    } catch (NotFoundException e) { }
	return null;
    }
}
