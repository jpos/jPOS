package org.jpos.apps.qsp.config;

import java.util.Properties;
import javax.swing.JPanel;

import org.jpos.iso.ISOChannel;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.gui.ISOChannelPanel;
import org.jpos.core.Configurable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOServer;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * Configure server 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigServer implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	ThreadPool pool = null;
	LogEvent evt = new LogEvent (qsp, "config-server");
	String name = node.getAttributes().getNamedItem ("name").getNodeValue();
	int port = Integer.parseInt (
	    node.getAttributes().getNamedItem ("port").getNodeValue()
	);
	Node maxSessions = node.getAttributes().getNamedItem ("maxSessions");
	if (maxSessions != null) 
	    pool = new ThreadPool (
		1, Integer.parseInt (maxSessions.getNodeValue())
	    );
	Logger logger = ConfigLogger.getLogger (node);
	String realm  = ConfigLogger.getRealm (node);
	ISOChannel channel = ConfigChannel.getChildChannel (node);

	if (!(channel instanceof ServerChannel))
	    throw new ConfigurationException (
		channel.getName() + " does not implement ServerChannel"
	    );

	ISOServer server = new ISOServer (port, (ServerChannel) channel, pool);

	evt.addMessage ("Server "+name+"/"+channel.getName()+"/"+port);
	server.setName (name);
	server.setLogger (logger, realm);
	JPanel panel = ConfigControlPanel.getPanel (node);
	if (panel != null) {
	    ISOChannelPanel icp = new ISOChannelPanel (name);
	    panel.add (icp);
	    server.addObserver (icp);
	}
	new Thread (server).start();
	Logger.log (evt);
    }
    public static ISOServer getServer (Node node) {
	Node n = node.getAttributes().getNamedItem ("name");
	if (n != null)
	    try {
		return ISOServer.getServer (n.getNodeValue());
	    } catch (NotFoundException e) { }
	return null;
    }
}
