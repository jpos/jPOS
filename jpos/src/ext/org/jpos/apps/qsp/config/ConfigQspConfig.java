package org.jpos.apps.qsp.config;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.QSPReConfigurator;

import org.w3c.dom.Node;

/**
 * Configure QSP
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigQspConfig implements QSPReConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	qsp.setLogger (ConfigLogger.getLogger (node),
		       ConfigLogger.getRealm (node));

	Node reloadNode = node.getAttributes().getNamedItem ("reload");
	if (reloadNode != null)
	    qsp.setMonitorConfigInterval (
		Long.parseLong (reloadNode.getNodeValue())
	    );
    }
    public void reconfig (QSP qsp, Node node) throws ConfigurationException {
	config (qsp, node);
    }
}
