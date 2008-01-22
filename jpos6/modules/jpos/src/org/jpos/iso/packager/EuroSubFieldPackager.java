/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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
import java.util.Hashtable;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOPackager;
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
    /** 
     * Default constructor
     */
    public EuroSubFieldPackager()
    {
        super();
    }

    /**
     * Always return false
     */
    protected boolean emitBitMap()
    {
        return false;
    }

    public byte[] pack (ISOComponent c) throws ISOException {
        try {
            int len =0;
            Hashtable tab = c.getChildren();
            ArrayList l = new ArrayList();

            // Handle first IF_CHAR field
            ISOField f0 = (ISOField) tab.get (new Integer(0));
            if (f0 != null) {
                String s = (String) f0.getValue();
                len += s.length();
                l.add (s.getBytes());
            }
            for (int i =1; i<fld.length; i++) {
                Object obj = tab.get (new Integer(i));
                if (obj instanceof ISOField) {
                    ISOField f = (ISOField) obj;
                    byte[] b = fld[i].pack(f);
                    len += b.length;
                    l.add (b);
                } 
            }
            int k=0;
            byte[] d = new byte[len];
            for (int i=0; i<l.size(); i++) {
                byte[] b = (byte[]) l.get(i);
                for (int j=0; j<b.length; j++)
                    d[k++] = b[j];
            }
            return d;
        }
        catch (Exception ex)
        {
            throw new ISOException (ex);
        }
    }

    public int unpack (ISOComponent m, byte[] b) throws ISOException
    {
        LogEvent evt = new LogEvent (this, "unpack");
        // Unpack the IF_CHAR field
        int consumed = 0;
        ISOComponent c;
        if (fld[0] != null) {
            c = fld[0].createComponent(0);
            consumed += fld[0].unpack (c, b, consumed);
            m.set(c);
        }

        // Now unpack the IFEP_LLCHAR fields
        for (int i=1; consumed < b.length ; i++) 
        {
            if (fld[i] == null)
                continue;
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

