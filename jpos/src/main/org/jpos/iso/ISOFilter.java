package org.jpos.iso;

import org.jpos.util.LogEvent;

/**
 * An ISOFilter has the oportunity to modify an incoming or
 * outgoing ISOMsg that is about to go thru an ISOChannel.
 * It also has the chance to Veto by throwing an Exception
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface ISOFilter {
    public class VetoException extends ISOException {
	public VetoException () {
	    super();
	}
	public VetoException (String detail) {
	    super(detail);
	}
	public VetoException (Exception nested) {
	    super(nested);
	}
	public VetoException (String detail, Exception nested) {
	    super(detail, nested);
	}
    }
    /**
     * @param channel current ISOChannel instance
     * @param m ISOMsg to filter
     * @param evt LogEvent
     * @return an ISOMsg (possibly parameter m)
     * @throws VetoException
     */
    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
	throws VetoException;
}
