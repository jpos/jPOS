package org.jpos.apps.qsp;

import org.w3c.dom.Node;
import org.jpos.iso.ISOException;
import org.jpos.core.ConfigurationException;

/**
 * reconfigures a previously configured component
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface QSPReConfigurator extends QSPConfigurator {
   /**
    * @param appl reference to running QSP application
    * @param node current node
    * @throws ConfigurationException
    */
    public void reconfig (QSP appl, Node node) throws ConfigurationException;
}

