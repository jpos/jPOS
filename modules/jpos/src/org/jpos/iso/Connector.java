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

import java.io.IOException;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;

/**
 * Connector implements ISORequestListener
 * and forward all incoming messages to a given
 * destination MUX, or Channel handling back responses
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.iso.ISORequestListener
 */
public class Connector 
    implements ISORequestListener, LogSource, Configurable
{
    private Logger logger;
    private String realm;
    protected String muxName;
    protected String channelName;
    protected int timeout = 0;
    protected static ThreadPool pool;
    
    public Connector () {
        super();
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
    * <li>poolsize
    * </ul>
    * @param cfg Configuration
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        timeout = cfg.getInt ("timeout");
        if (pool == null)
            pool    = new ThreadPool (1, cfg.getInt ("poolsize", 10));
        muxName     = cfg.get ("destination-mux", null);
        channelName = cfg.get ("destination-channel", null);
        if (muxName == null && channelName == null) {
            throw new ConfigurationException("Neither destination mux nor channel were specified.");
        }
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
                if (muxName != null) {
                    MUX destMux = (MUX) NameRegistrar.get (muxName);
                    ISOMsg response = destMux.request (c, timeout);
                    if (response != null) {
                        response.setHeader (c.getISOHeader()); 
                        source.send(response);
                    }
                } else if (channelName != null) {
                    Channel destChannel = (Channel) NameRegistrar.get (channelName);
                    destChannel.send (c);
                }
            } catch (ISOException e) {
                evt.addMessage (e);
            } catch (IOException e) {
                evt.addMessage (e);
            } catch (NotFoundException e) {
                evt.addMessage(e);
            }
            Logger.log (evt);
        }

    }
    public boolean process (ISOSource source, ISOMsg m) {
        if (pool == null) 
            pool = new ThreadPool (1, 10);

        pool.execute (new Process (source, m));
        return true;
    }
}
