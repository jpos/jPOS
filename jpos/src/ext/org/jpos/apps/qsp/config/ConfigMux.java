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
import org.jpos.apps.qsp.QSPReConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * Configure logger
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigMux implements QSPReConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String name = node.getAttributes().getNamedItem ("name").getNodeValue();
	LogEvent evt = new LogEvent (qsp, "config-mux", name);
	Logger logger = ConfigLogger.getLogger (node);
	String realm  = ConfigLogger.getRealm (node);
	ISOChannel channel = ConfigChannel.getChildChannel (node);
	ISOMUX mux = new ISOMUX (channel, logger, realm);
	boolean connect = node.getAttributes()
			.getNamedItem("connect").getNodeValue().equals("yes");
	evt.addMessage ("MUX "+name+"/"+channel.getName());
	mux.setName (name);
	mux.setConnect (connect);
	new Thread (mux).start();
	Logger.log (evt);
    }
    public void reconfig (QSP qsp, Node node) throws ConfigurationException
    {
	String name = node.getAttributes().getNamedItem ("name").getNodeValue();
	LogEvent evt = new LogEvent (qsp, "re-config-mux", name);
	boolean connect = node.getAttributes()
			.getNamedItem("connect").getNodeValue().equals("yes");
	try {
	    ISOMUX mux = ISOMUX.getMUX (name);
	    mux.setLogger (ConfigLogger.getLogger (node),
			   ConfigLogger.getRealm (node));
	    mux.setConnect (connect);
	} catch (NotFoundException e) {
	    evt.addMessage (e);
	    throw new ConfigurationException (e);
	} finally {
	    Logger.log (evt);
	}
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
