package org.jpos.tpl;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 */
public class NotFoundException extends Exception {
    public NotFoundException () {
	super();
    }
    public NotFoundException (String detail) {
	super(detail);
    }
}
