/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpos.util;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Allow runtime binding of jPOS's components (ISOChannels, Logger, MUXes, etc)
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class NameRegistrar implements Loggeable {
    private static NameRegistrar instance = new NameRegistrar();
    private ConcurrentMap<String, Object> registrar;

    public static class NotFoundException extends Exception {
        private static final long serialVersionUID = 8744022794646381475L;

        public NotFoundException() {
            super();
        }

        public NotFoundException(String detail) {
            super(detail);
        }
    }

    private NameRegistrar() {
        super();
        registrar = new ConcurrentHashMap<String, Object>();
    }

    public static ConcurrentMap<String, Object> getMap() {
        return getInstance().registrar;
    }

    /**
     * @return singleton instance
     */
    public static NameRegistrar getInstance() {
        return instance;
    }

    /**
     * register object
     * 
     * @param key
     *            - key with which the specified value is to be associated.
     * @param value
     *            - value to be associated with the specified key
     */
    public static void register(String key, Object value) {
        getMap().put(key, value);
    }

    /**
     * @param key
     *            key whose mapping is to be removed from registrar.
     */
    public static void unregister(String key) {
        getMap().remove(key);
    }

    /**
     * @param key
     *            key whose associated value is to be returned.
     * @throws NotFoundException
     *             if key not present in registrar
     */
    public static Object get(String key) throws NotFoundException {
        Object obj = getMap().get(key);
        if (obj == null) {
            throw new NotFoundException(key);
        }
        return obj;
    }

    /**
     * @param key
     *            key whose associated value is to be returned, null if not present.
     */
    public static Object getIfExists(String key) {
        return getMap().get(key);
    }

    public void dump(PrintStream p, String indent) {
        dump(p, indent, false);
    }

    public void dump(PrintStream p, String indent, boolean detail) {
        String inner = indent + "  ";
        p.println(indent + "--- name-registrar ---");
        for (Map.Entry<String, Object> entry : registrar.entrySet()) {
            Object obj = entry.getValue();
            String key = entry.getKey();
            if (key == null) {
                key = "null";
            }
            String objectClassName = (obj == null) ? "<NULL>" : obj.getClass().getName();
            p.println(inner + key.toString() + ": " + objectClassName);
            if (detail && obj instanceof Loggeable) {
                ((Loggeable) obj).dump(p, inner + "  ");
            }
        }
    }
}
