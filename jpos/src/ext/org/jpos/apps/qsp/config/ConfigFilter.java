package org.jpos.apps.qsp.config;

import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.FilteredChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 * Configure filter
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.iso.ISOFilter
 */
public class ConfigFilter implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	LogEvent evt = new LogEvent (qsp, "config-filter");
	Node parent;

	// Find parent Channel 
	if ( (parent  = node.getParentNode()) == null)
	    throw new ConfigurationException ("orphan filter");

	ISOChannel c = ConfigChannel.getChannel (parent);
	if (c == null) 
	    throw new ConfigurationException ("null parent channel");
	if (!(c instanceof FilteredChannel))
	    throw new ConfigurationException ("not a filtered channel");

	FilteredChannel channel = (FilteredChannel) c;

	NamedNodeMap attr = node.getAttributes();
	String className = attr.getNamedItem ("class").getNodeValue();
	String direction = attr.getNamedItem ("direction").getNodeValue();

	ISOFilter filter = (ISOFilter) ConfigUtil.newInstance (className);
	if (filter instanceof Configurable) {
	    try {
		((Configurable)filter).setConfiguration (
		    new SimpleConfiguration (
			ConfigUtil.addProperties (node, null, evt)
			)
		);
	    } catch (ISOException e) {
		throw new ConfigurationException (e);
	    }
	}
	
	if (direction.equals ("incoming"))
	    channel.addIncomingFilter (filter);
	else if (direction.equals ("outgoing"))
	    channel.addOutgoingFilter (filter);
	else
	    channel.addFilter (filter);

	evt.addMessage ("parent-channel=" + channel.getName());
	Logger.log (evt);
    }
}
