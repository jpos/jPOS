package org.jpos.core;

import java.io.*;
import java.util.*;
import org.jpos.iso.ISOException;

/**
 * Object is Configurable
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @since jPOS 1.2
 */
public interface Configurable {
   /**
    * @param cfg Configuration object
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
	throws ConfigurationException;
}
