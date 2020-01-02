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

package org.jpos.iso.filter;

import org.jpos.iso.RawIncomingFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOChannel;
import org.jpos.util.LogEvent;
import org.jpos.util.Dumpable;

public class DebugFilter implements RawIncomingFilter {
    public ISOMsg filter(ISOChannel channel, ISOMsg m, byte[] header, byte[] image, LogEvent evt)
            throws VetoException
    {
        if (evt != null) {
            if (header != null)
                evt.addMessage (new Dumpable("header", header));
            if (image != null)
                evt.addMessage (new Dumpable ("image", image));
        }
        return m;
    }

    public ISOMsg filter(ISOChannel channel, ISOMsg m, LogEvent evt) throws VetoException {
        return m;
    }
}
