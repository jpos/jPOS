package org.jpos.util;

import java.io.PrintStream;
import org.jpos.iso.ISOException;

/**
 * AssertFailedException
 * @author  <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class AssertFailedException extends ISOException {
    public AssertFailedException() {
        super();
    }
    public AssertFailedException (String s) {
        super(s);
    }
    public AssertFailedException (Exception nested) {
	super(nested);
    }
    public AssertFailedException (String s, Exception nested) {
	super(s, nested);
    }
}
