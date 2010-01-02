/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

import java.util.ArrayList;
import java.util.Map;

/**
 * MasterCard EBCDIC SubField packager
 * @author Eoin Flood
 * @author Mark Salter
 * @version $Revision: 2706 $ $Date: 2009-03-05 11:24:43 +0000 (Thu, 05 Mar 2009) $
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 *
 * This packager can be used is to handle EBCDIC subfields
 * such as field 48 for MasterCard.
 */
public class MasterCardEBCDICSubFieldPackager extends ISOBasePackager
{ 
    /** 
     * Default constructor
     */
    public MasterCardEBCDICSubFieldPackager()
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
            Map tab = c.getChildren();
            ArrayList l = new ArrayList();

            // Handle first IF_CHAR field
            ISOField f0 = (ISOField) tab.get (new Integer(0));
            if (f0 != null) {
                String s = (String) f0.getValue();
                len += s.length();
                l.add (fld[0].pack (f0));
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

        // Now unpack the fields
        while (consumed < b.length) {
            byte[] length = new byte[2];
            System.arraycopy(b, consumed,length,0,2);
            int i = Integer.parseInt(new String(ISOUtil.ebcdicToAscii(length)));

            if (fld[i] == null)
                throw new ISOException ("Unsupported sub-field " + i + " unpacking field " + m.getKey());

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

