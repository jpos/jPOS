package org.jpos.apps.qsp.config;

import java.io.IOException;

import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogListener;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.RotateLogListener;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.QSPConfigurator.ConfigurationException;

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
	LogListener listener = null;
	String type=node.getAttributes().getNamedItem ("type").getNodeValue();

	if (type.equals ("simple")) {
	    listener = new SimpleLogListener (System.out);
	    evt.addMessage ("SimpleLogListener created");
	} else if (type.equals ("rotate")) 
	    listener = createRotateLogListener (node, evt);
	return listener;
    }

    private LogListener createRotateLogListener (Node node, LogEvent evt) 
	throws ConfigurationException 
    {
	Node filenameNode = node.getAttributes().getNamedItem ("filename");
	Node windowNode   = node.getAttributes().getNamedItem ("window");
	Node copiesNode   = node.getAttributes().getNamedItem ("copies");

	if (filenameNode == null)
	    throw new ConfigurationException 
		("rotate log-listener needs filename attribute");

	String filename = filenameNode.getNodeValue();
	int window = windowNode != null ?
	    Integer.parseInt (windowNode.getNodeValue()) :
	    DEFAULT_WINDOW;

	int copies = copiesNode != null ?
	    Integer.parseInt (copiesNode.getNodeValue()) :
	    DEFAULT_COPIES;

	evt.addMessage ("RorateLogListener created,");
	evt.addMessage ("filename=\""+filename +"\"");
	evt.addMessage ("window="+window);
	evt.addMessage ("copies="+copies);

	try {
	    return new RotateLogListener (filename, window, copies);
	} catch (IOException e) {
	    throw new ConfigurationException 
		("error creating RotateLogListener", e);
	}
    }
}
