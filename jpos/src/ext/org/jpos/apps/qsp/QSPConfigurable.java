package org.jpos.apps.qsp;

import org.w3c.dom.Node;
import org.jpos.core.Configuration;

/**
 * QSP Tasks may want to implement QSPConfigurable if they are to
 * receive startup configuration paramenters.
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface QSPConfigurable {
    public void config (Configuration cfg) 
	throws QSPConfigurator.ConfigurationException;
}
