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

package org.jpos.space;

import java.util.ArrayList;
import java.util.List;

/**
 * Space related helper methods
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 1.4.7
 */

public class SpaceUtil {
    /**
     * return all entries under a given key
     *
     * @param sp the Space
     * @param key Entry's key
     * @return array containing all entries under key
     */
    public static Object[] inpAll (Space sp, Object key) {
        List list = new ArrayList();
        Object value;
        do {
            value = sp.inp (key);
            if (value != null) {
                list.add (value);
            }
        } while (value != null);
        return list.toArray();
    }

    /**
     * Remove all entries under key
     *
     * @param sp the Space
     * @param key Entry's key
     */
    public static void wipe (Space sp, Object key) {
        while (sp.inp (key) != null)
            ;
    }
    public static void wipeAndOut  (Space sp, Object key, Object value) {
        synchronized (sp) {
            wipe (sp, key);
            sp.out (key, value);
        }
    }
    public static void wipeAndOut  (Space sp, Object key, Object value, long timeout) 
    {
        synchronized (sp) {
            wipe (sp, key);
            sp.out (key, value, timeout);
        }
    }
    public static long nextLong (Space sp, Object key) {
        long l = 0L;
        synchronized (sp) {
            Object obj = sp.inp (key);
            wipe (sp, key); // just in case
            if (obj instanceof Long) 
                l = ((Long)obj).longValue();
            sp.out (key, new Long (++l));
        }
        return l;
    }
}

