/**
 * Signals that an ISO exception of some sort has occurred. 
 *
 * @author  apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:25  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

public
class ISOException extends Exception {
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
}
