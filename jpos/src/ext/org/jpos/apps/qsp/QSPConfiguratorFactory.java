package org.jpos.apps.qsp;

import org.jpos.core.ConfigurationException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class QSPConfiguratorFactory {
    public static final String PACKAGEPREFIX = 
	"org.jpos.apps.qsp.config.Config";

   /**
    * creates an instance of a QSPConfigurator<br>
    * (org.jpos.apps.qsp.config.* class)
    * @param tagName
    * @throws ConfigurationException
    */
    public static QSPConfigurator create (String tagName) 
	throws ConfigurationException
    {
	String className= PACKAGEPREFIX + toClassName (tagName);
	try {
            Class c = Class.forName(className);
	    return (QSPConfigurator) c.newInstance();
	} catch (Exception e) {
	    throw new ConfigurationException ("can't create "+className, e);
	}
    }
    private static String toClassName (String s) {
	boolean capitalize = true;
	s = s.toLowerCase();
	StringBuffer sb = new StringBuffer();
	for (int i=0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if (c == '-') {
		capitalize = true;
		continue;
	    }
	    sb.append (capitalize ? Character.toUpperCase(c) : c);
	    capitalize = false;
	}
	return sb.toString();
    }
}
