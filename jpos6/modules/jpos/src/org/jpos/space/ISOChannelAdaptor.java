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

import java.io.IOException;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogSource;


/**
 * Integrates legacy (pre 2.0) ISOChannels to new Space based architecture
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class ISOChannelAdaptor 
        extends SimpleLogSource 
        implements Configurable
{
    Space sp;
    Configuration cfg;
    ISOChannel channel;
    String to, from, ready;
    public ISOChannelAdaptor () {
        super ();
        sp = TransientSpace.getSpace ();
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        this.cfg = cfg;
        try {
            to   = get ("to");
            from = get ("from");
            channel = (ISOChannel) NameRegistrar.get (
                "channel." + get ("channel")
            );
            ready  = channel.toString() + ".ready";
            new Thread (new Sender ()).start ();
            new Thread (new Receiver ()).start ();
        } catch (NameRegistrar.NotFoundException e) {
            throw new ConfigurationException (e);
        }
    }
    public class Sender implements Runnable {
        public Sender () {
            super ();
        }
        public void run () {
            Thread.currentThread().setName ("channel-sender-" + to);
            for (;;) {
                try {
                    checkConnection ();
                    Object o = sp.in (to, 10000);
                    if (o instanceof ISOMsg)
                        channel.send ((ISOMsg) o);
                } catch (Exception e) { 
                    Logger.log (
                        new LogEvent (
                            ISOChannelAdaptor.this, "channel-sender"+to, e
                        )
                    );
                    ISOUtil.sleep (1000);
                }
            }
        }
    }
    public class Receiver implements Runnable {
        public Receiver () {
            super ();
        }
        public void run () {
            Thread.currentThread().setName ("channel-receiver-"+from);
            for (;;) {
                try {
                    sp.rd (ready);
                    ISOMsg m = channel.receive ();
                    sp.out (from, m);
                } catch (Exception e) { 
                    Logger.log (
                        new LogEvent (
                            ISOChannelAdaptor.this, "channel-receiver-"+from, e
                        )
                    );
                    disconnect ();
                }
            }
        }
    }
    protected void checkConnection () {
        try {
            while (!channel.isConnected ()) {
                while (sp.inp (ready) != null)
                    ;
                channel.connect ();
                if (!channel.isConnected ())
                    ISOUtil.sleep (10000);
            }
            sp.out (ready, new Object ());
        } catch (IOException e) {
            Logger.log (
                new LogEvent (
                    ISOChannelAdaptor.this, "check-connection", e
                )
            );
            ISOUtil.sleep (10000);
        }
    }
    protected void disconnect () {
        try {
            while (sp.inp (ready) != null)
                ;
            channel.disconnect ();
        } catch (IOException e) {
            Logger.log (
                new LogEvent (
                    ISOChannelAdaptor.this, "disconnect", e
                )
            );
        }
    }
    private String get (String prop) throws ConfigurationException
    {
        String value = cfg.get (prop);
        if (value == null)
            throw new ConfigurationException ("null property "+prop);
        return value;
    }
}

