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

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;
import org.jpos.util.DirPoll;
import org.jpos.util.ThreadPool;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;
import org.jpos.core.ReConfigurable;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.QSPReConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configure DirPoll node
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigDirPoll implements QSPReConfigurator {
    public static final String NAMEREGISTRAR_PREFIX = "qsp.dirpoll.";

    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	LogEvent evt = new LogEvent (qsp, "config-dir-poll");
	try {
	    DirPoll dp = new DirPoll();
	    configDirPoll (qsp, node, dp, evt);

	    // non-reconfigurable attributes
	    dp.setThreadPool (new ThreadPool (1, getPoolSize (node, evt)));

	    String name = ConfigUtil.getAttribute (node, "name", null);
	    if (name != null)
		NameRegistrar.register (NAMEREGISTRAR_PREFIX+name, dp);

	    Thread thread = new Thread(dp);
	    thread.setName ("qsp-dir-poll-"+name);
	    thread.start();
	} finally {
	    Logger.log (evt);
	}
    }

    public void reconfig (QSP qsp, Node node) throws ConfigurationException
    {
	Node nameNode = node.getAttributes().getNamedItem ("name");
	if (nameNode == null)
	    return; 

	String name   = nameNode.getNodeValue();
	LogEvent evt = new LogEvent (qsp, "re-config-dir-poll");
	evt.addMessage ("<name>" + name + "</name>");
	try {
	    DirPoll dp = (DirPoll) 
		NameRegistrar.get (NAMEREGISTRAR_PREFIX + name);
	    if (dp != null)
		configDirPoll (qsp, node, dp, evt);
	} catch (NameRegistrar.NotFoundException e) {
	    evt.addMessage ("<dir-poll-not-found/>");
	}
	Logger.log (evt);
    }
    // ---------------------------------------------------- private helpers
    private void configureProcessor 
	(Configurable processor, Node node, LogEvent evt)
	throws ConfigurationException
    {
	processor.setConfiguration (new SimpleConfiguration (
		ConfigUtil.addProperties (node, null, evt)
	    )
	);
    }
    private int getPoolSize(Node node, LogEvent evt) {
	int i = ConfigUtil.getAttributeAsInt (node, "poolsize", 10);
	evt.addMessage ("<poolsize>"+i+"</poolsize>");
	return i;
    }
    private int getPollInterval (Node node, LogEvent evt) {
	int i = ConfigUtil.getAttributeAsInt (node, "interval", 1000);
	evt.addMessage ("<interval>"+i+"</interval>");
	return i;
    }
    private String getPriorities (Node node, LogEvent evt) {
	String s = ConfigUtil.getAttribute (node, "priorities", "");
	evt.addMessage ("<priorities>"+s+"</priorities>");
	return s;
    }
    private String getPath (Node node, LogEvent evt) { 
	String s = ConfigUtil.getAttribute (node, "path", "");
	evt.addMessage ("<path>"+s+"</path>");
	return s;
    }
    private void configDirPoll (QSP qsp, Node node, DirPoll dp, LogEvent evt)
	throws ConfigurationException
    {
	dp.setPath (getPath (node, evt));
	dp.setLogger (
	    ConfigLogger.getLogger (node),
	    ConfigLogger.getRealm  (node)
	);
	dp.setPollInterval (getPollInterval (node, evt));
	dp.setPriorities (getPriorities (node, evt)); 

	String className = ConfigUtil.getAttribute (node, "processor", null);
	Object processor = ConfigUtil.newInstance (className);

	if (!(processor instanceof DirPoll.Processor))
	    throw new ConfigurationException (
		"invalid class "+className
		+" does not implement DirPoll.Processor");
	dp.setProcessor ((DirPoll.Processor) processor);
	evt.addMessage ("<processor>"+className+"</processor>");
	if (ConfigUtil.getAttribute (node, "create", "no").equals ("yes")) {
	    evt.addMessage ("<create-dirs/>");
	    dp.createDirs();
	}
	if (processor instanceof LogSource)
	    ((LogSource)processor).setLogger (dp.getLogger(), dp.getRealm());

	if (processor instanceof Configurable)
	    configureProcessor ((Configurable) processor, node, evt);
    }
}

