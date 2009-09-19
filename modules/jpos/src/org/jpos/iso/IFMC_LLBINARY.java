/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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

/**
 * Binary version of IFMC_LLCHAR 
 * @see IFMC_LLCHAR 
 */
public class IFMC_LLBINARY extends ISOFieldPackager {
    public IFMC_LLBINARY() {
        super();
    }
    public IFMC_LLBINARY (int len, String description) {
        super(len, description);
    }
    public byte[] pack (ISOComponent c) throws ISOException {
        int len;
        byte[] v = (byte[]) c.getValue();
    
        if ((len=v.length) > getLength() || len>96)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing LLMC_BINARY field "
                +(Integer) c.getKey() + " maxlen=" + getLength()
            );



        byte[] b = new byte[ len + 4 ];
        System.arraycopy (ISOUtil.zeropad(((Integer) c.getKey()).toString(), 2).getBytes(), 0, b, 0, 2);
        System.arraycopy (ISOUtil.zeropad(Integer.toString(len), 2).getBytes(), 0, b, 2, 2);
        System.arraycopy (v, 0, b, 4, len);
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
        if (!(c instanceof ISOBinaryField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOBinaryField");
        int len = Integer.parseInt(new String(b, offset+2, 2));
        ((ISOBinaryField)c).setFieldNumber (
            Integer.parseInt(new String(b, offset, 2))
        );
        byte[] v = new byte[len];
        System.arraycopy (b, offset+4, v, 0, len);
        c.setValue (v);
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
        c.setValue (readBytes (in, len));
    }
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBinaryField (fieldNumber);
    }
    public int getMaxPackedLength() {
        return getLength() + 4;
    }
}
