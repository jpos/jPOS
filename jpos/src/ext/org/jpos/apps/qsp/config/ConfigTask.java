package org.jpos.apps.qsp.config;

import java.util.Properties;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogProducer;
import org.jpos.core.SimpleConfiguration;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurable;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.QSPConfigurator.ConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configure log listener
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigTask implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String className = 
	    node.getAttributes().getNamedItem ("class").getNodeValue();
	LogEvent evt = new LogEvent (qsp, "config-task", className);
        try {
            Class c = Class.forName(className);
	    Runnable task = (Runnable ) c.newInstance();
	    if (task instanceof LogProducer) {
		((LogProducer)task).setLogger (
		    ConfigLogger.getLogger (node),
		    ConfigLogger.getRealm (node)
		);
	    }
	    if (task instanceof QSPConfigurable)
		configureTask ((QSPConfigurable) task, node, evt);

	    Thread thread = new Thread(task);
	    thread.setName ("qsp-task");
	    thread.start();
        } catch (ClassNotFoundException e) {
	    throw new ConfigurationException ("config-task:"+className, e);
        } catch (InstantiationException e) {
	    throw new ConfigurationException ("config-task:"+className, e);
        } catch (IllegalAccessException e) {
	    throw new ConfigurationException ("config-task:"+className, e);
	}
	Logger.log (evt);
    }
    private void configureTask (QSPConfigurable task, Node node, LogEvent evt)
	throws ConfigurationException
    {
	Properties props = new Properties();

	NodeList childs = node.getChildNodes();
	for (int i=0; i<childs.getLength(); i++) {
	    Node n = childs.item(i);
	    if (n.getNodeName().equals ("property")) {
		String name  = 
		    n.getAttributes().getNamedItem ("name").getNodeValue();
		String value = 
		    n.getAttributes().getNamedItem ("value").getNodeValue();
		props.put (name, value);
		evt.addMessage (name + "=" + value);
	    }
	}
	task.config (new SimpleConfiguration (props));
    }
}

