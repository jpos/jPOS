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

package org.jpos.space;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;


/**
 * Integrates legacy (pre 2.0) ISORequestListeners to the new Space 
 * based architecture
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class ISORequestListenerAdaptor extends SimpleLogSource 
        implements Configurable, ISORequestListener
{
    Space sp;
    Configuration cfg;
    ISOChannel channel;
    SpaceMUX mux;
    ThreadPool pool;
    long timeout;
    public ISORequestListenerAdaptor () {
        super ();
        sp = TransientSpace.getSpace ();
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        this.cfg = cfg;
        try {
            mux = (SpaceMUX) NameRegistrar.get (cfg.get ("mux"));
        } catch (NotFoundException e) {
            throw new ConfigurationException (e);
        }
        timeout = cfg.getLong ("timeout", 120000);
        pool    = new ThreadPool (1, cfg.getInt ("pool", 100));
    }

    public class Processor implements Runnable {
        ISOSource source;
        ISOMsg m;
        public Processor (ISOSource source, ISOMsg m) {
            super ();
            this.source = source;
            this.m      = m;
        }
        public void run () {
            try {
                ISOMsg resp = mux.request (m, timeout);
                if (resp != null)
                    source.send (resp);
            } catch (Exception e) {
                Logger.log (
                    new LogEvent (
                        ISORequestListenerAdaptor.this, 
                        "iso-request-listener-adaptor", e
                    )
                );
            }
        }
    }

    public boolean process (ISOSource source, ISOMsg m) {
        pool.execute (new Processor (source, m));
        return false;   // let other listeners do their work too
    }
}

