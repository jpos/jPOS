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

package org.jpos.apps.qsp;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.xerces.parsers.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.ErrorHandler;

import org.jpos.util.SimpleLogListener;
import org.jpos.util.SystemMonitor;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.core.ConfigurationException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see <a href="http://www.cebik.com/qsig.html">QSP</a>
 */
public class QSP implements ErrorHandler, LogSource {
    Document config;
    Logger logger;
    String realm;
    File configFile;
    long lastModified;
    static ControlPanel controlPanel = null;
    long monitorConfigInterval = 60 * 1000;
    Collection reconfigurables;

    public static String[] SUPPORTED_TAGS = 
	{ "logger",
	  "qsp-config",
	  "log-listener",
	  "persistent-engine",
	  "sequencer",
	  "control-panel",
	  "channel",
	  "filter",
	  "mux",
	  "server",
	  "request-listener",
	  "card-agent",
	  "dir-poll",
	  "task",
	  "daily-task"
	};

    public QSP () {
	super();
	reconfigurables = new ArrayList();
    }
    public void setConfig (Document config) {
	this.config = config;
    }
    public void setConfigFile (File f) {
	this.configFile = f;
	this.lastModified = f.lastModified();
    }
    public void setMonitorConfigInterval (long l) {
	monitorConfigInterval = l;
    }
    public File getConfigFile () {
	return configFile;
    }
    public Collection getReConfigurables() {
	return reconfigurables;
    }
    public ControlPanel initControlPanel (int rows, int cols) {
	if (controlPanel == null) {
	    synchronized (QSP.class) {
		if (controlPanel == null) 
		    controlPanel = new ControlPanel (this, rows, cols);
	    }
	}
	return controlPanel;
    }
    public ControlPanel getControlPanel (){
	return controlPanel;
    }
    public void warning (SAXParseException e) throws SAXException {
	Logger.log (new LogEvent (this, "warning", e));
	throw e;
    }
    public void error (SAXParseException e) throws SAXException {
	Logger.log (new LogEvent (this, "error", e));
	throw e;
    }

    public void fatalError (SAXParseException e) throws SAXException {
	Logger.log (new LogEvent (this, "fatalError", e));
	throw e;
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger () {
	return logger;
    }
    public void configure (String tagname) throws ConfigurationException {
	QSPConfigurator configurator = QSPConfiguratorFactory.create (tagname);
	NodeList nodes = config.getElementsByTagName (tagname);
	if (configurator instanceof QSPReConfigurator && nodes.getLength()>0)
	    reconfigurables.add (tagname);
	for (int i=0; i<nodes.getLength(); i++) {
	    configurator.config (this, nodes.item(i));
	}
    }
    public void reconfigure (String tagname) throws ConfigurationException {
	QSPConfigurator configurator = QSPConfiguratorFactory.create (tagname);
	if (configurator instanceof QSPReConfigurator) {
	    NodeList nodes = config.getElementsByTagName (tagname);
	    for (int i=0; i<nodes.getLength(); i++) 
		((QSPReConfigurator)configurator).reconfig 
		    (this, nodes.item(i));
	}
    }
    private boolean monitorConfigFile () {
	long l;
	while (lastModified == (l=configFile.lastModified()))
	    try {
		Thread.sleep (monitorConfigInterval);
	    } catch (InterruptedException e) { }
	lastModified = l;
	return true;
    }
    public static void main (String args[]) {
	if (args.length != 1) {
	    System.out.println ("Usage: org.jpos.apps.qsp.QSP <configfile>");
	    System.exit (1);
	}
	DOMParser parser = new DOMParser();
	QSP qsp = new QSP();
	// qsp.getLogger().addListener (new SimpleLogListener(System.out));
	try {
	    qsp.setConfigFile (new File (args[0]));
	    parser.setFeature("http://xml.org/sax/features/validation", true);
	    parser.setErrorHandler (qsp);
	    parser.parse (qsp.getConfigFile().getPath());
	    qsp.setConfig (parser.getDocument());
	    for (int i=0; i<SUPPORTED_TAGS.length; i++)
		qsp.configure (SUPPORTED_TAGS[i]);

	    if (controlPanel != null)
		controlPanel.showUp();

	    if (qsp.getLogger() != null) 
		new SystemMonitor (3600000, qsp.getLogger(), "monitor");
		    
	    while (qsp.monitorConfigFile ()) {
		parser.parse (qsp.getConfigFile().getPath());
		qsp.setConfig (parser.getDocument());
		Iterator iter = qsp.getReConfigurables().iterator();
		while (iter.hasNext())
		    qsp.reconfigure ((String) iter.next());
	    }
	} catch (IOException e) {
	    Logger.log (new LogEvent (qsp, "error", e));
	    System.out.println (e);
	} catch (SAXException e) {
	    Logger.log (new LogEvent (qsp, "error", e));
	    System.out.println (e);
	} catch (ConfigurationException e) {
	    Logger.log (new LogEvent (qsp, "error", e));
	    System.out.println (e);
	}
    }
}
