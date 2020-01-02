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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 */
public class SimpleConfiguration implements Configuration, Serializable {
    private Properties props;

    public SimpleConfiguration () {
        props = new Properties();
    }
    public SimpleConfiguration (Properties props) {
        this.props = props;
    }
    public SimpleConfiguration (String filename)
        throws IOException
    {
        props = new Properties();
        load (filename);
    }

    /**
     * Returns the value of the configuration property named <tt>name</tt>, or the default value <tt>def</tt>.
     *
     * If the property value has the format <code>${xxx}</code> then its value is taken from a system property
     * if it exists, or an environment variable. System property takes priority over environment variable.
     *
     * If the format is <code>$sys{...}</code> we read only a system property.
     * if the format is <code>$env{...}</code> only an environment variable is used.
     *
     * @param name The configuration property key name.
     * @param def  The default value.
     * @return  The value stored under <tt>name</tt>,
     *          or <tt>def</tt> if there's no configuration property under the given <tt>name</tt>.
     */
    public String get (String name, String def) {
        Object obj = props.get (name);
        if (obj instanceof String[]) {
            String[] arr= (String[]) obj;
            obj = arr.length > 0 ? arr[0] : null;
        } else if (obj instanceof List) {
            List l = (List) obj;
            obj = l.size() > 0 ? l.get(0) : null;
        }
        return (obj instanceof String) ? Environment.get((String) obj, def) : def;
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

        Environment env = Environment.getEnvironment();
        IntStream.range(0, ret.length).forEachOrdered(i -> ret[i] = env.getProperty(ret[i]));
        return Arrays.stream(ret).filter(Objects::nonNull).toArray(String[]::new);
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
            dd[i] = Double.valueOf(ss[i].trim());
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
        return get(name, "");
    }
    public int getInt (String name) {
        return Integer.parseInt(get(name, "0").trim());
    }
    public int getInt (String name, int def) {
        return Integer.parseInt(get(name, Integer.toString(def)).trim());
    }
    public long getLong (String name) {
        return Long.parseLong(get(name, "0").trim());
    }
    public long getLong (String name, long def) {
        return Long.parseLong(get(name, Long.toString(def)).trim());
    }
    public double getDouble(String name) {
        return Double.valueOf(get(name, "0.00").trim());
    }
    public double getDouble(String name, double def) {
        return Double.valueOf(get(name, Double.toString(def)).trim());
    }
    public boolean getBoolean (String name) {
        String v = get (name, "false").trim();
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes");
    }
    public boolean getBoolean (String name, boolean def) {
        String v = get (name);
        return v.length() == 0 ? def :
                v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes");
    }
    public void load(String filename) 
        throws IOException
    {
        FileInputStream fis = new FileInputStream(filename);
        props.load(new BufferedInputStream(fis));
        fis.close();
    }
    public synchronized void put (String name, Object value) {
        props.put (name, value);
    }
    @Override
    public Set<String> keySet() {
        return props.stringPropertyNames();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleConfiguration that = (SimpleConfiguration) o;
        return Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hash(props);
    }

    @Override
    public String toString() {
        return "SimpleConfiguration{" +
          "props=" + props +
          '}';
    }

    private static final long serialVersionUID = -6361797037366246968L;
}
