package uy.com.cs.jpos.util;

import java.io.*;
import java.util.*;

/*
 * $Log$
 * Revision 1.1  2000/01/11 01:24:59  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.2  1999/11/18 23:31:39  apr
 * Changed rotate time from milliseconds to seconds
 *
 */
public class RotateLogListener extends SimpleLogListener 
    implements Runnable
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
