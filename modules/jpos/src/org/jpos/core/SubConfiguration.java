/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.core;


/**
 * SubConfiguration objects lets childs objects access attributes
 * in its parent object with a given prefix, for example "child.".
 * Child objects can access properties with their prefix removed.
 *
 * @author <a href="mailto:alcarraz@iie.edu.uy">Andr?s Alcarraz</a>
 * @version $Id$
 */
public class SubConfiguration implements Configuration{
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
        cfg.put (prefix + name, value);
    }
    /**
     * Creates a new object, it takes the class from the value of the property
     * <code>propertyName</code>
     * @param propertyName Property whose value is the class name of  
     * the object being created.
     */
    public Object getObject (String propertyName) throws ConfigurationException{
        try{
            Class[] paramTypes = {};
            Object[] params = {};
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
}

