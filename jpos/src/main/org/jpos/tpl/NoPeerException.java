package org.jpos.tpl;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 */
public class NoPeerException extends Exception {
    public NoPeerException (String detail) {
	super(detail);
    }
}
