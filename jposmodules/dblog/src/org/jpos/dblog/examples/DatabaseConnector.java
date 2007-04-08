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
 *
 * Modified Connector.java by Jeff Gordy
 *
 *
 */

package org.jpos.dblog.examples;


import java.io.IOException;

import java.util.Date;
import java.util.TimeZone;

import java.lang.String;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;
import org.jpos.util.NameRegistrar.NotFoundException;

import org.jpos.space.*;
import org.jpos.iso.*;
import org.jpos.dblog.*;


/**
 * Connector implements ISORequestListener
 * and forward all incoming messages to a given
 * destination MUX, or Channel handling back responses
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.iso.ISORequestListener
 *
 * This file is a modified version of Connector by Jeff Gordy.  It receives a
 * message from one server, manipulates the fileds and puts it in the eFunds scan
 * format
 *
 * It then sends the message to the interchange.
 * It receives the reply and transmits the
 * original message back to the requester with the response code.
 */
public class DatabaseConnector
    implements ISORequestListener, LogSource, Configurable
{
    Logger logger;
    String realm;
    MUX destMux;
    Channel destChannel;
    int timeout = 0;
    static ThreadPool pool;

    public DatabaseConnector () {
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
        String muxName     = cfg.get ("destination-mux", null);
        String channelName = cfg.get ("destination-channel", null);
        try {
            if (muxName != null)
                destMux = (MUX) NameRegistrar.get (muxName);
            else if (channelName != null)
                destChannel = (Channel) NameRegistrar.get (channelName);
        } catch (NotFoundException e) {
            throw new ConfigurationException (e);
        }
    }


    protected class Process implements Runnable {
        ISOSource source;
        ISOMsg m;
        DatabaseLogger db;
        Configuration cfg = null;

        Process (ISOSource source, ISOMsg m) {
            super();
            this.source = source;
            this.m = m;

            try {
              cfg = new SimpleConfiguration("/home/jgordy/test.cfg");
            }
            catch (Exception e) {
              System.out.println("Error with Configuration File: " + e.getMessage());
            }


            try {
              String[] bits = {"11", "39", "41", "121"};
              this.db = new DatabaseLogger(this.cfg, bits);
            } catch (java.sql.SQLException e) {
              new SQLExceptionHandler(e);
            }
        }

        public void run () {
            LogEvent evt = new LogEvent (DatabaseConnector.this,
                "connector-request-listener");
            try {

                String msgMTI = m.getMTI();
                ISOMsg c = null;
                int requires_scan = 0;
                if (msgMTI.equals("0800") ) {
                    c = handle_client_0800(m);
                } else if (msgMTI.equals("0100") ) {
                    try {
                      db.logIncomingMessage(m);
                    } catch (Exception e) {
                      evt.addMessage(e);
                    }
                    c = handle_client_0100(m, db);
                    requires_scan = 1;
                }

                if (destMux != null && requires_scan == 1) {
                    ISOMsg response = destMux.request (c, timeout);
                    if (response != null) {
                        response.setHeader (c.getISOHeader());

                        try {
                          db.logReplyMessage(response);
                        } catch (Exception e) {
                          evt.addMessage(e);
                        }

                        ISOMsg short_response = (ISOMsg) m.clone();
                        short_response.setResponseMTI();
                        short_response.set(39, response.getString(39) );
                        if (response.hasField(121) ) {
                            short_response.set(121, response.getString(121) );
                        }
                        short_response.setHeader(c.getISOHeader() );

                        try {
                          db.logOutgoingMessage(short_response);
                        } catch (Exception e) {
                          evt.addMessage(e);
                        }

                        source.send(short_response);
                    }
                } else if (destChannel != null && requires_scan == 1) {
                    destChannel.send (c);
                }
                if (requires_scan == 0) {
                    source.send(c);
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
        if (pool == null)
            pool = new ThreadPool (1, 10);

        pool.execute (new Process (source, m));
        return true;
    }

    private ISOMsg handle_client_0800(ISOMsg m) {
        LogEvent evt = new LogEvent (DatabaseConnector.this,
            "connector-interchange-handler");
        ISOMsg c = null;
        try {
            c = (ISOMsg) m.clone();
            c.setResponseMTI();
            c.set(39, "00");
        } catch (ISOException e) {
            evt.addMessage (e);
        }
        return c;
    }


    private ISOMsg handle_client_0100(ISOMsg m, DatabaseLogger db) {
        LogEvent evt = new LogEvent (DatabaseConnector.this,
            "connector-interchange-handler");
        ISOMsg c = null;
        try {
            c = (ISOMsg) m.clone();

            // set processing code to "Check Verification"
            c.set(3, "040000");

            // set bit 7 to the date
            Date d = new Date();
            TimeZone tz = TimeZone.getTimeZone("GMT");
            c.set(7, ISODate.getDateTime(d, tz) );

            // set bit 11 to our internal system trace audit number
            Space sp = SpaceFactory.getSpace("jdbm:sequencers");
            long l = SpaceUtil.nextLong (sp, "SYSTRACE");
            c.set(11, Long.toString(l));

            // set bit 12 to the local transaction time
            c.set(12, ISODate.getTime(d) );

            // set bit 13 to the local transaction date
            c.set(13, ISODate.getDate(d) );

            // set bit 15 to the local settlement date
            c.set(15, ISODate.getDate(d) );

            // set bit 18 to the merchant type sic code
            c.set(18, "5411");

            // set bit 32 to our "Acquiring Institution ID"
            c.set(32, "0000000000");

            // set bit 37
            String termSeq = Long.toString(l);
            termSeq = "00000000" + termSeq;
            int len = termSeq.length();
            termSeq = termSeq.substring(len - 8);
            c.set(37, ISODate.getJulianDate(d) + termSeq);

            // set bit 42 to FEDChex Merchant Designator
            c.set(42, "TST");

            // set bit 43 Card acceptor name and location
            String address = "12345 Happy Street, Suite E";
            String city = "Irvine";
            String state = "CA";
            String country = "US";
            // address is 23 characters long
            len = address.length();
            if (len > 23) {
                address = address.substring(len - 23);
            } else {
                for (int i = 23 - len; i > 0; i--) {
                    address = address.concat(" ");
                }
            }
            // city is 13 characters long
            len = city.length();
            if (len > 13) {
                city = city.substring(len - 13);
            } else {
                for (int i = 13 - len; i > 0; i--) {
                    city = city.concat(" ");
                }
            }

            c.set(43, address + city + state + country);

            // set bit 49 Curency Code
            c.set(49, "840");

            // set bit 123 AVS/Check Authorization Data
            String b123 = "TDMF";
            String micr_part_formatted = null;
            String dl_part_formatted = null;

            String aba = c.getString(121);
            String acct = c.getString(122);
            String checkno = c.getString(124);
            String dlnum = c.getString(125);
            String dlstate = c.getString(119);

            c.unset(121);
            c.unset(122);
            c.unset(124);
            c.unset(125);
            c.unset(119);

            len = aba.length();
            if (len > 9) {
                aba = aba.substring(len - 9);
            } else {
                for (int i = 9 - len; i > 0; i--) {
                    aba = aba.concat("0");
                }
            }

            len = acct.length();
            if (len > 18) {
                acct = acct.substring(len - 18);
            } else {
                for (int i = 18 - len; i > 0; i--) {
                    acct = acct.concat(" ");
                }
            }

            len = checkno.length();
            if (len > 12) {
                checkno = checkno.substring(len - 12);
            }

            micr_part_formatted = aba + acct + checkno;
            len = micr_part_formatted.length();
            b123 = b123 + len + micr_part_formatted;

            if (dlnum.length() > 0) {
                len = dlnum.length();
                if (len > 28) {
                    dlnum = dlnum.substring(len - 28);
                }

                len = dlstate.length();
                if (len > 2) {
                    dlstate = dlstate.substring(len - 2);
                } else {
                    for (int i = 2 - len; i > 0; i--) {
                        dlstate = dlstate.concat(" ");
                    }
                }

                dl_part_formatted = dlstate + dlnum;
                len = dl_part_formatted.length();
                String paddedLen = "0" + len;
                int len2 = paddedLen.length();
                paddedLen = paddedLen.substring(len2 - 2);
                b123 = b123 + "DR" + paddedLen + dl_part_formatted;
            }

            c.set(123, b123);

            try {
              db.logTransformMessage(c);
            } catch (Exception e) {
              evt.addMessage(e);
            }

        } catch (ISOException e) {
            evt.addMessage (e);
        }
        return c;
    }
}








