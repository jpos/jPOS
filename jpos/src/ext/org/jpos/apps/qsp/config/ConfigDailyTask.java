package org.jpos.apps.qsp.config;

import java.util.Properties;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;
import org.jpos.core.ReConfigurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.QSPReConfigurator;
import org.jpos.apps.qsp.task.DailyTask;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configure User defined Tasks
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigDailyTask implements QSPReConfigurator {
    public static final String NAMEREGISTRAR_PREFIX = "qsp.daily.task.";

    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String className = 
	    node.getAttributes().getNamedItem ("class").getNodeValue();
	LogEvent evt    = new LogEvent (qsp, "config-daily-task", className);
	String name     = getValue (node, "name");
	String poolSize = getValue (node, "poolsize");
	ThreadPool pool = (poolSize != null)  ?
	    new ThreadPool (1, Integer.parseInt (poolSize)) : null;

        try {
            Class c = Class.forName(className);
	    Runnable task = (Runnable) c.newInstance();
	    DailyTask controller = new DailyTask (task, pool);

	    controller.setLogger (
		ConfigLogger.getLogger (node),
		ConfigLogger.getRealm (node) + ".daily-task" 
	    );
	    configureTask (controller, node, evt);

	    if (name != null)
		NameRegistrar.register (NAMEREGISTRAR_PREFIX+name, controller);
	    Thread thread = new Thread(controller);
	    thread.setName ("qsp-daily-task-"+name);
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
	String name   = getValue (node, "name");
	if (name == null)
	    return; // nothing to do

	LogEvent evt = new LogEvent (qsp, "re-config-task", name);
	try {
	    DailyTask controller = (DailyTask) 
		NameRegistrar.get (NAMEREGISTRAR_PREFIX + name);
	    configureTask (controller, node, evt);
	} catch (NameRegistrar.NotFoundException e) {
	    evt.addMessage ("<task-not-found/>");
	}
	Logger.log (evt);
    }
    private void configureTask (Configurable task, Node node, LogEvent evt)
	throws ConfigurationException
    {
	Properties props = new Properties();
	String start = getValue (node, "start");
	if (start == null)
	    throw new ConfigurationException ("Attribute 'start' no found");

	props.put ("start", start);
	task.setConfiguration (
	    new SimpleConfiguration (
		ConfigUtil.addProperties (node, props, evt)
	    )
	);
    }
    private String getValue (Node node, String tagName) {
	Node n = node.getAttributes().getNamedItem (tagName);
	return n != null ? n.getNodeValue() : null;
    }
}

