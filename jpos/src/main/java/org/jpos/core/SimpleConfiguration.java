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
    public String get (String name, String def) {
        Object obj = props.get (name);
        if (obj instanceof List) {
            List l = (List) obj;
            obj = (l.size() > 0) ? l.get(0) : null;
        }
        return (obj instanceof String) ? ((String) obj) : def;
    }
    public String[] getAll (String name) {
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
    public int[] getInts (String name) {
        String[] ss = getAll (name);
        int[] ii = new int[ss.length];
        for (int i=0; i<ss.length; i++)
            ii[i] = Integer.parseInt(ss[i].trim());
        return ii;
    }
    public long[] getLongs (String name) {
        String[] ss = getAll (name);
        long[] ll = new long[ss.length];
        for (int i=0; i<ss.length; i++)
            ll[i] = Long.parseLong(ss[i].trim());
        return ll;
    }
    public double[] getDoubles (String name) {
        String[] ss = getAll (name);
        double[] dd = new double[ss.length];
        for (int i=0; i<ss.length; i++)
            dd[i] = Double.valueOf(ss[i].trim()).doubleValue();
        return dd;
    }
    public boolean[] getBooleans (String name) {
        String[] ss = getAll (name);
        boolean[] bb = new boolean[ss.length];
        for (int i=0; i<ss.length; i++)
            bb[i] = ss[i].equalsIgnoreCase("true") || ss[i].equalsIgnoreCase("yes");
        return bb;
    }
    public String get (String name) {
        return get (name, "");
    }
    public int getInt (String name) {
        return Integer.parseInt(props.getProperty(name, "0").trim());
    }
    public int getInt (String name, int def) {
        return Integer.parseInt(
            props.getProperty (name, Integer.toString (def)).trim());
    }
    public long getLong (String name) {
        return Long.parseLong(props.getProperty(name, "0").trim());
    }
    public long getLong (String name, long def) {
        return Long.parseLong (
            props.getProperty (name, Long.toString (def)).trim());
    }
    public double getDouble(String name) {
        return Double.valueOf(
            props.getProperty(name,"0.00").trim()).doubleValue();
    }
    public double getDouble(String name, double def) {
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
    public void load(String filename) 
        throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(filename);
        props.load(new BufferedInputStream(fis));
        fis.close();
    }
    public void put (String name, Object value) {
        props.put (name, value);
    }
}

