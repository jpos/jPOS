package org.jpos.apps.qsp.config;

import java.util.Properties;
import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.util.LogEvent;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Configurable;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

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
	try {
	    Node parent;
	    // Find parent MUX
	    if ( (parent  = node.getParentNode()) == null)
		throw new ConfigurationException ("orphan request listener");

	    Object obj = null;
	    if (parent.getNodeName().equals ("mux")) 
		obj = ConfigMux.getMUX (parent);
	    else if (parent.getNodeName().equals ("server"))
		obj = ConfigServer.getServer (parent);

	    if (obj == null) {
		ConfigurationException e = 
		    new ConfigurationException ("null parent");
		evt.addMessage (e);
		throw e;
	    }
	    addRequestListener (obj, node, evt);
	} finally {
	    Logger.log (evt);
	}
    }

    private void addRequestListener (Object obj, Node node, LogEvent evt) 
	throws ConfigurationException
    {
	NamedNodeMap attr = node.getAttributes();
	String className = attr.getNamedItem ("class").getNodeValue();
	ISORequestListener listener = 
	    (ISORequestListener) ConfigUtil.newInstance (className);
	evt.addMessage ("<request-listener class=\""+className+"\"/>");
	try {
	    if (listener instanceof LogSource) {
		((LogSource)listener).setLogger (
		    ConfigLogger.getLogger (node),
		    ConfigLogger.getRealm (node)
		);
		evt.addMessage ("<log-source/>");
	    }
	    if (listener instanceof Configurable) {
		evt.addMessage ("<configurable>");
		Properties props = ConfigUtil.addProperties (node, null, evt);
		((Configurable)listener).setConfiguration (
		    new SimpleConfiguration (props)
		);
		evt.addMessage ("</configurable>");
	    }
	    if (obj instanceof ISOMUX) {
		((ISOMUX)obj).setISORequestListener (listener);
		evt.addMessage ("<parent type=\"mux\" name=\""+
		    ((ISOMUX)obj).getName() + "\"/>");
	    }
	    else if (obj instanceof ISOServer) {
		((ISOServer)obj).addISORequestListener (listener);
		evt.addMessage ("<parent type=\"server\" name=\""+
		    ((ISOServer)obj).getName() + "\"/>");
	    }
	} catch (ISOException e) {
	    throw new ConfigurationException (e);
	}
    }
}
