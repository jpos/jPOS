package org.jpos.apps.qsp.config;

import java.util.Properties;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Sequencer;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configure sequencer
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigSequencer implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String className = 
	    node.getAttributes().getNamedItem ("class").getNodeValue();
	LogEvent evt = new LogEvent (qsp, "config-sequencer", className);
        try {
            Class c = Class.forName(className);
	    Sequencer seq = (Sequencer) c.newInstance();
	    if (seq instanceof LogSource) {
		((LogSource)seq).setLogger (
		    ConfigLogger.getLogger (node),
		    ConfigLogger.getRealm (node)
		);
	    }
	    if (seq instanceof Configurable)
		configureSequencer ((Configurable) seq, node, evt);

	    if (seq instanceof Loggeable)
		evt.addMessage (seq);

        } catch (ClassNotFoundException e) {
	    throw new ConfigurationException ("config-task:"+className, e);
        } catch (InstantiationException e) {
	    throw new ConfigurationException ("config-task:"+className, e);
        } catch (IllegalAccessException e) {
	    throw new ConfigurationException ("config-task:"+className, e);
	}
	Logger.log (evt);
    }
    private void configureSequencer (Configurable seq, Node node, LogEvent evt)
	throws ConfigurationException
    {
	seq.setConfiguration (new SimpleConfiguration (
		ConfigUtil.addProperties (node, null, evt)
	    )
	);
    }
}

