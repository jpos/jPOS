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
