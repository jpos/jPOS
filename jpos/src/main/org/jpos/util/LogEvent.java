package uy.com.cs.jpos.util;

import java.io.*;
import java.util.*;
import java.sql.SQLException;


/*
 * $Log$
 * Revision 1.1  2000/01/11 01:24:57  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.4  1999/11/24 18:04:39  apr
 * minor DOC changes
 *
 * Revision 1.3  1999/11/18 23:39:03  apr
 * Added addMessage(tag,message) and SQLException support
 *
 * Revision 1.2  1999/09/21 21:33:19  apr
 * LogEvent now extends EventObject and LogListener is an EventListener
 *
 */

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @serial
 */
public class LogEvent extends EventObject {
    LogProducer source;
    public String tag;
    public Vector payLoad;
    
    public LogEvent (LogProducer source, String tag) {
	super (source);
	this.source  = source;
	this.tag     = tag;
	this.payLoad = new Vector();
    }
    public LogEvent (LogProducer source, String tag, Object msg) {
	super (source);
	this.source  = source;
	this.tag     = tag;
	this.payLoad = new Vector(1);
	addMessage(msg);
    }
    public void addMessage (Object msg) {
	this.payLoad.addElement(msg);
    }
    public void addMessage (String tagname, String message) {
	this.payLoad.addElement ("<"+tagname+">"+message+"<"+tagname+"/>");
    }
    public void dump (PrintStream p, String indent) {
	if (payLoad.size() == 0) 
	    p.println (indent + "<" + tag + "/>");
	else {
	    p.println (indent + "<" + tag + ">");
	    String newIndent = indent + "  ";
	    Iterator i = payLoad.iterator();
	    while (i.hasNext()) {
		Object o = i.next();
		if (o instanceof Loggeable) 
		    ((Loggeable)o).dump (p, newIndent);
		else if (o instanceof SQLException) {
		    SQLException e = (SQLException) o;
		    p.println (newIndent + "<SQLException>"
				+e.getMessage() + "</SQLException>");
		    p.println (newIndent + "<SQLState>"
				+e.getSQLState() + "</SQLState>");
		    p.println (newIndent + "<VendorError>"
				+e.getErrorCode() + "</VendorError>");
		}
		else if (o instanceof Throwable) {
		    p.println (newIndent + "<exception name=\""
			+ ((Throwable)o).getMessage() + "\">");
		    p.print (newIndent);
		    ((Throwable)o).printStackTrace (p);
		    p.println (newIndent + "</exception>");
		}
		else
		    p.println (newIndent + o.toString());
	    }
	    p.println (indent + "</" + tag + ">");
	}
    }
    public String getRealm() {
	return source.getRealm();
    }
}

