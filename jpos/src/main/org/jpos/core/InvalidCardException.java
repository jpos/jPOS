/**
 * @author apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1999/09/26 19:54:07  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;
import java.io.*;
import uy.com.cs.jpos.iso.ISODate;

public class InvalidCardException extends Exception {
    public InvalidCardException () {
	super();
    }
    public InvalidCardException (String s) {
	super(s);
    }
}
