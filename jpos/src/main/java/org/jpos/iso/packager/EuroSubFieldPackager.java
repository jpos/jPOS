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

package org.jpos.iso.packager;

import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * EuroPay SubField packager
 * @author Eoin Flood
 * @version $Revision$ $Date$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 *
 * This packager is used by EuroPackager to package subfields
 * such as field 48.
 */
@SuppressWarnings("unchecked")
public class EuroSubFieldPackager extends ISOBasePackager
{
    protected static Prefixer tagPrefixer = AsciiPrefixer.LL;

    /**
     * Always return false
     */
    @Override
    protected boolean emitBitMap()
    {
        return false;
    }

    @Override
    public byte[] pack (ISOComponent c) throws ISOException {
        LogEvent evt = new LogEvent (this, "pack");
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(100)) {
            Map tab = c.getChildren();

            for (Entry ent : (Set<Entry>) tab.entrySet()) {
                Integer i = (Integer) ent.getKey();
                if (i < 0)
                    continue;
                if (fld[i] == null)
                    throw new ISOException ("Unsupported sub-field " + i + " packing field " + c.getKey());
                if (ent.getValue() instanceof ISOComponent)
                    try {
                        ISOComponent f = (ISOComponent) ent.getValue();
                        byte[] b = fld[i].pack(f);
                        bout.write(b);
                    } catch (Exception e) {
                        evt.addMessage ("error packing subfield "+i);
                        evt.addMessage (c);
                        evt.addMessage (e);
                        throw e;
                    }
            }

            byte[] d = bout.toByteArray();
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (d));
            return d;
        }
        catch (Exception ex)
        {
            throw new ISOException (ex);
        }
    }

    @Override
    public int unpack (ISOComponent m, byte[] b) throws ISOException
    {
        LogEvent evt = new LogEvent (this, "unpack");
        int consumed = 0;
        ISOComponent c = null;

        // Unpack the fields
        while (consumed < b.length) {
            //If this is first iteration and there is a packager for SE 0 then i=0, i.e. use field packager for SE 0
            //Else determine current tag
            int i = c == null && fld[0] != null ? 0 : tagPrefixer.decodeLength(b, consumed);

            if (i >= fld.length || fld[i] == null)
                throw new ISOException("Unsupported sub-field " + i + " unpacking field " + m.getKey());

            c = fld[i].createComponent(i);
            consumed += fld[i].unpack (c, b, consumed);
            if (logger != null) 
            {
                evt.addMessage ("<unpack fld=\"" + i 
                    +"\" packager=\""
                    +fld[i].getClass().getName()+ "\">");
                    evt.addMessage ("  <value>" 
                    +c.getValue().toString()
                    + "</value>");
                evt.addMessage ("</unpack>");
            }
            m.set(c);
        }
        Logger.log (evt);
        return consumed;
    }
}

