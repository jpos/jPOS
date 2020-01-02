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
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOResponseListener;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogSource;

import java.io.IOException;

/**
 * Space based MUX implementation
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @see org.jpos.iso.MUX
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class SpaceMUX extends SimpleLogSource
       implements MUX, Configurable, SpaceListener
{
    LocalSpace sp;
    Configuration cfg;
    String to, from, unhandled, name;
    public SpaceMUX () {
        super ();
        sp = TransientSpace.getSpace ();
    }
    /**
     * @param to    output queue
     * @param from  input  queue
     * @param unhandled optional unhandled queue (may be null)
     */
    public SpaceMUX (String to, String from, String unhandled) {
        this ();
        this.to        = to;
        this.from      = from;
        this.unhandled = unhandled;
        sp.addListener (from, this);
    }

    /**
     * reads "to", "from" and optional "unhandled" properties
     * @param cfg 
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg  = cfg;
        to        = get ("to");
        from      = get ("from");
        unhandled = cfg.get ("unhandled");  // can be null

        sp.addListener (from, this);
    }
    /**
     * @param m message to send
     * @param timeout amount of time in millis to wait for a response
     * @return response or null
     */
    public ISOMsg request (ISOMsg m, long timeout) throws ISOException {
        String key = getKey (m);
        String req = key + ".req";
        sp.out (req, m);
        sp.out (to, m);

        ISOMsg resp = (ISOMsg) sp.in (key, timeout);

        if (resp == null && sp.inp (req) == null) {
            // possible race condition, retry for a few extra seconds
            resp = (ISOMsg) sp.in (key, 10000);
        }
        return resp;
    }
    public void send (ISOMsg m) throws ISOException, IOException {
        sp.out (to, m);
    }
    protected String getFrom () {
        return from;
    }
    protected String getKey (ISOMsg m) throws ISOException {
        return getFrom () + "." +
           (m.hasField(41)?ISOUtil.zeropad((String)m.getValue(41),16) : "")
           + (m.hasField (11) ?
                ISOUtil.zeropad((String) m.getValue(11),6) :
                Long.toString (System.currentTimeMillis()));
    }
    public void notify (Object k, Object value) {
        Object obj = sp.inp (k);
        if (obj instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) obj;
            try {
                String key = getKey (m);
                String req = key + ".req";
                if (sp.inp (req) != null) {
                    sp.out (key, m);
                    return;
                }
            } catch (ISOException e) { 
                Logger.log (new LogEvent (this, "notify", e));
            }
            if (unhandled != null)
                sp.out (unhandled, m, 120000);
        }
    }
    private String get (String prop) throws ConfigurationException
    {
        String value = cfg.get (prop);
        if (value == null)
            throw new ConfigurationException ("null property "+prop);
        return value;
    }
    /**
     * associates this MUX with a name using NameRegistrar
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName (String name) {
        this.name = name;
        NameRegistrar.register ("mux."+name, this);
    }
    /**
     * @return this ISOMUX's name ("" if no name was set)
     */
    public String getName() {
        return this.name;
    }
    /**
     * @return MUX instance with given name.
     * @throws NameRegistrar.NotFoundException;
     * @see NameRegistrar
     */
    public static MUX getMUX (String name)
        throws NameRegistrar.NotFoundException
    {
        return (MUX) NameRegistrar.get ("mux."+name);
    }
    public boolean isConnected () {
        return true;
    }
    public void request (ISOMsg m, long timeout, ISOResponseListener r, Object handBack) 
        throws ISOException 
    {
        throw new ISOException ("Not implemented");
    }
}

