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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.*;
import org.jpos.util.NameRegistrar.NotFoundException;

import java.io.IOException;

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
    private boolean preserveSourceHeader = true;
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
        preserveSourceHeader = cfg.getBoolean ("preserve-source-header", true);
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
                        if (preserveSourceHeader)
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
