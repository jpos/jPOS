package org.jpos.apps.qsp.config;

import java.util.Properties;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.tpl.PersistentEngine;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configure PersistentEngine
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigPersistentEngine implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String className = 
	    node.getAttributes().getNamedItem("class") != null ? 
		node.getAttributes().getNamedItem ("class").getNodeValue() :
		"org.jpos.tpl.PersistentEngine";
	LogEvent evt = 
	new LogEvent (qsp, "config-persistent-engine", className);
	PersistentEngine engine = new PersistentEngine();
	engine.setLogger (
	    ConfigLogger.getLogger (node),
	    ConfigLogger.getRealm (node)
	);
	engine.setConfiguration (
	    new SimpleConfiguration (
		ConfigUtil.addProperties (node, null, evt)
	    )
	);
	if (engine instanceof Loggeable)
	    evt.addMessage (engine);
	NameRegistrar.register ("persistent.engine."+ 
	    node.getAttributes().getNamedItem ("name").getNodeValue(),
	    engine
	);
	Logger.log (evt);
    }
    public static PersistentEngine getPersistentEngine (Node node) {
	Node n = node.getAttributes().getNamedItem ("persistent-engine");
	if (n != null)
	    try {
		return (PersistentEngine) 
		    NameRegistrar.get("persistent.engine."+n.getNodeValue());
	    } catch (NotFoundException e) { }
	return null;
    }
}

