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

package org.jpos.core;


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
    /**
     * @param propertyName  ditto
     * @param propertyValue ditto
     * @return all properties with a given name (or a zero length string)
     */
    public String[] getAll  (String propertyName);
    public int[] getInts  (String propertyName);
    public long[] getLongs (String propertyName);
    public double[] getDoubles (String propertyName);
    public boolean[] getBooleans (String propertyName);
    public String get       (String propertyName, String defaultValue);
    public int getInt       (String propertyName);
    public int getInt       (String propertyName, int defaultValue);
    public long getLong     (String propertyName);
    public long getLong     (String propertyName, long defaultValue);
    public double getDouble (String propertyName);
    public double getDouble (String propertyName, double defaultValue);
    public boolean getBoolean (String propertyName);
    public boolean getBoolean (String propertyName, boolean defaultValue);
    /**
     * @param name the Property name
     * @param value typically a String, but could be a String[] too
     */
    public void put (String name, Object value);
}

