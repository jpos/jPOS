package org.jpos.core;

import java.io.*;
import java.util.*;
import org.jpos.iso.ISOException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @since jPOS 1.2
 */
public interface Configurable {
    public void setConfiguration (Configuration cfg) throws ISOException;
}
