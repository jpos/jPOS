package org.jpos.core;

import org.jpos.iso.ISOException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see Configurable
 * @since jPOS 1.2
 */
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
