package org.jpos.util;

import java.io.*;
import java.util.*;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

/**
 * Rotates logs
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.core.Configurable
 * @since jPOS 1.2
 */

public class RotateLogListener extends SimpleLogListener 
    implements Runnable, Configurable
{
    FileOutputStream f;
    String logName;
    int maxCopies;
    int sleepTime;

    /**
     * @param name base log filename
     * @param t switch logs every t seconds
     * @param maxCopies number of old logs
     */
    public RotateLogListener (String logName, int t, int maxCopies) 
	throws IOException
    {
	super();
	this.logName   = logName;
	this.maxCopies = maxCopies;
	this.sleepTime = t;
	f = null;
	openLogFile ();
	if (t != 0) 
	    (new Thread(this)).start();
    }
    public RotateLogListener () {
	super();
	logName = null;
	maxCopies = 0;
	sleepTime = 0;
	f = null;
    }

   /**
    * Configure this RotateLogListener<br>
    * Properties:<br>
    * <ul>
    *  <li>file     base log filename
    *  <li>[window] in seconds (default 0 - never rotate)
    *  <li>[count]  number of copies (default 0 == single copy)
    * </ul>
    * @param cfg Configuration 
    * @throws ConfigurationException
    */
    public void setConfiguration (Configuration cfg)
	throws ConfigurationException
    {
	maxCopies = cfg.getInt ("copies");
	sleepTime = cfg.getInt ("window");
	logName   = cfg.get ("file");
	try {
	    openLogFile();
	} catch (IOException e) {
	    throw new ConfigurationException (e);
	}
	if (sleepTime != 0) 
	    (new Thread(this)).start();
    }
    private void openLogFile() throws IOException {
	f = new FileOutputStream (logName, true);
	setPrintStream (new PrintStream(f));
    }
    public synchronized void logRotate ()
	throws IOException
    {
	setPrintStream (null);
	super.close();
	f.close();
	for (int i=maxCopies; i>0; ) {
	    File dest   = new File (logName + "." + i);
	    File source = new File (logName + ((--i > 0) ? ("." + i) : ""));
	    dest.delete();
	    source.renameTo(dest);
	}
	openLogFile();
    }
    public void run () {
	for (;;) {
	    try {
		Thread.sleep (sleepTime * 1000);
		logRotate();
	    } catch (InterruptedException e) { 
	    } catch (IOException e) {
		e.printStackTrace (System.err);
	    }
	}
    }
}
