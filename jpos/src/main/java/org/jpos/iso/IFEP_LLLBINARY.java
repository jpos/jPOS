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

public class IFEP_LLLBINARY extends ISOBinaryFieldPackager {

    private static final int TAG_HEADER_LENGTH = 2;
    private final int prefixerPackedLength;

    public IFEP_LLLBINARY() {
        super();
        prefixerPackedLength = 3;
    }

    public IFEP_LLLBINARY(int length, String description) {
        super(length, description, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL);
        checkLength(length, 999);
        prefixerPackedLength = EbcdicPrefixer.LLL.getPackedLength();
    }

    public IFEP_LLLBINARY(int length, String description, BinaryInterpreter binaryInterpreter, Prefixer prefixer) {
        super(length, description, binaryInterpreter, prefixer);
        checkLength(length, 999);
        prefixerPackedLength = prefixer.getPackedLength();
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

        byte[] b = new byte[len + prefixerPackedLength + TAG_HEADER_LENGTH];
        byte[] llltt = 
            ISOUtil.asciiToEbcdic (
              ISOUtil.zeropad(Integer.toString(len+ TAG_HEADER_LENGTH), prefixerPackedLength)
              +ISOUtil.zeropad(c.getKey().toString(), TAG_HEADER_LENGTH));

        System.arraycopy(llltt, 0, b, 0, TAG_HEADER_LENGTH + prefixerPackedLength);
        System.arraycopy(c.getValue(), 0, b, TAG_HEADER_LENGTH + prefixerPackedLength, len);
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
        int len = Integer.parseInt(ISOUtil.ebcdicToAscii(b, offset, prefixerPackedLength)) - TAG_HEADER_LENGTH;
        if (!(c instanceof ISOBinaryField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOBinaryField");

        c.setFieldNumber (
            Integer.parseInt(ISOUtil.ebcdicToAscii (b, offset+prefixerPackedLength, TAG_HEADER_LENGTH))
        );
        byte[] value = new byte[len];
        System.arraycopy(b, offset + prefixerPackedLength + TAG_HEADER_LENGTH , value, 0, len);
        c.setValue (value);
        return len + prefixerPackedLength + TAG_HEADER_LENGTH;
    }
    public void unpack (ISOComponent c, InputStream in) 
        throws IOException, ISOException
    {

        if (!(c instanceof ISOField))
            throw new ISOException 
                (c.getClass().getName() + " is not an ISOField");

        int len   = Integer.parseInt(ISOUtil.ebcdicToAscii(readBytes (in, prefixerPackedLength))) - TAG_HEADER_LENGTH;
        int fldno = Integer.parseInt(ISOUtil.ebcdicToAscii(readBytes (in, TAG_HEADER_LENGTH)));
        c.setFieldNumber (fldno);
        c.setValue (readBytes(in, len));
    }
    public int getMaxPackedLength() {
        return getLength() + prefixerPackedLength + TAG_HEADER_LENGTH;
    }
}
