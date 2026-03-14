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

package org.jpos.core;


import java.util.Set;

/**
 *
 * CardAgents relies on a Configuration object to provide
 * runtime configuration parameters such as merchant number, etc.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 */
public interface Configuration {
    /** @param propertyName the property name
     * @return the property value, or an empty string if not found
     */
    String get(String propertyName);
    /**
     * Returns all property values with the given name.
     * @param propertyName the property name
     * @return all matching values, or a zero-length array
     */
    String[] getAll(String propertyName);
    /** @param propertyName the property name @return all values as int array */
    int[] getInts(String propertyName);
    /** @param propertyName the property name @return all values as long array */
    long[] getLongs(String propertyName);
    /** @param propertyName the property name @return all values as double array */
    double[] getDoubles(String propertyName);
    /** @param propertyName the property name @return all values as boolean array */
    boolean[] getBooleans(String propertyName);
    /** @param propertyName the property name @param defaultValue value to return if not found @return the value or defaultValue */
    String get(String propertyName, String defaultValue);
    /** @param propertyName the property name @return the value as an int, or 0 if not found */
    int getInt(String propertyName);
    /** @param propertyName the property name @param defaultValue default if not found @return the value as int */
    int getInt(String propertyName, int defaultValue);
    /** @param propertyName the property name @return the value as a long, or 0 if not found */
    long getLong(String propertyName);
    /** @param propertyName the property name @param defaultValue default if not found @return the value as long */
    long getLong(String propertyName, long defaultValue);
    /** @param propertyName the property name @return the value as a double, or 0.0 if not found */
    double getDouble(String propertyName);
    /** @param propertyName the property name @param defaultValue default if not found @return the value as double */
    double getDouble(String propertyName, double defaultValue);
    /** @param propertyName the property name @return the value as a boolean, or false if not found */
    boolean getBoolean(String propertyName);
    /** @param propertyName the property name @param defaultValue default if not found @return the value as boolean */
    boolean getBoolean(String propertyName, boolean defaultValue);
    /**
     * Stores a property value.
     * @param name the property name
     * @param value the value (typically a String or String[])
     */
    void put(String name, Object value);
    /** @return the set of all property names in this configuration */
    Set<String> keySet();
}
