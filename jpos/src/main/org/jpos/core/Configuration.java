/*
 * $Log$
 * Revision 1.2  1999/11/11 10:18:48  apr
 * added get(name,name), getInt(name), getLong(name) and getDouble(name)
 *
 * Revision 1.1  1999/09/26 22:31:59  apr
 * CVS sync
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * CardAgents relies on a Configuration object to provide
 * runtime configuration parameters such as merchant number, etc.
 */
public interface Configuration {
    public String get       (String propertyName);
    public String get       (String propertyName, String defaultValue);
    public int getInt       (String propertyName);
    public long getLong     (String propertyName);
    public double getDouble (String propertyName);
}
