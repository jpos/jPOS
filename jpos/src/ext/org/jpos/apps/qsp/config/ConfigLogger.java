package org.jpos.apps.qsp.config;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;

/**
 * Configure logger
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigLogger implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String name = node.getAttributes().getNamedItem ("name").getNodeValue();
	Logger.getLogger (name);
	Logger.log (
	    new LogEvent (qsp, "config-logger", name)
	);
    }
   /**
    * @return Logger for this node or null
    */
    public static Logger getLogger (Node node) {
	Node n = node.getAttributes().getNamedItem ("logger");
	return (n != null) ? Logger.getLogger (n.getNodeValue()) : null;
    }
   /**
    * @return realm for this node (or "")
    */
    public static String getRealm(Node node) {
	Node n = node.getAttributes().getNamedItem ("realm");
	return (n != null) ? n.getNodeValue() : "";
    }
}
