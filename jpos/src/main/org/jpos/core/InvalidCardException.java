/*
 * $Log$
 * Revision 1.2  1999/09/26 22:32:01  apr
 * CVS sync
 *
 * Revision 1.1  1999/09/26 19:54:07  apr
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
public class InvalidCardException extends Exception {
    public InvalidCardException () {
	super();
    }
    public InvalidCardException (String s) {
	super(s);
    }
}
