package org.jpos.apps.qsp.config;

import java.util.Properties;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.jpos.util.LogEvent;

import org.jpos.apps.qsp.QSPConfigurator.ConfigurationException;

/**
 * Config Helper methods
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigUtil {
   /**
    * @param propertyName property name
    * @param attributeName attribute name
    * @param props  container (created if null)
    * @param node   context node
    * @param evt    optional LogEvent (can be null)
    */
    public static void addProperty (String propertyName, String attributeName,
	Properties props, Node node, LogEvent evt)
    {
	Node n = node.getAttributes().getNamedItem (attributeName);
	if (n != null) {
	    String value = n.getNodeValue();
	    props.put (propertyName, value);
	    if (evt != null)
		evt.addMessage (propertyName+"="+value);
	}
    }

   /**
    * @param node   context node
    * @param names  name pairs (node-attribute/property-name)
    * @param props  container (created if null)
    * @param evt    optional LogEvent (can be null)
    * @return [possibly created] props
    */
    public static Properties addAttributesProperties (
	Node node, String[][] names, Properties props, LogEvent evt)
    {
	if (props == null)
	    props = new Properties();
	for (int i=0; i<names.length; i++)
	    addProperty (names[i][0], names[i][1], props, node, evt);

	return props;
    }

   /**
    * @param node   context node
    * @param props  container (created if null)
    * @param evt    optional LogEvent (can be null)
    * @return [possibly created] props
    */
    public static Properties addProperties 
	(Node node, Properties props, LogEvent evt)
    {
	if (props == null)
	    props = new Properties();

	NodeList childs = node.getChildNodes();
	for (int i=0; i<childs.getLength(); i++) {
	    Node n = childs.item(i);
	    if (n.getNodeName().equals ("property")) {
		String name  = 
		    n.getAttributes().getNamedItem ("name").getNodeValue();
		String value = 
		    n.getAttributes().getNamedItem ("value").getNodeValue();
		props.put (name, value);
		if (evt != null)
		    evt.addMessage (name + "=" + value);
	    }
	}
	return props;
    }

   /**
    * @param className class Name
    * @return new Object instance
    * @throws ConfigurationException (with wrapped exception)
    */
    public static Object newInstance (String className)
	throws ConfigurationException
    {
        try {
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
	    throw new ConfigurationException (className, e);
        } catch (InstantiationException e) {
	    throw new ConfigurationException (className, e);
        } catch (IllegalAccessException e) {
	    throw new ConfigurationException (className, e);
	}
    }
}
