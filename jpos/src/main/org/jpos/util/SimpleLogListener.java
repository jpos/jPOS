package org.jpos.util;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see Configurable
 * @since jPOS 1.2
 */
public class SimpleLogListener implements LogListener {
    PrintStream p;

    public SimpleLogListener () {
	super();
	p = System.out;
    }
    public SimpleLogListener (PrintStream p) {
	super();
	setPrintStream (p);
    }
    public synchronized void setPrintStream (PrintStream p) {
	this.p = p;
    }
    public synchronized void close() {
	if (p != null) {
	    p.close();
	    p = null;
	}
    }
    public synchronized void log (LogEvent ev) {
	if (p != null) {
	    p.println (
		"<log realm=\"" +ev.getRealm()+ "\" at=\""+(new Date())+"\">"
	    );
	    ev.dump (p, "  ");
	    p.println ("</log>");
	    p.flush();
	}
    }
}
