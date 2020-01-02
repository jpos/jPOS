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

package org.jpos.iso;
import java.io.IOException;
import java.io.InputStream;


/**
 * ISOFieldPackager Binary Hex Fixed LLBINARY
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_LLHFBINARY extends ISOFieldPackager {
    public IFB_LLHFBINARY() {
        super();
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFB_LLHFBINARY (int len, String description) {
    super(len, description);
    }
   /**
    * @param c - a component
    * @return packed component
    * @exception ISOException
    */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len = ((byte[]) c.getValue()).length;
        if (len > getLength() || len>255) {
            throw new ISOException (
                "invalid len "+len +" packing field "+ c.getKey()
            );
        }
        byte[] b = new byte[getLength() + 1];
        b[0] = (byte) len;
        System.arraycopy(c.getValue(), 0, b, 1, len);
        return b;
    }
   /**
    * @param c - the Component to unpack
    * @param b - binary image
    * @param offset - starting offset within the binary image
    * @return consumed bytes
    * @exception ISOException
    */
    public int unpack (ISOComponent c, byte[] b, int offset) throws ISOException
    {
        int len = b[offset] & 0xFF;
        byte[] value = new byte[len];
        System.arraycopy(b, ++offset, value, 0, len);
        c.setValue (value);
        return getLength()+1;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {
        byte[] b = readBytes (in, 1);
        int len = b[0] & 0xFF;
        c.setValue (readBytes (in, len));
        in.skip (getLength () - len);
    }
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBinaryField (fieldNumber);
    }
    public int getMaxPackedLength() {
        return getLength() + 1;
    }
}

