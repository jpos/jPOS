package org.jpos.apps.qsp.config;

import java.util.Properties;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;
import org.jpos.core.ReConfigurable;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.QSPReConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configure User defined Tasks
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigTask implements QSPReConfigurator {
    public static final String NAMEREGISTRAR_PREFIX = "qsp.task.";

    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String className = 
	    node.getAttributes().getNamedItem ("class").getNodeValue();
	Node nameNode = node.getAttributes().getNamedItem ("name");
	String name   = nameNode != null ? nameNode.getNodeValue() : null;
	LogEvent evt = new LogEvent (qsp, "config-task", className);
        try {
            Class c = Class.forName(className);
	    Runnable task = (Runnable ) c.newInstance();
	    if (task instanceof LogSource) {
		((LogSource)task).setLogger (
		    ConfigLogger.getLogger (node),
		    ConfigLogger.getRealm (node)
		);
	    }
	    if (task instanceof Configurable)
		configureTask ((Configurable) task, node, evt);

	    if (name != null)
		NameRegistrar.register (NAMEREGISTRAR_PREFIX+name, task);
	    Thread thread = new Thread(task);
	    thread.setName ("qsp-task-"+name);
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
    public void reconfig (QSP qsp, Node node) throws ConfigurationException
    {
	Node nameNode = node.getAttributes().getNamedItem ("name");
	if (nameNode == null)
	    return; // nothing to do

	String name   = nameNode.getNodeValue();
	LogEvent evt = new LogEvent (qsp, "re-config-task", name);
	try {
	    Object task = NameRegistrar.get (NAMEREGISTRAR_PREFIX + name);
	    if (task instanceof ReConfigurable) 
		configureTask ((Configurable) task, node, evt);
	} catch (NameRegistrar.NotFoundException e) {
	    evt.addMessage ("<task-not-found/>");
	}
	Logger.log (evt);
    }
    private void configureTask (Configurable task, Node node, LogEvent evt)
	throws ConfigurationException
    {
	task.setConfiguration (new SimpleConfiguration (
	    ConfigUtil.addProperties (node, null, evt)
	    )
	);
    }
}

