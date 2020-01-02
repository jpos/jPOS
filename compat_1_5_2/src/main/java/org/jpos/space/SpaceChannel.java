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
import org.jpos.iso.Channel;
import org.jpos.iso.ISOMsg;

/**
 * Space based Channel implementation
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @see org.jpos.iso.Channel
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class SpaceChannel implements Channel, Configurable
{
    Space sp;
    Configuration cfg;
    String from, to;
    public SpaceChannel () {
        super ();
        sp = TransientSpace.getSpace ();
    }
    /**
     * @param to    output queue
     * @param from  input  queue
     */
    public SpaceChannel (String to, String from) {
        this ();
        this.to   = to;
        this.from = from;
    }
    /**
     * reads "to" and "from" properties
     * @param cfg 
     */
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
        to       = cfg.get ("to");
        from     = cfg.get ("from");
    }
    /**
     * @param m message to send
     */
    public void send (ISOMsg m) {
        sp.out (to, m);
    }
    /**
     * Blocks until a message is received
     * @return received message
     */
    public ISOMsg receive () {
        return (ISOMsg) sp.in (from);
    }
    /**
     * Blocks for a given period of time until a message is received
     * @param timeout time to wait in millis
     * @return received message
     */
    public ISOMsg receive (long timeout) {
        return (ISOMsg) (timeout == 0 ? sp.inp (from) : sp.in (from, timeout));
    }
}

