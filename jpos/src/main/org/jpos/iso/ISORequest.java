/**
 * Representa una transaccion ISO-8583 a traves de una pareja
 * de <code>ISOMsg</code>s (request/response).
 * 
 * <code>ISORequest</code> envia peticiones a <code>ISOMUX</code>.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOMsg
 * @see ISOException
 * @see ISOChannel
 * @see ISOMUX
 */

/*
 * $Log$
 * Revision 1.1  1998/11/09 23:40:33  apr
 * *** empty log message ***
 *
 */

package uy.com.cs.jpos.iso;

import java.util.*;

public class ISORequest {
	private ISOMsg request, response;
	private long requestTime, responseTime;
	private boolean expired;


	public ISORequest (ISOMsg m) {
		request = m;
		Date d = new Date();
		requestTime = d.getTime();
		expired = false;
	}
	public boolean isExpired() {
		return expired;
	}
	public void setExpired(boolean b) {
		expired = b;
	}
	public void queue (ISOMUX mux) {
		mux.queue(this);
	}
	public ISOMsg getRequest() {
		return request;
	}
	/**
	 * @param timeout	timeout in milliseconds
	 * @return ISOMsg or null
	 */
	public ISOMsg getResponse(int timeout) {
		synchronized (this) {
			try {
				if (timeout > 0)
					wait(timeout);
				else
					wait();
			} catch (InterruptedException e) { }
			setExpired (response == null);
		}
		return response;
	}

	public void setResponse(ISOMsg m) {
		Date d = new Date();
		responseTime = d.getTime();
		synchronized (this) {
			response = m;
			this.notify();
		}
	}
}
