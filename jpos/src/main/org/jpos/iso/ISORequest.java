package uy.com.cs.jpos.iso;

import java.util.*;

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
public class ISORequest {
    private ISOMsg request, response;
    private long requestTime, responseTime;
    private boolean expired;

    /**
     * creates an ISORequest suitable to be queued to an ISOMUX
     * @param m - the Request Message
     */
    public ISORequest (ISOMsg m) {
        request = m;
        Date d = new Date();
        requestTime = d.getTime();
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
     * wait for a response to arrive. 
     * ISOMUX will notify this object when the response message is ready.
     *
     * @param timeout   timeout in milliseconds. 0 blocks forever
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
}
