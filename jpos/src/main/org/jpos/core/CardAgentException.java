/*
 * $Log$
 * Revision 1.2  1999/09/26 22:31:56  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:03  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;
import java.io.*;
import uy.com.cs.jpos.iso.ISODate;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 */
public class CardAgentException extends Exception {
    Exception e;
    public CardAgentException () {
	super();
    }
    public CardAgentException (String s) {
	super(s);
    }
    public CardAgentException (Exception e) {
	super();
	this.e = e;
    }
    public CardAgentException (String s, Exception e) {
	super(s);
	this.e = e;
    }
    public Exception getNested() {
	return e;
    }
}
