package org.jpos.iso;

import org.xml.sax.Attributes;

/**
 * Interface for configurable packager
 *
 * @author Vishnu Pillai
 * @see org.jpos.iso.ISOPackager
 */
public interface ConfigurablePackager {

    public void setGenericPackagerParams(Attributes configuration) throws ISOException;

}
