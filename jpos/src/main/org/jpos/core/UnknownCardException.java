/*
 * $Log$
 * Revision 1.1  1999/10/08 12:53:59  apr
 * Devel intermediate version - CVS sync
 *
 * Revision 1.2  1999/09/26 22:32:01  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:07  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 */
public class UnknownCardException extends Exception {
    public UnknownCardException () {
	super();
    }
    public UnknownCardException (String s) {
	super(s);
    }
}
