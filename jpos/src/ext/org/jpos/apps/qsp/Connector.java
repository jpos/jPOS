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

package org.jpos.apps.qsp;

import java.io.IOException;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequest;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;

/**
 * QSP Connector implements ISORequestListener
 * and forward all incoming messages to a given
 * destination MUX, handling back responses
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.iso.ISORequestListener
 */
public class Connector 
    implements ISORequestListener, LogSource, Configurable
{
    Logger logger;
    String realm;
    ISOMUX destMux;
    ISOChannel destChannel;
    int timeout = 0;
    boolean bounce = false;
    static ThreadPool pool;
    static {
        pool = new ThreadPool (1, 100);
    }
    public Connector () {
        super();
        destMux = null;
        destChannel = null;
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
    * Destination can be a Channel or a MUX. If Destination is a Channel
    * then timeout applies (used on ISORequest to get a Response).
    * <ul>
    * <li>destination-mux
    * <li>destination-channel
    * <li>timeout
    * <li>bounce
    * </ul>
    * @param cfg Configuration
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        timeout = cfg.getInt ("timeout");
        bounce  = cfg.getBoolean ("bounce");
        String muxName     = cfg.get ("destination-mux", null);
        String channelName = cfg.get ("destination-channel", null);
        try {
            if (muxName != null)
                destMux = ISOMUX.getMUX (muxName);
            else if (channelName != null)
                destChannel = BaseChannel.getChannel (channelName);
        } catch (NotFoundException e) {
            throw new ConfigurationException (e);
        }
    }

    /**
     * hook used to optional bounce an unanswered message 
     * to its source channel
     * @param s message source
     * @param m unanswered message
     * @exception ISOException
     * @exception IOException
     */
    protected void processNullResponse (ISOSource s, ISOMsg m, LogEvent evt) 
        throws ISOException, IOException
    {
        if (bounce) {
            ISOMsg c = (ISOMsg) m.clone();
            c.setResponseMTI();
            if (c.hasField (39))
                c.unset (39);
            s.send (c);
            evt.addMessage ("<bounced/>");
        } else
            evt.addMessage ("<null-response/>");
    }

    protected class Process implements Runnable {
        ISOSource source;
        ISOMsg m;
        Process (ISOSource source, ISOMsg m) {
            super();
            this.source = source;
            this.m = m;
        }
        public void run () {
            LogEvent evt = new LogEvent (Connector.this, 
                "connector-request-listener");
            try {
                ISOMsg c = (ISOMsg) m.clone();
                evt.addMessage (c);
                if (destMux != null) {
                    if (timeout > 0) {
                        ISOMsg response = null;
                        if (destMux.isConnected()) {
                            ISORequest req = new ISORequest (c);
                            destMux.queue (req);
                            evt.addMessage ("<queued/>");
                            response = req.getResponse (timeout);
                        } else
                            evt.addMessage ("<mux-not-connected/>");
                        if (response != null) {
                            evt.addMessage ("<got-response/>");
                            evt.addMessage (response);
                            response.setHeader (c.getISOHeader()); 
                            source.send(response);
                        } else {
                            processNullResponse (source, m, evt);
                        }
                    } else {
                        evt.addMessage ("<sent-through-mux/>");
                        destMux.send (c);
                    }
                } else if (destChannel != null) {
                    evt.addMessage ("<sent-to-channel/>");
                    destChannel.send (c);
                }
            } catch (ISOException e) {
                evt.addMessage (e);
            } catch (IOException e) {
                evt.addMessage (e);
            }
            Logger.log (evt);
        }

    }
    public boolean process (ISOSource source, ISOMsg m) {
        pool.execute (new Process (source, m));
        return true;
    }
}
