/**
 * @author apr@cs.com.uy
 * @version $Id$
 */

/*
 * $Log$
 * Revision 1.1  1999/09/26 19:54:04  apr
 * jPOS core 0.0.1 - setting up artifacts
 *
 */

package uy.com.cs.jpos.core;
import java.io.*;
import uy.com.cs.jpos.iso.ISODate;

public class CardAgentNotFoundException extends Exception {
    public CardAgentNotFoundException () {
	super();
    }
    public CardAgentNotFoundException (String s) {
	super(s);
    }
}
