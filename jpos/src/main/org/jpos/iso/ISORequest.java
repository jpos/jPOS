/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.iso;

import java.io.*;
import java.util.*;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;
import org.jpos.util.LogSource;

/*
 * $Log$
 * Revision 1.12  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.11  2000/04/16 23:53:08  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.10  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.9  2000/01/11 01:24:48  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
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
public class ISORequest implements LogSource, Loggeable {
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
