package org.jpos.apps.qsp.config;

import java.util.Properties;
import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogProducer;
import org.jpos.util.LogEvent;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.QSPConfigurator.ConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

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
	Node parent;

	// Find parent MUX
	if ( (parent  = node.getParentNode()) == null)
	    throw new ConfigurationException ("orphan request listener");

	ISOMUX mux = ConfigMux.getMUX (parent);
	if (mux== null) 
	    throw new ConfigurationException ("null parent channel");
	addRequestListener (mux, node, evt);
	Logger.log (evt);
    }
    private void addRequestListener (ISOMUX mux, Node node, LogEvent evt) 
	throws ConfigurationException
    {
	NamedNodeMap attr = node.getAttributes();
	String className = attr.getNamedItem ("class").getNodeValue();
	ISORequestListener listener = 
	    (ISORequestListener) ConfigUtil.newInstance (className);
	evt.addMessage ("<request-listener class=\""+className+"\"/>");
	try {
	    if (listener instanceof LogProducer) {
		((LogProducer)listener).setLogger (
		    ConfigLogger.getLogger (node),
		    ConfigLogger.getRealm (node)
		);
		evt.addMessage ("<log-producer/>");
	    }
	    if (listener instanceof Configurable) {
		Properties props = ConfigUtil.addProperties (node, null, evt);
		props.put ("source-mux", mux.getName());

		((Configurable)listener).setConfiguration (
		    new SimpleConfiguration (props)
		);
		evt.addMessage ("<configurable/>");
	    }
	    mux.setISORequestListener (listener);
	} catch (ISOException e) {
	    throw new ConfigurationException (e);
	}
    }
}
