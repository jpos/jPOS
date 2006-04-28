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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 */
public class SimpleConfiguration implements Configuration {
    private Properties props;

    public SimpleConfiguration () {
        props = new Properties();
    }
    public SimpleConfiguration (Properties props) {
        this.props = props;
    }
    public SimpleConfiguration (String filename) 
        throws FileNotFoundException, IOException
    {
        props = new Properties();
        load (filename);
    }
    synchronized public String get (String name, String def) {
        Object obj = props.get (name);
        if (obj instanceof List) {
            List l = (List) obj;
            obj = (l.size() > 0) ? l.get(0) : null;
        }
        return (obj instanceof String) ? ((String) obj) : def;
    }
    synchronized public String[] getAll (String name) {
        String[] ret;
        Object obj = props.get (name);
        if (obj instanceof String[]) {
            ret = (String[]) obj;
        } else if (obj instanceof String) {
            ret = new String[1];
            ret[0] = (String) obj;
        } else
            ret = new String[0];

        return ret;
    }
    synchronized public String get (String name) {
        return get (name, "");
    }
    synchronized public int getInt (String name) {
        return Integer.parseInt(props.getProperty(name, "0").trim());
    }
    synchronized public int getInt (String name, int def) {
        return Integer.parseInt(
            props.getProperty (name, Integer.toString (def)).trim());
    }
    synchronized public long getLong (String name) {
        return Long.parseLong(props.getProperty(name, "0").trim());
    }
    synchronized public long getLong (String name, long def) {
        return Long.parseLong (
            props.getProperty (name, Long.toString (def)).trim());
    }
    synchronized public double getDouble(String name) {
        return Double.valueOf(
            props.getProperty(name,"0.00").trim()).doubleValue();
    }
    synchronized public double getDouble(String name, double def) {
        return Double.valueOf(
            props.getProperty(name,Double.toString(def)).trim()).doubleValue();
    }
    public boolean getBoolean (String name) {
        String v = get (name, "false").trim();
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes");
    }
    public boolean getBoolean (String name, boolean def) {
        String v = get (name);
        return v.length() == 0 ? def :
            (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes"));
    }
    synchronized public void load(String filename) 
        throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(filename);
        props.load(new BufferedInputStream(fis));
        fis.close();
    }
    synchronized public void put (String name, Object value) {
        props.put (name, value);
    }
}
