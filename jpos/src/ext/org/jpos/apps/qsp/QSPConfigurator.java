package org.jpos.apps.qsp;

import org.w3c.dom.Node;
import org.jpos.iso.ISOException;
import org.jpos.core.ConfigurationException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface QSPConfigurator {
   /**
    * @param appl reference to running QSP application
    * @param node current node
    * @throws ConfigurationException
    */
    public void config (QSP appl, Node node) throws ConfigurationException;
}

