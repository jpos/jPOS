
/*
 * $Log$
 * Revision 1.2  1999/11/11 10:18:49  apr
 * added get(name,name), getInt(name), getLong(name) and getDouble(name)
 *
 * Revision 1.1  1999/09/26 22:32:01  apr
 * CVS sync
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.util.*;

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
    synchronized public String get (String name) {
	return props.getProperty (name, "");
    }
    synchronized public String get (String name, String def) {
	return props.getProperty (name, def);
    }
    synchronized public int getInt (String name) {
        return Integer.parseInt(props.getProperty(name, "0").trim());
    }
    synchronized public long getLong (String name) {
        return Long.parseLong(props.getProperty(name, "0").trim());
    }
    synchronized public double getDouble(String name) {
        return Double.valueOf(
	    props.getProperty(name,"0.00").trim()).doubleValue();
    }
    synchronized public void load(String filename) 
	throws FileNotFoundException, IOException
    {
	FileInputStream fis = new FileInputStream(filename);
	props.load(new BufferedInputStream(fis));
        fis.close();
    }
}
