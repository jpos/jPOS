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

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogSource;

/**
 * Space based MUX implementation
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @see org.jpos.iso.MUX
 */
public class SpaceMUX extends SimpleLogSource
       implements MUX, ReConfigurable, SpaceListener
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
}

