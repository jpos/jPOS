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

package org.jpos.space;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.util.*;
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

