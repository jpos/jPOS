package org.jpos.apps.qsp.config;

import java.io.IOException;
import java.util.Properties;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFactory;
import org.jpos.iso.ISOException;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.NameRegistrar;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;

/**
 * Configure logger
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigChannel implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	ISOChannel channel;
	LogEvent evt = new LogEvent (qsp, "config-channel");
	String name = node.getAttributes().getNamedItem ("name").getNodeValue();
	try {
	    channel = ISOFactory.getChannel (name);
	} catch (NameRegistrar.NotFoundException e) {
	    channel = createChannel (name, node, evt);
	    channel.setName (name);
	}
	Logger.log (evt);
    }

    private ISOChannel createChannel (String name, Node node, LogEvent evt)
	throws ConfigurationException
    {
	String [][] names = { { name + ".channel",  "class"    },
	                      { name + ".packager", "packager" },
			      { name + ".header",   "header"   },
			      { name + ".host",     "host"     },
			      { name + ".port",     "port"     }
			    };

	Properties props = ConfigUtil.addAttributesProperties 
	    (node, names, null, evt);

	SimpleConfiguration cfg = new SimpleConfiguration (props);
	Logger logger = ConfigLogger.getLogger (node);
	String realm  = ConfigLogger.getRealm  (node);

	try {
	    ISOChannel channel = 
		ISOFactory.newChannel (cfg, name, logger, realm);
	    boolean connect = 
		node.getAttributes() 
		    .getNamedItem("connect").getNodeValue().equals("yes");
	    if (connect) {
		try {
		    channel.connect();
		} catch (IOException e) {
		    evt.addMessage (e);
		}
	    }
	    return channel;
	} catch (ISOException e) {
	    throw new ConfigurationException ("error creating channel", e);
	} 
    }

    public static ISOChannel getChannel (Node node) {
	Node n = node.getAttributes().getNamedItem ("name");
	if (n != null)
	    try {
		return ISOFactory.getChannel (n.getNodeValue());
	    } catch (NameRegistrar.NotFoundException e) { }
	return null;
    }
}
