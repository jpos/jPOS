/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso;

import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;

import java.io.PrintStream;
import java.util.Date;

/*
 * $Log$
 * Revision 1.15  2003/10/13 10:34:15  apr
 * Tabs expanded to 8 spaces
 *
 * Revision 1.14  2003/05/16 04:14:05  alwyns
 * Import cleanups.
 *
 * Revision 1.13  2003/05/16 00:28:27  apr
 * Added long getResponseTime() (suggested by Kris - good idea!)
 *
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
    /**
     * @return time in milliseconds of how long it took to get a Response
     */
    public long getResponseTime () {
        return responseTime-requestTime;
    }
}

