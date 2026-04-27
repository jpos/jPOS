/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

@SuppressWarnings("unchecked")
public class SpaceUtil {
    /** Default constructor; no instance state to initialise. */
    public SpaceUtil() {}
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
            ; // NOPMD
    }

    /**
     * @param sp the Space
     * @param key entry's key
     * @param value entry's value
     * @deprecated Use space.put instead
     */
    @Deprecated
    public static void wipeAndOut  (Space sp, Object key, Object value) {
        sp.put(key, value);
    }

    /**
     * @param sp the Space
     * @param key entry's key
     * @param value entry's value
     * @param timeout entry lifespan in milliseconds
     * @deprecated use space.put instead
     */
    @Deprecated
    public static void wipeAndOut  (Space sp, Object key, Object value, long timeout) {
        sp.out(key, value, timeout);
    }
    /**
     * Atomically increments and returns the long counter stored under {@code key}.
     *
     * @param sp the Space
     * @param key counter's key
     * @return the new counter value
     */
    public static long nextLong (Space sp, Object key) {
        long l = 0L;
        synchronized (sp) {
            Object obj = sp.inp (key);
            wipe (sp, key); // just in case
            if (obj instanceof Long)
                l = (Long) obj;
            sp.out (key, ++l);
        }
        return l;
    }
    /**
     * Writes {@code value} only when the slot at {@code key} is empty after an {@code nrd} probe.
     *
     * @param sp the Space
     * @param key entry's key
     * @param value entry's value
     * @param nrdTimeout how long {@code nrd} waits for the slot to become non-empty
     * @param outTimeout entry lifespan once written
     * @return {@code true} if the value was written, {@code false} if the slot was already populated
     */
    public static boolean outIfEmpty (Space sp, Object key, Object value, long nrdTimeout, long outTimeout) {
        synchronized (sp) {
            if (sp.nrd(key, nrdTimeout) == null) {
                sp.out(key, value, outTimeout);
                return true;
            }
        }
        return false;
    }
    /**
     * Blocks until the slot at {@code key} is empty, then writes {@code value} with the given lifespan.
     *
     * @param sp the Space
     * @param key entry's key
     * @param value entry's value
     * @param timeout entry lifespan once written
     */
    public static void outWhenEmpty (Space sp, Object key, Object value, long timeout) {
        synchronized (sp) {
            sp.nrd(key);
            sp.out(key, value, timeout);
        }
    }
    /**
     * Blocks until the slot at {@code key} is empty, then writes {@code value}.
     *
     * @param sp the Space
     * @param key entry's key
     * @param value entry's value
     */
    public static void outWhenEmpty (Space sp, Object key, Object value) {
        synchronized (sp) {
            sp.nrd(key);
            sp.out(key, value);
        }
    }
}
