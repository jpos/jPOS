package uy.com.cs.jpos.iso;

import java.io.*;
import java.util.*;

/*
 * $Log$
 * Revision 1.8  1999/10/10 15:52:29  apr
 * Added ISORequest.isTransmitted() support
 *
 * Revision 1.7  1999/10/07 16:21:24  apr
 * Bugfix provided by georgem@tvinet.com to prevent race condition on
 * getResponse()
 *
 */

/**
 * link together a whole transaction, the requesting ISOMsg with the
 * corresponding response in a form suitable to be queued to an
 * <b>ISOMUX</b>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 * @see ISOMUX
 * @serial
 */
public class ISORequest implements LogProducer, Loggeable {
    private ISOMsg request, response;
    private long requestTime, txTime, responseTime;
    private boolean expired;
    private Logger logger = null;
    private String realm = null;

    /**
     * creates an ISORequest suitable to be queued to an ISOMUX
     * @param m - the Request Message
     */
    public ISORequest (ISOMsg m) {
        request = m;
        Date d = new Date();
        requestTime = d.getTime();
	txTime = 0;
        expired = false;
    }
    /**
     * checks if the message has expired
     */
    public boolean isExpired() {
        return expired;
    }
    /**
     * force this message as expired
     * (used internally by ISOMUX)
     */
    public void setExpired(boolean b) {
        expired = b;
    }
    /**
     * queue the message to a MUX.
     *
     * Warning: do not queue the same message to more than one MUX.
     * @param mux - an ISOMUX
     */
    public void queue (ISOMUX mux) {
        mux.queue(this);
    }
    /**
     * @return the request message
     */
    public ISOMsg getRequest() {
        return request;
    }
    /**
     * MUX calls setTransmitted when chances are really good
     */
    public void setTransmitted () {
	txTime = System.currentTimeMillis();
    }
    /**
     * @return true if this request was actually sent thru channel
     */
    public boolean isTransmitted() {
	return txTime != 0;
    }
	
    /**
     * wait for a response to arrive. 
     * ISOMUX will notify this object when the response message is ready.
     *
     * @param timeout   timeout in milliseconds. 0 blocks forever
     * @return ISOMsg or null
     */
    public ISOMsg getResponse(int timeout) {
        synchronized (this) {
	    if (response == null) {
		try {
		    if (timeout > 0)
			wait(timeout);
		    else
			wait();
		} catch (InterruptedException e) { }
	    }
	    setExpired (response == null);
        }
	Logger.log (new LogEvent (this, "ISORequest", this));
        return response;
    }
    public void dump (PrintStream p, String indent) {
	String newIndent = indent + "  ";
	p.println (indent + "<request" +
	    (expired ? " expired=\"true\">" : ">"));
	request.dump (p, newIndent);
	p.println (indent + "</request>");
	if (response != null) {
	    p.println (indent + "<response elapsed=\""
		+(responseTime-requestTime)+ "\">");
	    response.dump (p, newIndent);
	    p.println (indent + "</response>");
	}
    }

    /**
     * called by ISOMUX to set the response and notify this
     * possibly waiting object.
     */
    public void setResponse(ISOMsg m) {
        Date d = new Date();
        responseTime = d.getTime();
        synchronized (this) {
            response = m;
            this.notify();
        }
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
}
