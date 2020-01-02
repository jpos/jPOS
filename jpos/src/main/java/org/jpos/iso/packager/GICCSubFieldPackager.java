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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOUtil;

/**
 * 
 * This packager is used to package subfields such as field 60 of GICC.
 */
public class GICCSubFieldPackager extends ISOBasePackager {
    /**
     * Default constructor
     */
    public GICCSubFieldPackager() {
            super();
    }

    /**
     * Always return false
     */
    protected boolean emitBitMap() {
            return false;
    }

    public byte[] pack(ISOComponent c) throws ISOException {
            try {
                int len = 0;
                byte[] result;
                Map tab = c.getChildren();

                List list = new ArrayList();

                // Handle first IF_CHAR field
                ISOField f0 = (ISOField) tab.get(0);
                if (f0 != null) {
                String s = (String) f0.getValue();
                list.add(s.getBytes());
                len += s.getBytes().length;
            }
            for (int i = 1; i < fld.length; i++) {
                Object obj = tab.get(i);
                if (obj instanceof ISOComponent)
                {
                    ISOComponent f = (ISOComponent) obj;
                    byte[] b = fld[i].pack(f);
                    list.add(b);
                    len += b.length;
                }
            }

            result = new byte[len];
            int k = 0;
            for (int i = 0; i < list.size(); i++) {
                byte[] b = (byte[]) list.get(i);
                for (int j = 0; j < b.length; j++)
                    result[k++] = b[j];
            }

            return result;
        }
        catch (Exception ex) {
            throw new ISOException(ex);
        }
    }

    public int unpack(ISOComponent m, byte[] b) throws ISOException
    {
        // Unpack the IF_CHAR field
        int consumed = 0;
        ISOComponent c;
        if (fld[0] != null && b[consumed] == 0x20) {
                    // Hack to support a nine-byte filler
            c = fld[0].createComponent(0);
            consumed += fld[0].unpack(c, b, consumed);
            m.set(c);
        }

        // Now unpack the IFEP_LLCHAR fields
        for (; consumed < b.length;)
        {
            int fieldNumber = Integer.parseInt(ISOUtil.ebcdicToAscii(b,
                consumed + 3, 2));
            if (fld[fieldNumber] == null)
                break;
            c = fld[fieldNumber].createComponent(fieldNumber);
            consumed += fld[fieldNumber].unpack(c, b, consumed);
            m.set(c);
        }

        return consumed;
    }
}

