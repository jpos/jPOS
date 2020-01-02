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

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class Connector implements Configurable, SpaceListener
{
    LocalSpace sp;
    Configuration cfg;
    String from, to;
    public Connector () {
        super ();
        sp = TransientSpace.getSpace ();
    }
    public Connector (String from, String to) {
        this ();
        this.from = from;
        this.to   = to;
        sp.addListener (from, this);
    }
    public void setConfiguration (Configuration cfg) 
    {
        if (this.cfg != null) 
            sp.removeListener (from, this);

        this.cfg = cfg;
        from   = cfg.get ("from");
        to     = cfg.get ("to");

        sp.addListener (from, this);
    }
    public void notify (Object key, Object value) {
        Object o = sp.inp (key);
        if (o != null)
            sp.out (to, o);
    }
}

