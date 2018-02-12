/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

import org.jpos.space.SpaceUtil;
import org.jpos.space.TSpace;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Allow runtime binding of jPOS's components (ISOChannels, Logger, MUXes, etc)
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class NameRegistrar implements Loggeable {
    private static final NameRegistrar instance = new NameRegistrar();
    private static final TSpace<String, Object> sp = new TSpace<String,Object>();

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
    }

    public static TSpace<String, Object> getSpace() {
        return sp;
    }

    /**
     * @return a copy of the NameRegistrar's entries as a Map
     */
    public static Map<String,Object> getAsMap() {
        Map<String,Object> map = new HashMap<String,Object>();
        for (String k : sp.getKeySet()) {
            Object v  = sp.rdp(k);
            if (v != null)
                map.put(k,v);
        }
        return map;
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
        sp.put(key, value);
    }

    /**
     * @param key
     *            key whose mapping is to be removed from registrar.
     */
    public static void unregister(String key) {
        SpaceUtil.wipe(sp, key);
    }

    /**
     * @param key
     *            key whose associated value is to be returned.
     * @throws NotFoundException
     *             if key not present in registrar
     */
    public static Object get(String key) throws NotFoundException {
        Object obj = sp.rdp(key);
        if (obj == null) {
            throw new NotFoundException(key);
        }
        return obj;
    }

    public static Object get(String key, long timeout) {
        return sp.rd (key, timeout);
    }

    /**
     * @param key
     *            key whose associated value is to be returned, null if not present.
     */
    public static Object getIfExists(String key) {
        return sp.rdp(key);
    }

    public void dump(PrintStream p, String indent) {
        dump(p, indent, false);
    }

    public void dump(PrintStream p, String indent, boolean detail) {
        String inner = indent + "  ";
        p.println(indent + "name-registrar:");
        for (String key : sp.getKeySet()) {
            Object obj = sp.rdp(key);
            String objectClassName = obj == null ? "<NULL>" : obj.getClass().getName();
            p.println(inner + key + ": " + objectClassName);
            if (detail && obj instanceof Loggeable) {
                ((Loggeable) obj).dump(p, inner + "  ");
            }
        }
    }
}
