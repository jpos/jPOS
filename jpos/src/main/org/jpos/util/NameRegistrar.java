package org.jpos.util;

import java.util.Map;
import java.util.Hashtable;

/**
 * Allow runtime binding of jPOS's components (ISOChannels, Logger, MUXes, etc)
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class NameRegistrar {
    private static NameRegistrar instance = null;
    private Map registrar;

    public static class NotFoundException extends Exception {
	public NotFoundException() {
	    super();
	}
	public NotFoundException(String detail) {
	    super(detail);
	}
    }

    private NameRegistrar() {
	super();
	registrar = new Hashtable();
    }
    private static Map getMap() {
	return getInstance().registrar;
    }
    /**
     * @return singleton instance
     */
    private static NameRegistrar getInstance() {
        if (instance == null) {
            synchronized (NameRegistrar.class) {
                if (instance == null) 
                    instance = new NameRegistrar();
            }
        }
        return instance;
    }
    /**
     * register object
     * @param key - key with which the specified value is to be associated.
     * @param value - value to be associated with the specified key
     */
    public static void register (String key, Object value) {
	getMap().put (key, value);
    }
    /**
     * @param key key whose mapping is to be removed from registrar.
     */
    public static void unregister (String key) {
	getMap().remove (key);
    }
    /**
     * @param key key whose associated value is to be returned.
     * @throws NotFoundException if key not present in registrar
     */
    public static Object get (String key) throws NotFoundException {
	Object obj = getMap().get(key);
	if (obj == null)
	    throw new NotFoundException (key);
	return obj;
    }
}

