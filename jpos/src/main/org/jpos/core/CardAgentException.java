package org.jpos.core;
import org.jpos.iso.ISOException;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 */
public class CardAgentException extends ISOException {
    public CardAgentException () {
	super();
    }
    public CardAgentException (String s) {
	super(s);
    }
    public CardAgentException (Exception e) {
	super(e);
    }
    public CardAgentException (String s, Exception e) {
	super(s, e);
    }
    protected String getTagName() {
	return "card-agent-exception";
    }
}
