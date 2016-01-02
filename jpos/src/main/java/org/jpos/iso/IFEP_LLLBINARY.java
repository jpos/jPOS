/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

package org.jpos.iso;
import java.io.IOException;
import java.io.InputStream;

public class IFEP_LLLBINARY extends ISOBinaryFieldPackager {
    public IFEP_LLLBINARY() {
        super();
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len = ((byte[]) c.getValue()).length;
    
        if (len > getLength())  
            throw new ISOException (
                "invalid len "+len +" packing IFEP_LLLBINARY field "
                +c.getKey().toString()
            );

        byte[] b = new byte[len + 5];
        byte[] llltt = 
            ISOUtil.asciiToEbcdic (
              ISOUtil.zeropad(Integer.toString(len+2), 3)
              +ISOUtil.zeropad(c.getKey().toString(), 2));

        System.arraycopy(llltt, 0, b, 0, 5);
        System.arraycopy(c.getValue(), 0, b, 5, len);
        return b;
    }

    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        int len = Integer.parseInt(ISOUtil.ebcdicToAscii(b, offset, 3)) - 2;
        if (!(c instanceof ISOBinaryField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOBinaryField");

        c.setFieldNumber (
            Integer.parseInt(ISOUtil.ebcdicToAscii (b, offset+3, 2))
        );
        byte[] value = new byte[len];
        System.arraycopy(b, offset+5, value, 0, len);
        c.setValue (value);
        return len + 5;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {

        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");

        int len   = Integer.parseInt(ISOUtil.ebcdicToAscii(readBytes (in, 3))) - 2;
        int fldno = Integer.parseInt(ISOUtil.ebcdicToAscii(readBytes (in, 2)));
        c.setFieldNumber (fldno);
        c.setValue (readBytes(in, len));
    }
    public int getMaxPackedLength() {
        return getLength() + 5;
    }
}
