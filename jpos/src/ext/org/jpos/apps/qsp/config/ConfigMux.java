package org.jpos.apps.qsp.config;

import java.util.Properties;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISORequestListener;
import org.jpos.core.Configurable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMUX;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar.NotFoundException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * Configure logger
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigMux implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	LogEvent evt = new LogEvent (qsp, "config-mux");
	String name = node.getAttributes().getNamedItem ("name").getNodeValue();
	Logger logger = ConfigLogger.getLogger (node);
	String realm  = ConfigLogger.getRealm (node);
	ISOChannel channel = getChildChannel (node);
	ISOMUX mux = new ISOMUX (channel, logger, realm);
	evt.addMessage ("MUX "+name+"/"+channel.getName());
	mux.setName (name);
	new Thread (mux).start();
	Logger.log (evt);
    }
    private ISOChannel getChildChannel (Node node) 
	throws ConfigurationException
    {
	ISOChannel channel = null;
	NodeList childs = node.getChildNodes();
	for (int i=0; i<childs.getLength() && channel == null; i++) {
	    Node n = childs.item(i);
	    if (n.getNodeName().equals ("channel"))
		channel = ConfigChannel.getChannel (n);
	}

	if (channel == null)
	    throw new ConfigurationException
	       ("invalid mux - could not find channel");

	return channel;
    }
    public static ISOMUX getMUX (Node node) {
	Node n = node.getAttributes().getNamedItem ("name");
	if (n != null)
	    try {
		return ISOMUX.getMUX (n.getNodeValue());
	    } catch (NotFoundException e) { }
	return null;
    }
}
