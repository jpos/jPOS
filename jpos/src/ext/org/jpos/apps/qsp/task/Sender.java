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

package org.jpos.apps.qsp.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.core.Sequencer;
import org.jpos.core.VolatileSequencer;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequest;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogSource;

/**
 * Reference implementation of a user task
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class Sender 
    extends SimpleLogSource
    implements Runnable, ReConfigurable, SenderMBean
{
    ISOMUX mux;
    File message;
    long initialDelay;
    long delay;
    int  waitForResponse;
    long lastRoundTrip = -1;
    Logger logger;
    String realm;
    ISOPackager packager;
    Sequencer seq;
    Configuration cfg;
    boolean disabled = false;
    String lastResponseCode = null;
    Thread thisThread;

    public Sender() {
        super();
    }

    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        try {
            mux = ISOMUX.getMUX (cfg.get ("mux"));
            message = new File (cfg.get ("message"));
            initialDelay    = cfg.getLong ("initial-delay");
            waitForResponse = cfg.getInt  ("wait-for-response");
            delay           = cfg.getLong ("delay");
            packager        = new XMLPackager();

            String seqName  = cfg.get ("sequencer", null);
            if (seqName != null) {
                seq = (Sequencer) NameRegistrar.get (
                    "sequencer."+cfg.get("sequencer")
                );
            } else if (seq == null) {
                seq = new VolatileSequencer();
            }
        } catch (NameRegistrar.NotFoundException e) {
            throw new ConfigurationException (e);
        } catch (ISOException e) {
            throw new ConfigurationException (e);
        }
    }

    private void applyProps (ISOMsg m) throws ISOException {
        for (int i=0; i<128; i++) {
            if (m.hasField(i) && m.getValue(i) instanceof String) {
                String value = (String) m.getValue(i);
                if (value.equalsIgnoreCase ("$date") )
                    m.set (new ISOField (i, ISODate.getDateTime(new Date())));
                else if ((value.toLowerCase().startsWith ("$date") ) && (value.indexOf("GMT") > 0)) {
                    String zoneID = value.substring(value.indexOf("GMT"));
                    m.set (new ISOField (i, ISODate.getDateTime(new Date(), TimeZone.getTimeZone(zoneID))));
                }                    
                else if (value.charAt (0) == '$')
                    m.set (new ISOField (i,
                      ISOUtil.zeropad (
                        Integer.toString(seq.get (value.substring(1))),6)
                      )
                    );
                else if (value.charAt (0) == '=') {
                    String p = cfg.get (value.substring(1), null);
                    if (p != null)
                        m.set (new ISOField (i, p));
                }
            }
        }
    }

    public void run () {
        thisThread = Thread.currentThread ();
        if (initialDelay > 0)
            try {
                Thread.sleep (initialDelay);
            } catch (InterruptedException e) { }

        do {
            LogEvent evt = new LogEvent (this, "sender-run");
            try {
                sendOne (evt);
            } catch (Throwable e) {
                evt.addMessage (e);
            } finally  {
                Logger.log (evt);
            }
            if (delay > 0)
                try {
                    Thread.sleep (delay);
                } catch (InterruptedException e) { }
        } while (!cfg.getBoolean ("one-shot"));
    }

    public void sendOne(LogEvent evt) throws IOException {
        FileInputStream fis = new FileInputStream (message);
        try {
            byte[] b = new byte[fis.available()];
            fis.read (b);
            ISOMsg m = new ISOMsg ();
            m.setPackager (packager);
            m.unpack (b);
            applyProps (m);
            evt.addMessage (m);
            ISORequest req = new ISORequest (m);
            long start = System.currentTimeMillis ();
            mux.queue (req);
            if (waitForResponse > 0) {
                ISOMsg resp = req.getResponse (waitForResponse);
                if (resp != null) {
                    lastRoundTrip = System.currentTimeMillis () - start;

                    StringBuffer sb = new StringBuffer (resp.getMTI());
                    if (resp.hasField (39)) {
                        sb.append (' ');
                        sb.append (resp.getString (39));
                    }
                    if (resp.hasField (38)) {
                        sb.append (' ');
                        sb.append (resp.getString (38));
                    }
                    lastResponseCode = sb.toString ();

                    evt.addMessage (resp);
                }
                else {
                    lastRoundTrip = -1;
                    lastResponseCode = "<timed-out>";
                }
            }
        } catch (ISOException e) {
            evt.addMessage (e);
        } finally {
            fis.close();
        }
    }

    public void forceSend () {
        LogEvent evt = new LogEvent (this, "force-send");
        try {
            if (!disabled)
                sendOne (evt);
        } catch (IOException e) {
            evt.addMessage (e);
        }
        Logger.log (evt);
    }
    public long getLastRoundTrip () {
        return lastRoundTrip;
    }
    public void setDisabled (boolean disabled) {
        if (disabled != this.disabled) {
            Logger.log (
                new LogEvent (this, "sender", 
                    "sender task has been " + 
                        (disabled ? "disabled" : "enabled")
                        )
            );
        }
        this.disabled = disabled;
    }
    public boolean isDisabled () {
        return disabled;
    }
    public void setDelay (long delay) {
        this.delay = delay;
        thisThread.interrupt ();
    }
    public long getDelay () {
        return delay;
    }
    public String getLastResponseCode () {
        return lastResponseCode;
    }
}

