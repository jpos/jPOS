package org.jpos.apps.qsp.config;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.NameRegistrar;
import org.jpos.core.ConfigurationException;

import org.jpos.apps.qsp.QSP;
import org.jpos.apps.qsp.QSPConfigurator;
import org.jpos.apps.qsp.ControlPanel;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;


/**
 * Configure logger
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigControlPanel implements QSPConfigurator {
    public void config (QSP qsp, Node node) throws ConfigurationException
    {
	String rows = 
	    node.getAttributes().getNamedItem ("rows").getNodeValue();
	String cols = 
	    node.getAttributes().getNamedItem ("cols").getNodeValue();

	qsp.initControlPanel (Integer.parseInt (rows),
			      Integer.parseInt (cols));

	configChilds (qsp, node);
    }

    private void configChilds (QSP qsp, Node node) throws
	ConfigurationException
    {
	NodeList childs = node.getChildNodes();
	for (int i=0; i<childs.getLength(); i++) {
	    Node n = childs.item(i);
	    if (n.getNodeName().equals ("panel")) {
		String panelName = 
		  n.getAttributes().getNamedItem("name").getNodeValue();
		JPanel panel = new JPanel();
		NameRegistrar.register ("panel."+panelName, panel);
		qsp.getControlPanel().add (panel);
	    }
	}
    }

    public static JPanel getPanel (Node node) {
	JPanel panel = null;
	Node n = node.getAttributes().getNamedItem ("panel");
	if (n != null) {
	    try {
		panel = (JPanel) 
		    NameRegistrar.get ("panel." + n.getNodeValue());
	    } catch (NameRegistrar.NotFoundException  e) { }
	}
	return panel;
    }
}
