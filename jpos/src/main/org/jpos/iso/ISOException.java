package org.jpos.iso;

import java.io.PrintStream;
import org.jpos.util.Loggeable;

/**
 * Signals that an ISO exception of some sort has occurred. 
 *
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ISOException extends Exception implements Loggeable {
    /**
     * @serial
     */
    Exception nested = null;

    /**
     * Constructs an <code>ISOException</code> with no detail message. 
     */
    public ISOException() {
        super();
    }

    /**
     * Constructs an <code>ISOException</code> with the specified detail 
     * message. 
     *
     * @param   s   the detail message.
     */
    public ISOException(String s) {
        super(s);
    }

    /**
     * Constructs an <code>ISOException</code> with a nested
     * exception
     * @param nested another exception
     */
    public ISOException (Exception nested) {
	super(nested.toString());
	this.nested = nested;
    }

    /**
     * Constructs an <code>ISOException</code> with a detail Message nested
     * exception
     * @param   s   the detail message.
     * @param nested another exception
     */
    public ISOException (String s, Exception nested) {
	super(s);
	this.nested = nested;
    }

    /**
     * @return nested exception (may be null)
     */
    public Exception getNested() {
	return nested;
    }

    public void dump (PrintStream p, String indent) {
	String inner = indent + "  ";
	p.println (indent + "<isoexception>");
	p.println (inner  + getMessage());
	if (nested != null) {
	    if (nested instanceof ISOException) 
		((ISOException)nested).dump (p, inner);
	    else {
		p.println (inner + "<nested-exception>");
		p.print   (inner);
		nested.printStackTrace (p);
		p.println (inner + "</nested-exception>");
	    }
	}
	p.print (inner);
	printStackTrace (p);
	p.println (indent + "</isoexception>");
    }
}
