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

import java.util.HashSet;
import java.util.Set;

/**
 * SubConfiguration objects lets childs objects access attributes
 * in its parent object with a given prefix, for example "child.".
 * Child objects can access properties with their prefix removed.
 *
 * @author <a href="mailto:alcarraz@iie.edu.uy">Andr?s Alcarraz</a>
 * @version $Id$
 */
public class SubConfiguration implements Configuration {
    /** Parent Configuration */
    protected Configuration cfg;
    /** prefix identifying the child parameters */
    protected String prefix;
    /** Creates a new empty SubConfiguration object */
    public SubConfiguration() {
        super();
    }
    /**
     * Creates a new SubConfiguration from its parent's Configuration 
     * and the a given <code>prefix</code>.
     * @param cfg Parent's Configuration 
     * @param prefix prefix identifying child parameters.
     */
    public SubConfiguration(Configuration cfg, String prefix) {
        super ();
        this.cfg = cfg;
        this.prefix = prefix;
    }
    /**
     * Sets the container configuration.
     * @param newCfg New container configuration.
     */
    public void setConfiguration(Configuration newCfg){
        cfg=newCfg;
    }
    /**
     * Sets the prefix that identifies the parameters of the child object
     * inside the parent configuration.
     * @param newPrefix New prefix
     */
    public void setPrefix(String newPrefix){
        prefix = newPrefix;
    }
    public String get(String propertyName){
        return cfg.get(prefix + propertyName);
    }
    public String[] getAll(String propertyName){
        return cfg.getAll(prefix + propertyName);
    }
    public int[] getInts (String propertyName) {
        return cfg.getInts (prefix + propertyName);
    }
    public long[] getLongs (String propertyName) {
        return cfg.getLongs (prefix + propertyName);
    }
    public double[] getDoubles (String propertyName) {
        return cfg.getDoubles (prefix + propertyName);
    }
    public boolean[] getBooleans (String propertyName) {
        return cfg.getBooleans(prefix + propertyName);
    }
    public String get(String propertyName, String defaultValue){
        return cfg.get(prefix + propertyName, defaultValue);
    }
    public boolean getBoolean(String propertyName){
        return cfg.getBoolean(prefix + propertyName);
    }
    public boolean getBoolean(String propertyName, boolean defaultValue){
        return cfg.getBoolean(prefix + propertyName, defaultValue);
    }
    public double getDouble(String propertyName){
        return cfg.getDouble(prefix + propertyName);
    }
    public double getDouble(String propertyName, double defaultValue){
        return cfg.getDouble(prefix + propertyName, defaultValue);
    }
    public long getLong(String propertyName){
        return cfg.getLong(prefix + propertyName);
    }
    public long getLong(String propertyName, long defaultValue){
        return cfg.getLong(prefix + propertyName, defaultValue);
    }
    public int getInt(String propertyName){
        return cfg.getInt(prefix + propertyName);
    }
    public int getInt(String propertyName, int defaultValue){
        return cfg.getInt(prefix + propertyName, defaultValue);
    }
    public void put (String name, Object value) {
        cfg.put(prefix + name, value);
    }
    /**
     * Creates a new object, it takes the class from the value of the property
     * <code>propertyName</code>
     * @param propertyName Property whose value is the class name of  
     * the object being created.
     */
    public Object getObject (String propertyName) throws ConfigurationException{
        try{
            Object ret = 
                Class.forName (get (propertyName)).newInstance();
            if(ret instanceof Configurable) 
                ((Configurable)ret).setConfiguration(this);
            return ret;
        } catch (Exception e){
            throw new ConfigurationException ("Error trying to create an " 
                                             + "object from property " 
                                             + prefix + propertyName, e
                                             );
        }
    }
    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        for (String k : cfg.keySet())
            if (k.startsWith(prefix))
                keys.add(k);

        return keys;
    }
}
