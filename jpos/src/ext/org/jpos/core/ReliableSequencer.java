/*
 * $Log$
 * Revision 1.6  2000/05/23 16:41:12  apr
 * now Configurable (required by QSP)
 *
 * Revision 1.5  2000/04/16 23:53:01  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.4  2000/03/01 14:44:38  apr
 * Changed package name to org.jpos
 *
 * Revision 1.3  2000/01/11 01:24:40  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.2  1999/12/21 13:54:33  apr
 * BugFix: get intValue()
 *
 * Revision 1.1  1999/12/21 12:15:09  apr
 * Added ReliableSequencer
 *
 */

package org.jpos.core;

import java.io.*;
import java.util.*;
import org.jpos.util.Loggeable;
import com.sun.jini.reliableLog.ReliableLog;
import com.sun.jini.reliableLog.LogException;
import com.sun.jini.reliableLog.LogHandler;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 *
 * A production grade Sequencer based on com.sun.jini.reliableLog
 * (you'll need a copy of sun-util.jar in your classpath in order 
 * to use it)
 *
 */
public class ReliableSequencer 
    extends LogHandler 
    implements Sequencer, Loggeable, Configurable
{
    private Map map;
    private ReliableLog log;
    public static final int MAXLOGSIZE = 200000;
    public ReliableSequencer () {
	map = new HashMap();
	log = null;
    }

    /**
     * constructs and setup a ReliableSequencer object
     */
    public static ReliableSequencer createInstance (String dir) 
	throws IOException
    {
	ReliableSequencer seq = new ReliableSequencer();
	ReliableLog log = new ReliableLog (dir, seq);
	log.recover();
	log.snapshot();
	seq.setReliableLog (log);
	return seq;
    }

    /**
     * @param cfg containing <code>logdir</code> property
     */
    public void setConfiguration (Configuration cfg) 
	throws ConfigurationException
    {
	try {
	    ReliableLog log = new ReliableLog (cfg.get("logdir"), this);
	    log.recover();
	    log.snapshot();
	    setReliableLog (log);
	} catch (IOException e) {
	    throw new ConfigurationException (e);
	}
    }

    /**
     * @param counterName
     * @param add increment
     * @return counterName's value + add
     */
    synchronized public int get (String counterName, int add) {
	int i = 0;
	Integer I = (Integer) map.get (counterName);
	if (I != null)
	    i = I.intValue();
	I = new Integer (i + add);
	map.put (counterName, I);
	try {
	    log.update (new LogEntry (counterName, I), true);
	    if (log.logSize() > MAXLOGSIZE)
		log.snapshot();
	} catch (LogException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return I.intValue();
    }
    /**
     * @param counterName
     * @return counterName's value + 1
     */
    public int get (String counterName) {
	return get (counterName, 1);
    }
    /**
     * @param counterName
     * @param newValue
     * @return oldValue
     */
    synchronized public int set (String counterName, int newValue) {
	Integer I = new Integer (newValue);
	Integer oldValue = (Integer) map.put (counterName, I);
	try {
	    log.update (new LogEntry (counterName, I), true);
	    if (log.logSize() > MAXLOGSIZE)
		log.snapshot();
	} catch (LogException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return oldValue == null ? 0 : oldValue.intValue();
    }

    /**
     * @param log an already recovered - ready to use ReliableLog
     */
    public void setReliableLog (ReliableLog log) {
	this.log = log;
    }

    public void snapshot(OutputStream out) throws Exception
    {
	ObjectOutputStream stream = new ObjectOutputStream(out);
	stream.writeUTF(this.getClass().getName());
	stream.writeObject(map);
	stream.writeObject(null);
	stream.flush();
    }
    public void recover(InputStream in) throws Exception
    {
	ObjectInputStream stream = new ObjectInputStream(in);
	if (!this.getClass().getName().equals(stream.readUTF()))
	    throw new IOException("log from wrong implementation");
	map = (Map) stream.readObject();
    }
    public void applyUpdate(Object update) throws Exception
    {
	if (!(update instanceof LogEntry))
	    throw new Exception ("not a LogEntry");

	LogEntry entry = (LogEntry) update;
	map.put (entry.key, entry.value);
    }

    public static class LogEntry implements Serializable {
	public Object key, value;
	public LogEntry (Object key, Object value) {
	    this.key = key;
	    this.value = value;
	}
    }
    
    public void dump (PrintStream p, String indent) {
	String inner = indent + "  ";
	p.println (indent + "<reliable-sequencer>");
	Iterator i = map.entrySet().iterator();
	while (i.hasNext()) {
	    Map.Entry e = (Map.Entry) i.next();
	    p.println (inner + "<seq name=\""+e.getKey()
	                      +"\" value=\""+e.getValue()+"\"/>"
	    );
	}

	p.println (indent + "</reliable-sequencer>");
    }

    public static int usage () {
	System.out.println ("Usage: ReliableLog logdir counterName intValue");
	return 1;
    }

    public static void main (String args[]) {
	if (args.length < 3)
	    System.exit (usage());

	String dir = args[0];
	String key = args[1];
	int val    = Integer.parseInt (args[2]);

	try {
	    ReliableSequencer seq = ReliableSequencer.createInstance(dir);
	    System.out.println (key + " was " + seq.set (key, val));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
