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

package org.jpos.iso;
import java.io.IOException;
import java.io.InputStream;

public class IFEMC_LLCHAR extends ISOFieldPackager {
    public IFEMC_LLCHAR() {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFEMC_LLCHAR (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len;
        String s = (String) c.getValue();
    
        if ((len=s.length()) > getLength() || len>97)
            throw new ISOException (
                "invalid len "+len +" packing IFEMC_LLCHAR field "
                +(Integer) c.getKey() + " maxlen=" + getLength()
            );

        byte[] b = new byte[ len + 4 ];
        System.arraycopy (ISOUtil.zeropad(((Integer) c.getKey()).toString(), 2).getBytes(), 0, b, 0, 2);
        System.arraycopy (ISOUtil.zeropad(Integer.toString(len), 2).getBytes(), 0, b, 2, 2);
        ISOUtil.asciiToEbcdic (s, b, 4);
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
        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");
        int len = Integer.parseInt(new String(b, offset+2, 2));
        ((ISOField)c).setFieldNumber (
            Integer.parseInt(new String(b, offset, 2))
        );
        c.setValue (ISOUtil.ebcdicToAscii(b, offset+4, len));
        return len + 4;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {

        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");

        int fldno = Integer.parseInt(new String(readBytes (in, 2)));
        int len   = Integer.parseInt(new String(readBytes (in, 2)));
        ((ISOField)c).setFieldNumber (fldno);
        c.setValue (ISOUtil.ebcdicToAscii((readBytes (in, len))));
    }

    public int getMaxPackedLength() {
        return getLength() + 4;
    }
}

