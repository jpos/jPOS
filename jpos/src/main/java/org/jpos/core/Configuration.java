/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.core;


import java.util.Set;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * CardAgents relies on a Configuration object to provide
 * runtime configuration parameters such as merchant number, etc.
 */
public interface Configuration {
    String get(String propertyName);
    /**
     * @param propertyName  ditto
     * @return all properties with a given name (or a zero-length array)
     */
    String[] getAll(String propertyName);
    int[] getInts(String propertyName);
    long[] getLongs(String propertyName);
    double[] getDoubles(String propertyName);
    boolean[] getBooleans(String propertyName);
    String get(String propertyName, String defaultValue);
    int getInt(String propertyName);
    int getInt(String propertyName, int defaultValue);
    long getLong(String propertyName);
    long getLong(String propertyName, long defaultValue);
    double getDouble(String propertyName);
    double getDouble(String propertyName, double defaultValue);
    boolean getBoolean(String propertyName);
    boolean getBoolean(String propertyName, boolean defaultValue);
    /**
     * @param name the Property name
     * @param value typically a String, but could be a String[] too
     */
    void put(String name, Object value);
    Set<String> keySet();
}
