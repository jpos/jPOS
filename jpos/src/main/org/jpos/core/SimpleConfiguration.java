
/*
 * $Log$
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
    synchronized public void load(String filename) 
	throws FileNotFoundException, IOException
    {
	FileInputStream fis = new FileInputStream(filename);
	props.load(new BufferedInputStream(fis));
        fis.close();
    }
}
