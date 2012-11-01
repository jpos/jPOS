/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jpos.iso.AsciiPrefixer;
import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.Prefixer;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

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
        try {
            int len =0;
            Map tab = c.getChildren();
            List<byte[]> l = new ArrayList();

            for (Map.Entry ent: (Set<Map.Entry>)tab.entrySet()){
                Integer i = (Integer)ent.getKey();
                if (i < 0)
                    continue;
                if (fld[i] == null)
                    throw new ISOException ("Unsupported sub-field " + i + " packing field " + c.getKey());
                if (ent.getValue() instanceof ISOComponent)
                    try {
                        ISOComponent f = (ISOComponent) ent.getValue();
                        byte[] b = fld[i].pack(f);
                        len += b.length;
                        l.add (b);
                    } catch (Exception e) {
                        evt.addMessage ("error packing subfield "+i);
                        evt.addMessage (c);
                        evt.addMessage (e);
                        throw e;
                    }
            }
            int k=0;
            byte[] d = new byte[len];
            for (byte[] b :l) {
                System.arraycopy(b, 0, d, k, b.length);
                k += b.length;
            }
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
        ISOComponent c;

        // Unpack the fields
        while (consumed < b.length) {
            //Determine current tag
            int i = consumed==0&&fld[0]!=null?0:tagPrefixer.decodeLength(b, consumed);

            if (fld[i] == null)
                throw new ISOException ("Unsupported sub-field " + i + " unpacking field " + m.getKey());

            c = fld[i].createComponent(i,fld[i].getDisplay());
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

