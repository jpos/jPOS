package org.jpos.apps.qsp;

import org.w3c.dom.Node;
import org.jpos.iso.ISOException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface QSPConfigurator {
    public class ConfigurationException extends ISOException {
	public ConfigurationException () {
	    super();
	}
	public ConfigurationException (String detail) {
	    super (detail);
	}
	public ConfigurationException (Exception nested) {
	    super (nested);
	}
	public ConfigurationException (String detail, Exception nested) {
	    super (detail, nested);
	}
    }
   /**
    * @param appl reference to running QSP application
    * @param node current node
    * @throws ConfigurationException
    */
    public void config (QSP appl, Node node) throws ConfigurationException;
}
